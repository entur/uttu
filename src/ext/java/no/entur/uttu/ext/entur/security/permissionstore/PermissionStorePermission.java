package no.entur.uttu.ext.entur.security.permissionstore;

public record PermissionStorePermission(
  String operation,
  String responsibilityType,
  String responsibilityKey
) {}
