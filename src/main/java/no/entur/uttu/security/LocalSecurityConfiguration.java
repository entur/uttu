package no.entur.uttu.security;

import java.util.Arrays;
import java.util.List;
import no.entur.uttu.security.spi.UserContextService;
import org.rutebanken.helper.organisation.user.UserInfoExtractor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Profile("local-no-authentication")
@Component
public class LocalSecurityConfiguration {

  @Bean
  public SecurityFilterChain filterChain(
    HttpSecurity http,
    UserInfoExtractor userInfoExtractor
  ) throws Exception {
    return http
      .csrf(AbstractHttpConfigurer::disable)
      .cors(cors -> cors.configurationSource(corsConfigurationSource()))
      .addFilterBefore(new UserInfoFilter(userInfoExtractor), AuthorizationFilter.class)
      .build();
  }

  private CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.addAllowedOriginPattern("*");
    configuration.setAllowedMethods(Arrays.asList("GET", "POST"));
    configuration.setAllowedHeaders(List.of("*"));
    configuration.setExposedHeaders(List.of("*"));
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }

  @Bean
  public UserContextService userContextService() {
    return new FullAccessUserContextService();
  }

  @Bean
  public UserInfoExtractor defaultUserInfoExtractor() {
    return new NoAuthUserInfoExtractor();
  }
}
