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

package no.entur.uttu.graphql.fetchers;

import graphql.schema.DataFetchingEnvironment;
import no.entur.uttu.error.codederror.CodedError;
import no.entur.uttu.error.codederror.EntityHasReferencesCodedError;
import no.entur.uttu.error.codederror.FlexibleAreaValidationCodedError;
import no.entur.uttu.graphql.mappers.AbstractProviderEntityMapper;
import no.entur.uttu.model.FlexibleStopPlace;
import no.entur.uttu.model.ValidationResult;
import no.entur.uttu.repository.StopPointInJourneyPatternRepository;
import no.entur.uttu.repository.generic.ProviderEntityRepository;
import no.entur.uttu.service.FlexibleAreaValidationService;
import no.entur.uttu.util.Preconditions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("flexibleStopPlaceUpdater")
@Transactional
public class FlexibleStopPlaceUpdater
  extends AbstractProviderEntityUpdater<FlexibleStopPlace> {

  @Autowired
  private StopPointInJourneyPatternRepository stopPointInJourneyPatternRepository;

  @Autowired
  private FlexibleAreaValidationService flexibleAreaValidationService;

  public FlexibleStopPlaceUpdater(
    AbstractProviderEntityMapper<FlexibleStopPlace> mapper,
    ProviderEntityRepository<FlexibleStopPlace> repository
  ) {
    super(mapper, repository);
  }

  @Override
  protected void verifyDeleteAllowed(String id) {
    FlexibleStopPlace entity = repository.getOne(id);
    if (entity != null) {
      int noOfLines = stopPointInJourneyPatternRepository.countByFlexibleStopPlace(
        entity
      );
      CodedError error = EntityHasReferencesCodedError.fromNumberOfReferences(noOfLines);
      Preconditions.checkArgument(
        noOfLines == 0,
        error,
        "%s cannot be deleted as it is referenced by %s StopPointInJourneyPatterns(s)",
        entity.identity(),
        noOfLines
      );
    }
    super.verifyDeleteAllowed(id);
  }

  @Override
  protected FlexibleStopPlace saveEntity(DataFetchingEnvironment env) {
    FlexibleStopPlace entity = mapper.map(env.getArgument("input"));

    // Validate flexible stop place before saving
    ValidationResult validationResult =
      flexibleAreaValidationService.validateFlexibleStopPlace(entity);
    Preconditions.checkArgument(
      validationResult.isValid(),
      () ->
        FlexibleAreaValidationCodedError.fromValidationMessage(
          validationResult.getMessage()
        ),
      "Flexible stop place validation failed: %s",
      validationResult.getMessage()
    );

    entity.checkPersistable();
    return repository.save(entity);
  }
}
