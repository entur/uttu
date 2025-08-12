package no.entur.uttu.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.DefaultSecurityFilterChain;

@ExtendWith(MockitoExtension.class)
class LocalSecurityConfigurationTest {

  private LocalSecurityConfiguration config;

  @BeforeEach
  void setUp() {
    config = new LocalSecurityConfiguration();
  }

  @Test
  void shouldCreateCorrectBeanTypes() {
    var userInfoExtractor = config.defaultUserInfoExtractor();
    var userContextService = config.userContextService();

    assertNotNull(userInfoExtractor);
    assertNotNull(userContextService);
    assertInstanceOf(NoAuthUserInfoExtractor.class, userInfoExtractor);
    assertInstanceOf(FullAccessUserContextService.class, userContextService);
  }

  @Test
  void shouldProvideCorrectLocalDevelopmentDefaults() {
    var userInfoExtractor = config.defaultUserInfoExtractor();
    var userContextService = config.userContextService();

    assertEquals("local-user", userInfoExtractor.getPreferredUsername());
    assertEquals("Local User", userInfoExtractor.getPreferredName());
    assertEquals("Local User", userContextService.getPreferredName());
    assertTrue(userContextService.isAdmin());
  }

  @Test
  void shouldAllowAccessToAnyProvider() {
    var userContextService = config.userContextService();

    assertTrue(userContextService.hasAccessToProvider("FOO"));
    assertTrue(userContextService.hasAccessToProvider("test-provider"));
    assertTrue(userContextService.hasAccessToProvider(""));
  }

  @Test
  void shouldConfigureSecurityFilterChain() throws Exception {
    HttpSecurity httpSecurity = mock(HttpSecurity.class);

    when(httpSecurity.csrf(any())).thenReturn(httpSecurity);
    when(httpSecurity.cors(any())).thenReturn(httpSecurity);
    when(httpSecurity.addFilterBefore(any(), any())).thenReturn(httpSecurity);
    when(httpSecurity.build()).thenReturn(mock(DefaultSecurityFilterChain.class));

    var userInfoExtractor = config.defaultUserInfoExtractor();
    var filterChain = config.filterChain(httpSecurity, userInfoExtractor);

    assertNotNull(filterChain);
    verify(httpSecurity).csrf(any());
    verify(httpSecurity).cors(any());
    verify(httpSecurity).addFilterBefore(any(), any());
    verify(httpSecurity).build();
  }

  @Test
  void shouldThrowExceptionWhenFilterChainParametersAreNull() {
    var userInfoExtractor = config.defaultUserInfoExtractor();

    assertThrows(Exception.class, () -> config.filterChain(null, userInfoExtractor));
  }
}
