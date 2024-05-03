package no.entur.uttu.ext.entur.security;

import static org.rutebanken.helper.organisation.AuthorizationConstants.ROLE_ROUTE_DATA_ADMIN;
import static org.rutebanken.helper.organisation.AuthorizationConstants.ROLE_ROUTE_DATA_EDIT;

import java.util.List;
import no.entur.uttu.model.Provider;
import no.entur.uttu.repository.ProviderRepository;
import no.entur.uttu.security.UserContextService;
import org.rutebanken.helper.organisation.RoleAssignment;
import org.rutebanken.helper.organisation.RoleAssignmentExtractor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

public class EnturUserContextService implements UserContextService {

  private final ProviderRepository providerRepository;

  private final RoleAssignmentExtractor roleAssignmentExtractor;

  public EnturUserContextService(
    ProviderRepository providerRepository,
    RoleAssignmentExtractor roleAssignmentExtractor
  ) {
    this.providerRepository = providerRepository;
    this.roleAssignmentExtractor = roleAssignmentExtractor;
  }

  @Override
  public String getPreferredName() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    JwtAuthenticationToken jwtAuthenticationToken = (JwtAuthenticationToken) auth;
    Jwt jwt = (Jwt) jwtAuthenticationToken.getPrincipal();
    return jwt.getClaimAsString("https://ror.entur.io/preferred_name");
  }

  @Override
  public boolean isAdmin() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    List<RoleAssignment> roleAssignments =
      roleAssignmentExtractor.getRoleAssignmentsForUser(auth);

    return roleAssignments
      .stream()
      .anyMatch(roleAssignment ->
        roleAssignment.getRole().equals(ROLE_ROUTE_DATA_ADMIN) &&
        roleAssignment.getOrganisation().equals("RB")
      );
  }

  @Override
  public boolean hasAccessToProvider(String providerCode) {
    if (providerCode == null) {
      return false;
    }
    Provider provider = providerRepository.getOne(providerCode);
    if (provider == null) {
      return false;
    }

    Authentication auth = SecurityContextHolder.getContext().getAuthentication();

    List<RoleAssignment> roleAssignments =
      roleAssignmentExtractor.getRoleAssignmentsForUser(auth);

    return roleAssignments
      .stream()
      .anyMatch(roleAssignment ->
              (roleAssignment.getRole().equals(ROLE_ROUTE_DATA_ADMIN) &&
        roleAssignment.getOrganisation().equals("RB")) ||
        match(roleAssignment, ROLE_ROUTE_DATA_EDIT, provider)
      );
  }

  private boolean match(RoleAssignment roleAssignment, String role, Provider provider) {
    return (
      role.equals(roleAssignment.getRole()) &&
      provider.getCodespace().getXmlns().equals(roleAssignment.getOrganisation())
    );
  }
}
