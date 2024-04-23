package no.entur.uttu.ext.entur.security.permissionstore;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.rutebanken.helper.organisation.RoleAssignment;
import org.rutebanken.helper.organisation.RoleAssignmentExtractor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

public class PermissionStoreRestRoleAssignmentExtractor
  implements RoleAssignmentExtractor {

  private static final int UNINITIALIZED_ID = -1;
  private static final String ROR_OPERATION_PREFIX = "ror-";
  private static final String ROR_RESPONSIBILITY_TYPE_SCOPE = "scope";

  private static final ObjectMapper MAPPER = new ObjectMapper();

  private final PermissionStoreClient permissionStoreClient;
  private final String application;
  private final Map<String, List<RoleAssignment>> permissionCache;

  private int applicationId = UNINITIALIZED_ID;

  public PermissionStoreRestRoleAssignmentExtractor(
    PermissionStoreClient permissionStoreClient,
    String application
  ) {
    this.permissionStoreClient = permissionStoreClient;
    this.application = application;
    this.permissionCache = Collections.synchronizedMap(new HashMap<>());
  }

  @Override
  public List<RoleAssignment> getRoleAssignmentsForUser() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    return getRoleAssignmentsForUser(auth);
  }

  // 1272
  @Override
  public List<RoleAssignment> getRoleAssignmentsForUser(Authentication authentication) {
    Jwt principal = (Jwt) authentication.getPrincipal();
    return permissionCache.computeIfAbsent(
      principal.getSubject(),
      subject ->
        permissionStoreClient
          .getPermissions(subject, "partner", getApplicationId())
          .stream()
          .filter(permission -> permission.operation().startsWith(ROR_OPERATION_PREFIX))
          .filter(permission ->
            ROR_RESPONSIBILITY_TYPE_SCOPE.equals(permission.responsibilityType())
          )
          .map(permission -> parse(permission.responsibilityKey()))
          .toList()
    );
  }

  private synchronized int getApplicationId() {
    if (applicationId == UNINITIALIZED_ID) {
      applicationId =
        permissionStoreClient.getApplicationId(
          new PermissionStoreApplication(application, 0)
        );
    }
    return applicationId;
  }

  private static RoleAssignment parse(String roleAssignment) {
    try {
      return MAPPER.readValue(roleAssignment, RoleAssignment.class);
    } catch (IOException e) {
      throw new IllegalArgumentException(
        "Exception while parsing role assignments from JSON",
        e
      );
    }
  }
}
