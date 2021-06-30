/*
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *   https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 *
 */

package no.entur.uttu.config;

import org.entur.oauth2.AudienceValidator;
import org.entur.oauth2.JwtRoleAssignmentExtractor;
import org.entur.oauth2.RorAuth0RolesClaimAdapter;
import org.rutebanken.helper.organisation.RoleAssignmentExtractor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

/**
 * Configure Spring Beans for OAuth2 resource server and OAuth2 client security.
 */
@Configuration
@Profile("!local & !test")
public class OAuth2Config {

    /**
     * Extract role assignments from a JWT token.
     *
     * @return
     */
    @Bean
    public RoleAssignmentExtractor roleAssignmentExtractor() {
        return new JwtRoleAssignmentExtractor();
    }

    /**
     * Adapt the JWT claims produced by the RoR Auth0 tenant.
     *
     * @param rorAuth0ClaimNamespace
     * @return
     */
    @Bean
    public RorAuth0RolesClaimAdapter rorAuth0RolesClaimAdapter(@Value("${uttu.oauth2.resourceserver.auth0.ror.claim.namespace}") String rorAuth0ClaimNamespace) {
        return new RorAuth0RolesClaimAdapter(rorAuth0ClaimNamespace);
    }

    /**
     * Build a @{@link JwtDecoder} for RoR Auth0 domain.
     * To ensure compatibility with the existing authorization process ({@link JwtRoleAssignmentExtractor}), a "roles"
     * claim is inserted in the token thanks to @{@link RorAuth0RolesClaimAdapter}
     *
     * @return a @{@link JwtDecoder} for Auth0.
     */
    @Bean
    @Profile("!test")
    public JwtDecoder rorAuth0JwtDecoder(OAuth2ResourceServerProperties properties,
                                         @Value("${uttu.oauth2.resourceserver.auth0.ror.jwt.audience}") String rorAuth0Audience,
                                         @Autowired RorAuth0RolesClaimAdapter rorAuth0RolesClaimAdapter) {

        String rorAuth0Issuer = properties.getJwt().getIssuerUri();
        NimbusJwtDecoder jwtDecoder = (NimbusJwtDecoder) JwtDecoders.fromIssuerLocation(rorAuth0Issuer);

        OAuth2TokenValidator<Jwt> audienceValidator = new AudienceValidator(rorAuth0Audience);
        OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(rorAuth0Issuer);
        OAuth2TokenValidator<Jwt> withAudience = new DelegatingOAuth2TokenValidator<>(withIssuer, audienceValidator);
        jwtDecoder.setJwtValidator(withAudience);
        jwtDecoder.setClaimSetConverter(rorAuth0RolesClaimAdapter);
        return jwtDecoder;
    }

}

