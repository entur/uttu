package no.entur.uttu.ext.fintraffic.security;

import no.entur.uttu.security.spi.UserContextService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("fintraffic")
public class FintrafficSecurityConfiguration {

  @Bean
  UserContextService userContextService(
    @Value("${uttu.ext.fintraffic.security.tenant-id}") String tenantId,
    @Value("${uttu.ext.fintraffic.security.client-id}") String clientId,
    @Value("${uttu.ext.fintraffic.security.client-secret}") String clientSecret,
    @Value("${uttu.ext.fintraffic.security.scope}") String scope,
    @Value("${uttu.ext.fintraffic.security.admin-role-id}") String adminRoleId,
    @Value("${uttu.ext.fintraffic.security.vaco-api}") String vacoApi
  ) {
    return new FintrafficUserContextService(
      tenantId,
      clientId,
      clientSecret,
      scope,
      adminRoleId,
      vacoApi
    );
  }
}
