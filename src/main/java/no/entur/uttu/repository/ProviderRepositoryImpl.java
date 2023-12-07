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

package no.entur.uttu.repository;

import jakarta.persistence.EntityManager;
import no.entur.uttu.model.Provider;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.stereotype.Component;

@Component
public class ProviderRepositoryImpl
  extends SimpleJpaRepository<Provider, Long>
  implements ProviderRepository {

  private final EntityManager entityManager;

  public ProviderRepositoryImpl(EntityManager entityManager) {
    super(Provider.class, entityManager);
    this.entityManager = entityManager;
  }

  @Override
  public Provider getOne(String code) {
    return entityManager
      .createQuery("from Provider where code=:code", Provider.class)
      .setParameter("code", code)
      .getResultList()
      .stream()
      .findFirst()
      .orElse(null);
  }
}
