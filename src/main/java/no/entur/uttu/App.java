package no.entur.uttu;

import no.entur.uttu.model.Provider;
import no.entur.uttu.repository.generic.ProviderEntityRepositoryImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = {"no.entur.uttu.repository"},
        repositoryBaseClass = ProviderEntityRepositoryImpl.class)
@EntityScan(basePackageClasses = {Provider.class, Jsr310JpaConverters.class})
@EnableCaching
public class App {

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}
