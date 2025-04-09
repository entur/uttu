package no.entur.uttu.ext.entur.security;

import jakarta.servlet.http.HttpServletRequest;
import no.entur.uttu.repository.ProviderRepository;
import no.entur.uttu.security.spi.UserContextService;
import org.entur.oauth2.AuthorizedWebClientBuilder;
import org.entur.oauth2.JwtRoleAssignmentExtractor;
import org.entur.oauth2.RorAuthenticationConverter;
import org.entur.oauth2.multiissuer.MultiIssuerAuthenticationManagerResolverBuilder;
import org.entur.oauth2.user.JwtUserInfoExtractor;
import org.entur.ror.permission.RemoteBabaRoleAssignmentExtractor;
import org.rutebanken.helper.organisation.RoleAssignmentExtractor;
import org.rutebanken.helper.organisation.user.UserInfoExtractor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@Profile("entur")
public class EnturSecurityConfiguration {

  @Bean
  public JwtAuthenticationConverter customJwtAuthenticationConverter() {
    return new RorAuthenticationConverter();
  }

  @ConditionalOnProperty(
    value = "entur.security.role.assignment.extractor",
    havingValue = "jwt",
    matchIfMissing = true
  )
  @Bean
  public RoleAssignmentExtractor jwtRoleAssignmentExtractor() {
    return new JwtRoleAssignmentExtractor();
  }

  @ConditionalOnProperty(
    value = "entur.security.role.assignment.extractor",
    havingValue = "baba"
  )
  @Bean
  public RoleAssignmentExtractor babaRoleAssignmentExtractor(
    @Qualifier("internalWebClient") WebClient webClient,
    @Value("${entur.permission.rest.service.url}") String url
  ) {
    return new RemoteBabaRoleAssignmentExtractor(webClient, url);
  }

  /**
   * Return a WebClient for authorized API calls.
   * The WebClient inserts a JWT bearer token in the Authorization HTTP header.
   * The JWT token is obtained from the configured Authorization Server.
   *
   * @param properties The spring.security.oauth2.client.registration.* properties
   * @param audience   The API audience, required for obtaining a token from Auth0
   * @return a WebClient for authorized API calls.
   */
  @ConditionalOnProperty(
    value = "entur.security.role.assignment.extractor",
    havingValue = "baba"
  )
  @Bean("internalWebClient")
  WebClient internalWebClient(
    WebClient.Builder webClientBuilder,
    OAuth2ClientProperties properties,
    @Value("${internal.oauth2.client.audience}") String audience
  ) {
    return new AuthorizedWebClientBuilder(webClientBuilder)
      .withOAuth2ClientProperties(properties)
      .withAudience(audience)
      .withClientRegistrationId("internal")
      .build();
  }

  @Bean
  public UserInfoExtractor userInfoExtractor() {
    return new JwtUserInfoExtractor();
  }

  @Bean
  public UserContextService userContextService(
    ProviderRepository providerRepository,
    RoleAssignmentExtractor roleAssignmentExtractor,
    UserInfoExtractor userInfoExtractor
  ) {
    return new EnturUserContextService(
      providerRepository,
      roleAssignmentExtractor,
      userInfoExtractor
    );
  }

  @Bean
  public AuthenticationManagerResolver<HttpServletRequest> authenticationManagerResolver(
    @Value(
      "${uttu.oauth2.resourceserver.auth0.entur.partner.jwt.audience:}"
    ) String enturPartnerAuth0Audience,
    @Value(
      "${uttu.oauth2.resourceserver.auth0.entur.partner.jwt.issuer-uri:}"
    ) String enturPartnerAuth0Issuer,
    @Value(
      "${uttu.oauth2.resourceserver.auth0.ror.jwt.audience:}"
    ) String rorAuth0Audience,
    @Value(
      "${uttu.oauth2.resourceserver.auth0.ror.jwt.issuer-uri:}"
    ) String rorAuth0Issuer,
    @Value(
      "${uttu.oauth2.resourceserver.auth0.ror.claim.namespace:}"
    ) String rorAuth0ClaimNamespace
  ) {
    return new MultiIssuerAuthenticationManagerResolverBuilder()
      .withEnturPartnerAuth0Issuer(enturPartnerAuth0Issuer)
      .withEnturPartnerAuth0Audience(enturPartnerAuth0Audience)
      .withRorAuth0Issuer(rorAuth0Issuer)
      .withRorAuth0Audience(rorAuth0Audience)
      .withRorAuth0ClaimNamespace(rorAuth0ClaimNamespace)
      .build();
  }
}
