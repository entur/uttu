package no.entur.uttu.security;

import javax.annotation.Nullable;
import org.rutebanken.helper.organisation.user.UserInfoExtractor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * Default UserInfoExtractor that retrieves the username from the standard OIDC claim "preferred_username".
 * The preferred named is equal to the preferred username.
 */
public class DefaultUserInfoExtractor implements UserInfoExtractor {

  private static final String CLAIM_OIDC_PREFERRED_USERNAME = "preferred_username";

  @Nullable
  @Override
  public String getPreferredName() {
    return getPreferredUsername();
  }

  @Nullable
  @Override
  public String getPreferredUsername() {
    return getClaim(CLAIM_OIDC_PREFERRED_USERNAME);
  }

  private String getClaim(String claim) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth instanceof JwtAuthenticationToken jwtAuthenticationToken) {
      Jwt jwt = (Jwt) jwtAuthenticationToken.getPrincipal();
      return jwt.getClaimAsString(claim);
    } else {
      return null;
    }
  }
}
