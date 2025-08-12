package no.entur.uttu.security;

import org.rutebanken.helper.organisation.user.UserInfoExtractor;

public class NoAuthUserInfoExtractor implements UserInfoExtractor {

  @Override
  public String getPreferredName() {
    return "Local User";
  }

  @Override
  public String getPreferredUsername() {
    return "local-user";
  }
}
