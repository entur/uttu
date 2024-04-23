package no.entur.uttu.ext.entur.security.permissionstore;

import java.util.Collection;

public interface PermissionStoreClient {
  /**
   * Return the permissions for a given user.
   */
  Collection<PermissionStorePermission> getPermissions(
    String subject,
    String authority,
    int application
  );

  /**
   * Return the application id for a given application name.
   */
  int getApplicationId(PermissionStoreApplication permissionStoreApplication);
}
