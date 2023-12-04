package no.entur.uttu.config;

import java.util.Arrays;
import java.util.List;
import org.entur.oauth2.RorAuthenticationConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Authentication and authorization configuration for Uttu.
 * All requests must be authenticated except for the Swagger and Actuator endpoints.
 */
@Profile("!local & !test")
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@Component
public class UttuSecurityConfiguration {

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    return http
      .csrf(AbstractHttpConfigurer::disable)
      .authorizeHttpRequests(auth ->
        auth
          .requestMatchers(AntPathRequestMatcher.antMatcher("/services/swagger.json"))
          .permitAll()
          .requestMatchers(AntPathRequestMatcher.antMatcher("/actuator/prometheus"))
          .permitAll()
          .requestMatchers(AntPathRequestMatcher.antMatcher("/actuator/health"))
          .permitAll()
          .requestMatchers(AntPathRequestMatcher.antMatcher("/actuator/health/liveness"))
          .permitAll()
          .requestMatchers(AntPathRequestMatcher.antMatcher("/actuator/health/readiness"))
          .permitAll()
          .anyRequest()
          .authenticated()
      )
      .oauth2ResourceServer(configurer -> configurer.jwt(Customizer.withDefaults()))
      .cors(cors -> cors.configurationSource(corsConfigurationSource()))
      .build();
  }

  CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.addAllowedOriginPattern("*");
    configuration.setAllowedMethods(Arrays.asList("GET","POST"));
    configuration.setAllowedHeaders(Arrays.asList("*"));
    configuration.setExposedHeaders(Arrays.asList("*"));
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }

  @Bean
  public JwtAuthenticationConverter customJwtAuthenticationConverter() {
    return new RorAuthenticationConverter();
  }
}
