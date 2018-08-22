package no.entur.uttu.repository;

import no.entur.uttu.model.Provider;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;

@Component
public class ProviderRepositoryImpl extends SimpleJpaRepository<Provider, Long> implements ProviderRepository {

    private final EntityManager entityManager;

    public ProviderRepositoryImpl(EntityManager entityManager) {
        super(Provider.class, entityManager);
        this.entityManager = entityManager;
    }

}
