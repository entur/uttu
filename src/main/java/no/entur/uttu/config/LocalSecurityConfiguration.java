package no.entur.uttu.config;

import org.entur.oauth2.JwtRoleAssignmentExtractor;
import org.rutebanken.helper.organisation.RoleAssignmentExtractor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@Profile("local")
public class LocalSecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Bean
    public RoleAssignmentExtractor roleAssignmentExtractor() {
        return new JwtRoleAssignmentExtractor();
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.cors(withDefaults())
                .csrf().disable()
                .authorizeRequests()
                .anyRequest().permitAll();
    }
}
