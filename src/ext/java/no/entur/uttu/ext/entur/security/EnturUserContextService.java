package no.entur.uttu.ext.entur.security;

import no.entur.uttu.model.Provider;
import no.entur.uttu.repository.ProviderRepository;
import no.entur.uttu.security.spi.UserContextService;
import org.rutebanken.helper.organisation.RoleAssignmentExtractor;
import org.rutebanken.helper.organisation.authorization.AuthorizationService;
import org.rutebanken.helper.organisation.authorization.DefaultAuthorizationService;
import org.rutebanken.helper.organisation.user.UserInfoExtractor;

public class EnturUserContextService implements UserContextService {

  private final ProviderRepository providerRepository;
  private final AuthorizationService<String> authorizationService;
  private final UserInfoExtractor userInfoExtractor;

  public EnturUserContextService(
    ProviderRepository providerRepository,
    RoleAssignmentExtractor roleAssignmentExtractor,
    UserInfoExtractor userInfoExtractor
  ) {
    this.providerRepository = providerRepository;
    this.userInfoExtractor = userInfoExtractor;
    authorizationService = new DefaultAuthorizationService<>(
      this::getProviderCodespaceByProviderCode,
      roleAssignmentExtractor
    );
  }

  @Override
  public String getPreferredName() {
    return userInfoExtractor.getPreferredName();
  }

  @Override
  public boolean isAdmin() {
    return authorizationService.isRouteDataAdmin();
  }

  @Override
  public boolean hasAccessToProvider(String providerCode) {
    return authorizationService.canEditRouteData(providerCode);
  }

  private String getProviderCodespaceByProviderCode(String providerCode) {
    Provider provider = providerRepository.getOne(providerCode);
    return provider == null ? null : provider.getCodespace().getXmlns();
  }
}
