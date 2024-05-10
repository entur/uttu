package no.entur.uttu.ext.entur.security;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithSecurityContext;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(
  factory = WithMockCustomUserSecurityContextFactory.class,
  setupBefore = TestExecutionEvent.TEST_EXECUTION
)
public @interface WithMockCustomUser {
  String preferredName();

  String[] roles();
}
