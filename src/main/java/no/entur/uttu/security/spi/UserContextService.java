package no.entur.uttu.security.spi;

/**
 *  This interface provides authorization features to the app. Can be used to
 *  extend or customize authorization features.
 */
public interface UserContextService {
  /**
   * Get the user's display name
   */
  String getPreferredName();

  /**
   * Is the user an administrator?
   */
  boolean isAdmin();

  /**
   * Whether the user has access to edit data for a provider
   * @param providerCode The (codespace) code of the provider
   * @return true if the user has access
   */
  boolean hasAccessToProvider(String providerCode);
}
