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

import static no.entur.uttu.graphql.GraphQLNames.FIELD_FRONT_TEXT;

import no.entur.uttu.graphql.ArgumentWrapper;
import no.entur.uttu.model.DestinationDisplay;
import no.entur.uttu.repository.ProviderRepository;
import no.entur.uttu.repository.generic.ProviderEntityRepository;
import org.springframework.stereotype.Component;

@Component
public class DestinationDisplayMapper
  extends AbstractProviderEntityMapper<DestinationDisplay> {

  public DestinationDisplayMapper(
    ProviderRepository providerRepository,
    ProviderEntityRepository<DestinationDisplay> entityRepository
  ) {
    super(providerRepository, entityRepository);
  }

  @Override
  protected DestinationDisplay createNewEntity(ArgumentWrapper input) {
    return new DestinationDisplay();
  }

  @Override
  protected void populateEntityFromInput(
    DestinationDisplay entity,
    ArgumentWrapper input
  ) {
    input.apply(FIELD_FRONT_TEXT, entity::setFrontText);
  }
}
