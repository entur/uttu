package no.entur.uttu.security;

import no.entur.uttu.security.spi.UserContextService;
import org.rutebanken.helper.organisation.user.UserInfoExtractor;

/**
 * Fallback implementation giving full access to all operations for authenticated users,
 * enable by setting property uttu.security.user-context-service=full-access
 */
public class FullAccessUserContextService implements UserContextService {

  private final UserInfoExtractor userInfoExtractor;

  FullAccessUserContextService(UserInfoExtractor userInfoExtractor) {
    this.userInfoExtractor = userInfoExtractor;
  }

  @Override
  public String getPreferredName() {
    return userInfoExtractor.getPreferredName();
  }

  @Override
  public boolean isAdmin() {
    return true;
  }

  @Override
  public boolean hasAccessToProvider(String providerCode) {
    return true;
  }
}
