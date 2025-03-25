/*
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *   https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */

package no.entur.uttu.repository.generic;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import java.util.ArrayList;
import java.util.List;
import no.entur.uttu.config.Context;
import no.entur.uttu.model.ProviderEntity;
import no.entur.uttu.util.Preconditions;
import org.hibernate.NonUniqueResultException;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;

public class ProviderEntityRepositoryImpl<T extends ProviderEntity>
  extends SimpleJpaRepository<T, Long>
  implements ProviderEntityRepository<T> {

  private static final String PROVIDER_CODE_PARAMETER = "providerCode";
  private static final String NETEX_ID_PARAMETER = "netexId";
  private static final String NETEX_IDS_PARAMETER = "netexIds";

  private EntityManager entityManager;
  private JpaEntityInformation<T, Long> entityInformation;
  private String findAllQuery;
  private String findByIdsQuery;
  private String findOneByNetexIdQuery;

  public ProviderEntityRepositoryImpl(
    JpaEntityInformation entityInformation,
    EntityManager entityManager
  ) {
    super(entityInformation, entityManager);
    this.entityManager = entityManager;
    this.entityInformation = entityInformation;
    String entityName = entityInformation.getEntityName();
    findAllQuery = "from " + entityName + " e where e.provider.code=:providerCode";
    findByIdsQuery = findAllQuery + " and netexId in :netexIds";
    findOneByNetexIdQuery = findAllQuery + " and netexId=:netexId";
  }

  @Override
  public void deleteAll() {
    // getting errors for cascade delete with batch statement, fetching all and deleting one by on for now
    findAll().stream().forEach(this::delete);
    //        entityManager.createQuery("delete " + findAllQuery).setParameter("providerCode", Context.getVerifiedProviderCode()).executeUpdate();
  }

  @Override
  public List<T> findAll() {
    return entityManager
      .createQuery(findAllQuery, entityInformation.getJavaType())
      .setParameter(PROVIDER_CODE_PARAMETER, Context.getVerifiedProviderCode())
      .getResultList();
  }

  @Override
  public List<T> findByIds(List<String> netexIds) {
    if (netexIds == null || netexIds.isEmpty()) {
      return new ArrayList<>();
    }
    return entityManager
      .createQuery(findByIdsQuery, entityInformation.getJavaType())
      .setParameter(PROVIDER_CODE_PARAMETER, Context.getVerifiedProviderCode())
      .setParameter(NETEX_IDS_PARAMETER, netexIds)
      .getResultList();
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
    try {
      return entityManager
        .createQuery(findOneByNetexIdQuery, entityInformation.getJavaType())
        .setParameter(PROVIDER_CODE_PARAMETER, Context.getVerifiedProviderCode())
        .setParameter(NETEX_ID_PARAMETER, netexId)
        .getSingleResult();
    } catch (NoResultException | NonUniqueResultException e) {
      return null;
    }
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
