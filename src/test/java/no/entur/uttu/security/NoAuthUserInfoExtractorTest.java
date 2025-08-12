package no.entur.uttu.security;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class NoAuthUserInfoExtractorTest {

  @Test
  void shouldReturnLocalUserAsPreferredName() {
    NoAuthUserInfoExtractor extractor = new NoAuthUserInfoExtractor();
    assertEquals("Local User", extractor.getPreferredName());
    assertEquals("local-user", extractor.getPreferredUsername());
  }
}
