package no.entur.uttu.ext.entur.security;

import static org.mockito.Mockito.when;

import no.entur.uttu.model.Codespace;
import no.entur.uttu.model.Provider;
import no.entur.uttu.repository.ProviderRepository;
import org.entur.oauth2.JwtRoleAssignmentExtractor;
import org.entur.oauth2.user.DefaultJwtUserInfoExtractor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration
class EnturUserContextServiceTest {

  @Mock
  ProviderRepository mockProviderRepository;

  EnturUserContextService subject;

  @BeforeEach
  void setUp() {
    mockProviderRepository = Mockito.mock(ProviderRepository.class);
    subject = new EnturUserContextService(
      mockProviderRepository,
      new JwtRoleAssignmentExtractor(),
      new DefaultJwtUserInfoExtractor()
    );
  }

  @Test
  @WithMockCustomUser(preferredName = "John", roles = {})
  void testGetPreferredName() {
    Assertions.assertEquals("John", subject.getPreferredName());
  }

  @Test
  @WithMockCustomUser(preferredName = "John", roles = {})
  void testIsAdminForNonAdminReturnsFalse() {
    Assertions.assertFalse(subject.isAdmin());
  }

  @Test
  @WithMockCustomUser(
    preferredName = "John",
    roles = { "{\"r\": \"adminEditRouteData\", \"o\": \"RB\"}" }
  )
  void testIsAdminForAdminReturnsTrue() {
    Assertions.assertTrue(subject.isAdmin());
  }

  @Test
  @WithMockCustomUser(
    preferredName = "John",
    roles = { "{\"r\": \"adminEditRouteData\", \"o\": \"BAR\"}" }
  )
  void testIsAdminForAdminWithIncorrectOrgReturnsFalse() {
    Assertions.assertFalse(subject.isAdmin());
  }

  @Test
  @WithMockCustomUser(
    preferredName = "John",
    roles = { "{\"r\": \"editRouteData\", \"o\": \"FOO\"}" }
  )
  void testIsAdminForAdminWithNonAdminRoleReturnsFalse() {
    Assertions.assertFalse(subject.isAdmin());
  }

  @Test
  void testHasAccessToProviderReturnsFalseWithNullProviderCode() {
    Assertions.assertFalse(subject.hasAccessToProvider(null));
  }

  @Test
  void testHasAccessToProviderReturnsFalseWithNonExistingProviderForCode() {
    when(mockProviderRepository.getOne("foo")).thenReturn(null);
    Assertions.assertFalse(subject.hasAccessToProvider("foo"));
  }

  @Test
  @WithMockCustomUser(
    preferredName = "John",
    roles = { "{\"r\": \"adminEditRouteData\", \"o\": \"RB\"}" }
  )
  void testHasAccessToProviderReturnsTrueWithAdminUser() {
    when(mockProviderRepository.getOne("foo")).thenReturn(createProvider("foo"));
    Assertions.assertTrue(subject.hasAccessToProvider("foo"));
  }

  @Test
  @WithMockCustomUser(
    preferredName = "John",
    roles = { "{\"r\": \"adminEditRouteData\", \"o\": \"BAR\"}" }
  )
  void testHasAccessToProviderReturnsFalseWithAdminUserAndIncorrectOrg() {
    when(mockProviderRepository.getOne("foo")).thenReturn(createProvider("foo"));
    Assertions.assertFalse(subject.hasAccessToProvider("foo"));
  }

  @Test
  @WithMockCustomUser(preferredName = "John", roles = {})
  void testHasAccessToProviderReturnsFalseForNonAdminUserWithoutPermission() {
    when(mockProviderRepository.getOne("foo")).thenReturn(createProvider("foo"));
    Assertions.assertFalse(subject.hasAccessToProvider("foo"));
  }

  @Test
  @WithMockCustomUser(
    preferredName = "John",
    roles = { "{\"r\": \"editRouteData\", \"o\": \"FOO\"}" }
  )
  void testHasAccessToProviderReturnsTrueForNonAdminUserWithPermission() {
    when(mockProviderRepository.getOne("foo")).thenReturn(createProvider("foo"));
    Assertions.assertTrue(subject.hasAccessToProvider("foo"));
  }

  private Provider createProvider(String code) {
    Provider provider = new Provider();
    provider.setCode(code);

    Codespace codespace = new Codespace();
    codespace.setXmlns(code.toUpperCase());

    provider.setCodespace(codespace);
    return provider;
  }
}
