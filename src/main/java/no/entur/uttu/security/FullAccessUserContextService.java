package no.entur.uttu.security;

/**
 * Fallback implementation giving full access to all operations for authenticated users,
 * enable by setting property uttu.security.user-context-service=full-access
 */
public class FullAccessUserContextService implements UserContextService {

  @Override
  public String getPreferredName() {
    return "";
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
