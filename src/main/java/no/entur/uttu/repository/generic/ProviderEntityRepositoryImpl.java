package no.entur.uttu.repository.generic;

import com.google.common.base.Preconditions;
import no.entur.uttu.config.ProviderContext;
import no.entur.uttu.graphql.fetchers.FlexibleStopPlaceUpdater;
import no.entur.uttu.model.Provider;
import no.entur.uttu.model.ProviderEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.persistence.EntityManager;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ProviderEntityRepositoryImpl<T extends ProviderEntity> extends SimpleJpaRepository<T, Long> implements ProviderEntityRepository<T> {

    private static final Logger logger = LoggerFactory.getLogger(ProviderEntityRepositoryImpl.class);

    private EntityManager entityManager;
    private JpaEntityInformation<T, Long> entityInformation;
    private String findAllQuery;
    private String findOneByNetexIdQuery;

    public ProviderEntityRepositoryImpl(JpaEntityInformation entityInformation, EntityManager entityManager) {
        super(entityInformation, entityManager);
        this.entityManager = entityManager;
        this.entityInformation = entityInformation;
        findAllQuery = "from " + entityInformation.getEntityName() + " where provider.pk=:providerId";
        findOneByNetexIdQuery = findAllQuery + " and netexId=:netexId";
    }


    @Override
    public void deleteAll() {
        super.deleteAll();
    }

    @Override
    public List<T> findAll() {
        return entityManager.createQuery(findAllQuery,
                entityInformation.getJavaType()).setParameter("providerId", getVerifiedContextProviderId()).getResultList();
    }

    @Override
    public T getOne(String netexId) {
        List<T> results = entityManager.createQuery(findOneByNetexIdQuery,
                entityInformation.getJavaType()).setParameter("providerId", getVerifiedContextProviderId())
                                  .setParameter("netexId", netexId).getResultList();
        if (results.isEmpty()) {
            return null;
        }
        return results.get(0);
    }

    @Override
    public T getOne(Long aLong) {
        T entity = super.getOne(aLong);
        if (entity != null) {
            if (!entity.getProvider().getPk().equals(getVerifiedContextProviderId())) {
                return null;
            }
        }
        return entity;
    }


    @Override
    public <S extends T> S save(S entity) {
        if (entity != null) {
            String user = getUsername();
            Instant now = Instant.now();
            entity.setChanged(now);
            entity.setCreatedBy(user);

            if (entity.getCreated() == null) {
                entity.setCreated(now);
                entity.setChangedBy(user);
            }


            Long providerId = getVerifiedContextProviderId();
            if (entity.getProvider() == null) {
                entity.setProvider(getVerifiedProvider(providerId));
            } else {
                Preconditions.checkArgument(Objects.equals(entity.getProvider().getPk(), providerId),
                        "Provider mismatch, attempting to store entity[½s] in context of provider[%s] .", entity, providerId);

            }


            if (entity.getNetexId() == null) {
                entity.setNetexId(entity.getProvider().getCodeSpace().getXmlns() + ":" + entity.getClass().getSimpleName() + ":" + UUID.randomUUID());
                logger.info("Created new entity with NeTExId:{}", entity.getNetexId());
            }
        }

        return super.save(entity);
    }

    private Long getVerifiedContextProviderId() {
        Long providerId = ProviderContext.getProvider();
        Preconditions.checkArgument(providerId != null,
                "Provider not set for session");
        return providerId;
    }

    private Provider getVerifiedProvider(Long providerId) {
        Provider provider = entityManager.find(Provider.class, providerId);
        Preconditions.checkArgument(provider != null,
                "Provider not found [pk=%s]", providerId);
        return provider;
    }

    private String getUsername() {
        String user = "unknown";
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            user = Objects.toString(auth.getPrincipal());
        }
        return user;
    }
}
