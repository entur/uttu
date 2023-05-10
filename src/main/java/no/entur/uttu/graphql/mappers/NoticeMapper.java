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

package no.entur.uttu.graphql.mappers;

import static no.entur.uttu.graphql.GraphQLNames.FIELD_TEXT;

import no.entur.uttu.graphql.ArgumentWrapper;
import no.entur.uttu.model.Notice;
import no.entur.uttu.repository.ProviderRepository;
import no.entur.uttu.repository.generic.ProviderEntityRepository;
import org.springframework.stereotype.Component;

@Component
public class NoticeMapper extends AbstractProviderEntityMapper<Notice> {

  public NoticeMapper(
    ProviderRepository providerRepository,
    ProviderEntityRepository<Notice> entityRepository
  ) {
    super(providerRepository, entityRepository);
  }

  @Override
  protected Notice createNewEntity(ArgumentWrapper input) {
    return new Notice();
  }

  @Override
  protected void populateEntityFromInput(Notice entity, ArgumentWrapper input) {
    input.apply(FIELD_TEXT, entity::setText);
  }
}
