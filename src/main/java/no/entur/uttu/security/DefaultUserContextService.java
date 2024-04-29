package no.entur.uttu.security;

public class DefaultUserContextService implements UserContextService {

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
