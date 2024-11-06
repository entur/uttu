package no.entur.uttu.ext.fintraffic.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mizosoft.methanol.FormBodyPublisher;
import com.github.mizosoft.methanol.Methanol;
import com.github.mizosoft.methanol.MutableRequest;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import no.entur.uttu.ext.fintraffic.security.model.EntraTokenResponse;
import no.entur.uttu.ext.fintraffic.security.model.Me;
import no.entur.uttu.ext.fintraffic.security.model.VacoApiResponse;
import no.entur.uttu.security.spi.UserContextService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * Fintraffic uses a combination of <a href="https://learn.microsoft.com/en-us/entra/">Entra</a>, <a href="https://learn.microsoft.com/en-us/graph/overview">Microsoft Graph</a>
 * and in-house TIS VACO service specific data to determine user's access to various resources.
 */
public class FintrafficUserContextService implements UserContextService {

  private static final Logger logger = LoggerFactory.getLogger(
    FintrafficUserContextService.class
  );

  /**
   * Microsoft Graph authentication scope requires this as its hardcoded value.
   */
  private static final String MICROSOFT_GRAPH_SCOPE =
    "https://graph.microsoft.com/.default";
  /**
   * Microsoft Graph API root URI.
   *
   * @see <a href="https://learn.microsoft.com/en-us/graph/api/overview?view=graph-rest-1.0">Microsoft Graph REST API v1.0 endpoint reference</a>
   */
  private static final String MICROSOFT_GRAPH_API_ROOT =
    "https://graph.microsoft.com/v1.0";

  /**
   * Generic Microsoft Entra login URI, provided as template. Call <code>"...".format(tenantId)</code> to acquire
   * working login URI.
   *
   * @see <a href="https://learn.microsoft.com/en-us/entra/">Microsoft Entra</a>
   */
  private static final String MICROSOFT_ENTRA_LOGIN_URI =
    "https://login.microsoftonline.com/%s/oauth2/v2.0/token";

  /**
   * Entra tenant id for accessing any Entra or Graph protected resource within the represented tenant.
   *
   * @see #MICROSOFT_ENTRA_LOGIN_URI
   */
  private final String tenantId;

  /**
   * Entra client id for accessing Fintraffic TIS VACO API.
   */
  private final String clientId;

  /**
   * Entra client secret for accessing Fintraffic TIS VACO API.
   */
  private final String clientSecret;

  /**
   * Authentication scope for accessing Fintraffic TIS VACO API.
   */
  private final String scope;

  /**
   * Microsoft Entra application role's id for admin identifying users.
   */
  private final String adminRoleId;

  /**
   * Fintraffic TIS VACO service's API root URI.
   */
  private final String vacoApi;

  /**
   * Stores authentication scope to {@link EntraTokenResponse} for reusing authentication tokens.
   */
  private final ConcurrentMap<String, AtomicReference<EntraTokenResponse>> authenticationTokens =
    new ConcurrentHashMap<>();

  private final Methanol httpClient;

  /**
   * Internal {@link ObjectMapper} used for VACO API interactions, isolated from Spring's singleton on purpose to avoid
   * accidental misconfigurations.
   */
  private final ObjectMapper objectMapper;

  public FintrafficUserContextService(
    String tenantId,
    String clientId,
    String clientSecret,
    String scope,
    String adminRoleId,
    String vacoApi
  ) {
    this.tenantId = tenantId;
    this.clientId = clientId;
    this.clientSecret = clientSecret;
    this.scope = scope;
    this.adminRoleId = adminRoleId;
    this.vacoApi = vacoApi;
    this.objectMapper = initializeObjectMapper();
    this.httpClient = initializeHttpClient();
  }

  private static ObjectMapper initializeObjectMapper() {
    ObjectMapper om = new ObjectMapper();
    om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    return om;
  }

  private static Methanol initializeHttpClient() {
    return Methanol
      .newBuilder()
      .connectTimeout(Duration.ofSeconds(30))
      .requestTimeout(Duration.ofSeconds(30))
      .headersTimeout(Duration.ofSeconds(30))
      .readTimeout(Duration.ofSeconds(30))
      .followRedirects(HttpClient.Redirect.NORMAL)
      .userAgent("Entur Uttu/" + LocalDate.now().format(DateTimeFormatter.ISO_DATE))
      .build();
  }

  /**
   * {@inheritDoc}
   *
   * @return User's preferred name defined by the <code>preferred_username</code> claim provided by Entra.
   */
  @Override
  public String getPreferredName() {
    return getToken()
      .map(t -> t.getClaimAsString("preferred_username"))
      .orElse("unknown");
  }

  private static Optional<Jwt> getToken() {
    if (
      SecurityContextHolder
        .getContext()
        .getAuthentication() instanceof JwtAuthenticationToken token &&
      (token.getPrincipal() instanceof Jwt jwt)
    ) {
      return Optional.of(jwt);
    }
    return Optional.empty();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isAdmin() {
    return login(MICROSOFT_GRAPH_SCOPE)
      .flatMap(this::msGraphAppRoleAssignments)
      .map(tree -> {
        JsonNode assignedAppRoles = tree.path("value");
        if (assignedAppRoles.isArray()) {
          for (JsonNode assignedAppRole : assignedAppRoles) {
            if (
              assignedAppRole.has("appRoleId") &&
              assignedAppRole.get("appRoleId").asText().equals(adminRoleId)
            ) {
              return true;
            }
          }
        }
        return false;
      })
      .orElse(false);
  }

  /**
   * Hand-rolled implementation of acquiring app role assignments from MS Graph to avoid unnecessarily high amount of
   * dependencies.
   * <p>
   * Works by using service identity for calling MS Graph and requesting current user's role assignments.
   *
   * @param serviceToken Token authorized to access {@link #MICROSOFT_GRAPH_SCOPE}
   * @return Jackson {@link JsonNode} representing successful response from MS Graph or {@link Optional#empty()} if call failed.
   * @see #login(String)
   */
  private Optional<JsonNode> msGraphAppRoleAssignments(EntraTokenResponse serviceToken) {
    return getToken()
      .flatMap(jwt -> {
        MutableRequest request = MutableRequest
          .GET(
            MICROSOFT_GRAPH_API_ROOT +
            "/users/" +
            jwt.getClaimAsString("oid") +
            "/appRoleAssignments"
          )
          .header("Authorization", "Bearer " + serviceToken.getAccessToken())
          .header("Content-Type", "application/json");
        try {
          HttpResponse<String> response = httpClient.send(
            request,
            BodyHandlers.ofString()
          );
          JsonNode tree = objectMapper.readTree(response.body());
          return Optional.of(tree);
        } catch (IOException e) {
          logger.warn("I/O error during API request", e);
        } catch (InterruptedException e) {
          logger.warn(
            "Underlying thread interrupted during HTTP client action, interrupting current thread",
            e
          );
          Thread.currentThread().interrupt();
        }
        return Optional.empty();
      });
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean hasAccessToProvider(String providerCode) {
    return login(scope)
      .flatMap(token -> companyMembers(token, userOid()))
      .map(me -> {
        Set<String> things = me
          .data()
          .companies()
          .stream()
          .flatMap(vc -> vc.codespaces().stream())
          .collect(Collectors.toSet());
        return things.contains(providerCode);
      })
      .orElse(false);
  }

  /**
   * @return <code>oid</code> of the current user. May return <code>null</code> if no active user token is within the
   * scope of call.
   */
  private String userOid() {
    return getToken().map(jwt -> jwt.getClaimAsString("oid")).orElse(null);
  }

  /**
   * Logs in the application to Entra for requested scope using the client credentials flow.
   * <p>
   * Not to be confused with logging as end user or working on behalf of end user.
   *
   * @param tokenScope Entra scope to login to.
   * @return Login attempt's result state, if any.
   * @see #userOid()
   */
  private Optional<EntraTokenResponse> login(String tokenScope) {
    AtomicReference<EntraTokenResponse> tokenContainer =
      authenticationTokens.computeIfAbsent(tokenScope, t -> new AtomicReference<>(null));
    EntraTokenResponse currentToken = tokenContainer.get();
    if (
      currentToken == null ||
      currentToken.getValidUntil().isBefore(LocalDateTime.now().minusSeconds(10))
    ) {
      try {
        FormBodyPublisher formBody = FormBodyPublisher
          .newBuilder()
          .query("client_id", clientId)
          .query("grant_type", "client_credentials")
          .query("scope", tokenScope)
          .query("client_secret", clientSecret)
          .build();
        MutableRequest request = MutableRequest.POST(
          MICROSOFT_ENTRA_LOGIN_URI.formatted(tenantId),
          formBody
        );
        HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());

        EntraTokenResponse newToken = objectMapper.readValue(
          response.body(),
          EntraTokenResponse.class
        );
        tokenContainer.compareAndSet(currentToken, newToken);
      } catch (IOException e) {
        logger.warn("I/O error during API request", e);
      } catch (InterruptedException e) {
        logger.warn(
          "Underlying thread interrupted during HTTP client action, interrupting current thread",
          e
        );
        Thread.currentThread().interrupt();
      }
    }
    return Optional.ofNullable(tokenContainer.get());
  }

  private Optional<VacoApiResponse<Me>> companyMembers(
    EntraTokenResponse token,
    String oid
  ) {
    MutableRequest request = MutableRequest
      .GET(vacoApi + "/v1/companies/members/" + oid)
      .header("Authorization", "Bearer " + token.getAccessToken())
      .header("Content-Type", "application/json");
    try {
      HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
      return Optional.of(
        objectMapper.readValue(
          response.body(),
          new TypeReference<VacoApiResponse<Me>>() {}
        )
      );
    } catch (IOException e) {
      logger.warn("I/O error during API request", e);
    } catch (InterruptedException e) {
      logger.warn(
        "Underlying thread interrupted during HTTP client action, interrupting current thread",
        e
      );
      Thread.currentThread().interrupt();
    }
    return Optional.empty();
  }
}
