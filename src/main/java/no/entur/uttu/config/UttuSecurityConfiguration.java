package no.entur.uttu.config;

import static org.springframework.security.config.Customizer.withDefaults;

import java.util.Arrays;
import java.util.List;
import org.entur.oauth2.RorAuthenticationConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
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
@EnableGlobalMethodSecurity(prePostEnabled = true)
@Component
public class UttuSecurityConfiguration extends WebSecurityConfigurerAdapter {

  @Bean
  CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedHeaders(List.of("*"));
    configuration.addAllowedOrigin("*");
    configuration.setAllowedMethods(List.of("GET", "PUT", "POST", "DELETE"));
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }

  @Override
  public void configure(HttpSecurity http) throws Exception {
    http
      .cors(withDefaults())
      .csrf()
      .disable()
      .authorizeRequests()
      .antMatchers("/services/swagger.json")
      .permitAll()
      // exposed internally only, on a different port (pod-level)
      .antMatchers("/actuator/prometheus")
      .permitAll()
      .antMatchers("/actuator/health")
      .permitAll()
      .antMatchers("/actuator/health/liveness")
      .permitAll()
      .antMatchers("/actuator/health/readiness")
      .permitAll()
      .anyRequest()
      .authenticated()
      .and()
      .oauth2ResourceServer()
      .jwt()
      .jwtAuthenticationConverter(new RorAuthenticationConverter());
  }
}
