package no.entur.uttu.stubs;

import java.util.HashMap;
import java.util.Map;
import no.entur.uttu.security.UserContextService;

public class UserContextServiceStub implements UserContextService {

  private String preferredName;
  private boolean isAdmin;
  private final Map<String, Boolean> hasAccessToProvider = new HashMap<>();

  public void setPreferredName(String preferredName) {
    this.preferredName = preferredName;
  }

  public void setAdmin(boolean admin) {
    isAdmin = admin;
  }

  public void setHasAccessToProvider(String provider, boolean hasAccessToProvider) {
    this.hasAccessToProvider.put(provider, hasAccessToProvider);
  }

  @Override
  public String getPreferredName() {
    return preferredName;
  }

  @Override
  public boolean isAdmin() {
    return isAdmin;
  }

  @Override
  public boolean hasAccessToProvider(String providerCode) {
    return hasAccessToProvider.get(providerCode) == Boolean.TRUE;
  }
}
