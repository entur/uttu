package no.entur.uttu;

import no.entur.uttu.config.UttuSecurityConfiguration;
import no.entur.uttu.repository.generic.ProviderEntityRepositoryImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(exclude = SecurityAutoConfiguration.class)
@EnableJpaRepositories(basePackages = {"no.entur.uttu.repository"},
        repositoryBaseClass = ProviderEntityRepositoryImpl.class)
@ComponentScan(excludeFilters = {
                                        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = UttuSecurityConfiguration.class),
                                        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = App.class),
})
public class UttuTestApp {

    public static void main(String[] args) {
        SpringApplication.run(UttuTestApp.class, args);
    }
}