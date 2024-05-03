package no.entur.uttu.ext.entur.security;

import org.assertj.core.util.Arrays;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.time.Instant;
import java.util.Map;

public class WithMockCustomUserSecurityContextFactory
        implements WithSecurityContextFactory<WithMockCustomUser> {
    @Override
    public SecurityContext createSecurityContext(WithMockCustomUser annotation) {
        Map<String, Object> headers = Map.of(
            "alg", "HS256",
            "typ", "JWT"
        );

        Map<String,Object> claims = Map.of(
                "https://ror.entur.io/preferred_name", annotation.preferredName(),
                "role_assignments", Arrays.asList(annotation.roles())
        );

        Jwt jwt = new Jwt(
                "fake",
                Instant.now(),
                Instant.now().plusSeconds(60),
                headers,
                claims
        );

        Authentication auth =
                new JwtAuthenticationToken(jwt);

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        return context;
    }
}
