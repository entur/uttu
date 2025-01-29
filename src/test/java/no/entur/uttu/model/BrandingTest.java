package no.entur.uttu.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class BrandingTest {

  @Test
  void testCheckPersistableThrowsWithEmptyName() {
    var subject = new Branding();
    subject.setName("");
    Assertions.assertThrows(IllegalArgumentException.class, subject::checkPersistable);
  }

  @Test
  void testCheckPersistableThrowsWithNullName() {
    var subject = new Branding();
    subject.setName(null);
    Assertions.assertThrows(IllegalArgumentException.class, subject::checkPersistable);
  }

  @Test
  void testCheckPersistable() {
    var subject = new Branding();
    subject.setName("test");
    Assertions.assertDoesNotThrow(subject::checkPersistable);
  }
}
