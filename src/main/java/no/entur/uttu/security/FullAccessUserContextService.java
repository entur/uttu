package no.entur.uttu.security;

import no.entur.uttu.security.spi.UserContextService;

/**
 * Fallback implementation giving full access to all operations for authenticated users,
 * enable by setting property uttu.security.user-context-service=full-access
 */
public class FullAccessUserContextService implements UserContextService {

  @Override
  public String getPreferredName() {
    return "Local User";
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
