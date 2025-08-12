package no.entur.uttu.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import java.io.IOException;
import no.entur.uttu.config.Context;
import org.junit.jupiter.api.Test;
import org.rutebanken.helper.organisation.user.UserInfoExtractor;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class UserInfoFilterTest {

  @Test
  void shouldSetUsernameInContextDuringRequest() throws ServletException, IOException {
    NoAuthUserInfoExtractor extractor = new NoAuthUserInfoExtractor();
    UserInfoFilter filter = new UserInfoFilter(extractor);
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();

    final String[] capturedUsername = new String[1];

    MockFilterChain filterChain = new MockFilterChain() {
      @Override
      public void doFilter(ServletRequest request, ServletResponse response) {
        capturedUsername[0] = Context.getVerifiedUsername();
      }
    };

    filter.doFilterInternal(request, response, filterChain);

    assertEquals("local-user", capturedUsername[0]);
    assertNull(Context.getProvider());
  }

  @Test
  void shouldHandleNullUsernameFromExtractor() throws ServletException, IOException {
    UserInfoExtractor nullExtractor = new UserInfoExtractor() {
      @Override
      public String getPreferredName() {
        return null;
      }

      @Override
      public String getPreferredUsername() {
        return null;
      }
    };

    UserInfoFilter filter = new UserInfoFilter(nullExtractor);
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();

    final String[] capturedUsername = new String[1];
    MockFilterChain filterChain = new MockFilterChain() {
      @Override
      public void doFilter(ServletRequest request, ServletResponse response) {
        capturedUsername[0] = Context.getVerifiedUsername();
      }
    };

    filter.doFilterInternal(request, response, filterChain);

    assertEquals("unknown", capturedUsername[0]);
  }
}
