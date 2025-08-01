package no.entur.uttu.config;

import javax.annotation.Nullable;
import no.entur.uttu.security.UserInfoFilter;
import no.entur.uttu.security.spi.UserContextService;
import no.entur.uttu.stubs.UserContextServiceStub;
import org.rutebanken.helper.organisation.user.UserInfoExtractor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@Profile("test")
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration {

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    return http
      .csrf(AbstractHttpConfigurer::disable)
      .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
      .formLogin(Customizer.withDefaults())
      .httpBasic(Customizer.withDefaults())
      .addFilterAfter(
        new UserInfoFilter(userInfoExtractor()),
        UsernamePasswordAuthenticationFilter.class
      )
      .build();
  }

  private UserInfoExtractor userInfoExtractor() {
    return new UserInfoExtractor() {
      @Override
      public @Nullable String getPreferredName() {
        return null;
      }

      @Override
      public @Nullable String getPreferredUsername() {
        return null;
      }
    };
  }

  @Bean
  public InMemoryUserDetailsManager inMemoryUserDetailsManager() {
    PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
    InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();
    manager.createUser(
      User
        .withUsername("admin")
        .password("topsecret")
        .passwordEncoder(encoder::encode)
        .roles("adminEditRouteData")
        .build()
    );
    manager.createUser(
      User
        .withUsername("user")
        .password("secret")
        .passwordEncoder(encoder::encode)
        .roles("USER")
        .build()
    );
    return manager;
  }

  @Bean
  UserContextServiceStub userContextServiceStub() {
    return new UserContextServiceStub();
  }

  @Bean
  public UserContextService userContextService(
    UserContextServiceStub userContextServiceStub
  ) {
    return userContextServiceStub;
  }
}
