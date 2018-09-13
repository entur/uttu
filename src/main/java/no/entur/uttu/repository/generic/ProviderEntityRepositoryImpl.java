package no.entur.uttu.repository.generic;

import com.google.common.base.Preconditions;
import no.entur.uttu.config.Context;
import no.entur.uttu.model.ProviderEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;

import javax.persistence.EntityManager;
import java.util.List;

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
        findAllQuery = "from " + entityInformation.getEntityName() + " where provider.code=:providerCode";
        findOneByNetexIdQuery = findAllQuery + " and netexId=:netexId";
    }


    @Override
    public void deleteAll() {
        entityManager.createQuery("delete " + findAllQuery).setParameter("providerCoder", Context.getVerifiedProviderCode()).executeUpdate();
    }

    @Override
    public List<T> findAll() {
        return entityManager.createQuery(findAllQuery,
                entityInformation.getJavaType()).setParameter("providerCode", Context.getVerifiedProviderCode()).getResultList();
    }

    @Override
    public T delete(String netexId) {
        T entity = getOne(netexId);

        Preconditions.checkArgument(entity != null, "%s not found", netexId);
        super.delete(entity);
        return entity;
    }

    @Override
    public T getOne(String netexId) {
        List<T> results = entityManager.createQuery(findOneByNetexIdQuery,
                entityInformation.getJavaType()).setParameter("providerCode", Context.getVerifiedProviderCode())
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
            if (!entity.getProvider().getPk().equals(Context.getVerifiedProviderCode())) {
                return null;
            }
        }
        return entity;
    }

}
