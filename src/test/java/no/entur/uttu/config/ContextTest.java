package no.entur.uttu.config;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.Before;
import org.junit.Test;

public class ContextTest {

  private static final String PROVIDER_ID = "providerId";
  private static final String USERNAME = "username";

  @Before
  public void setUp() {
    Context.clear();
  }

  @Test
  public void unsetUsername() {
    assertThrows(IllegalArgumentException.class, Context::getVerifiedUsername);
  }

  @Test
  public void unsetProvider() {
    assertThrows(IllegalArgumentException.class, Context::getVerifiedProviderCode);
  }

  @Test
  public void setProviderCode() {
    Context.setProvider(PROVIDER_ID);
    assertEquals(PROVIDER_ID, Context.getVerifiedProviderCode());
  }

  @Test
  public void setUsername() {
    Context.setUserName(USERNAME);
    assertEquals(USERNAME, Context.getVerifiedUsername());
  }

  @Test
  public void clearContext() {
    Context.setProvider(PROVIDER_ID);
    Context.setUserName(USERNAME);
    Context.clear();
    assertThrows(IllegalArgumentException.class, Context::getVerifiedProviderCode);
    assertThrows(IllegalArgumentException.class, Context::getVerifiedUsername);
  }
}
