package no.entur.uttu.security;

public interface UserContextService {
  String getPreferredName();
  boolean isAdmin();
  boolean hasAccessToProvider(String providerCode);
}
