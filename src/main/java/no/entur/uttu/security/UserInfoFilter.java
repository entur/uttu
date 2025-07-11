package no.entur.uttu.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import no.entur.uttu.config.Context;
import org.rutebanken.helper.organisation.user.UserInfoExtractor;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Set the username in the session context after successful authentication and
 * clear the context after the response is sent.
 */
public class UserInfoFilter extends OncePerRequestFilter {

  private final UserInfoExtractor userInfoExtractor;

  public UserInfoFilter(UserInfoExtractor userInfoExtractor) {
    this.userInfoExtractor = userInfoExtractor;
  }

  @Override
  protected void doFilterInternal(
    HttpServletRequest request,
    HttpServletResponse response,
    FilterChain filterChain
  ) throws ServletException, IOException {
    try {
      String preferredUsername = userInfoExtractor.getPreferredUsername();
      Context.setUserName(preferredUsername == null ? "unknown" : preferredUsername);
      filterChain.doFilter(request, response);
    } finally {
      Context.clear();
    }
  }
}
