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

import static no.entur.uttu.graphql.GraphQLNames.FIELD_BOOKING_ARRANGEMENT;
import static no.entur.uttu.graphql.GraphQLNames.FIELD_DESTINATION_DISPLAY;
import static no.entur.uttu.graphql.GraphQLNames.FIELD_FLEXIBLE_STOP_PLACE_REF;
import static no.entur.uttu.graphql.GraphQLNames.FIELD_FOR_ALIGHTING;
import static no.entur.uttu.graphql.GraphQLNames.FIELD_FOR_BOARDING;
import static no.entur.uttu.graphql.GraphQLNames.FIELD_NOTICES;
import static no.entur.uttu.graphql.GraphQLNames.FIELD_QUAY_REF;

import no.entur.uttu.graphql.ArgumentWrapper;
import no.entur.uttu.model.StopPointInJourneyPattern;
import no.entur.uttu.repository.FlexibleStopPlaceRepository;
import no.entur.uttu.repository.ProviderRepository;
import no.entur.uttu.repository.generic.ProviderEntityRepository;
import no.entur.uttu.stopplace.spi.StopPlaceRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StopPointInJourneyPatternMapper
  extends AbstractProviderEntityMapper<StopPointInJourneyPattern> {

  @Autowired
  private BookingArrangementMapper bookingArrangementMapper;

  @Autowired
  private FlexibleStopPlaceRepository flexibleStopPlaceRepository;

  @Autowired
  private DestinationDisplayMapper destinationDisplayMapper;

  @Autowired
  private NoticeMapper noticeMapper;

  @Autowired
  private StopPlaceRegistry stopPlaceRegistry;

  public StopPointInJourneyPatternMapper(
    @Autowired ProviderRepository providerRepository,
    @Autowired ProviderEntityRepository<StopPointInJourneyPattern> entityRepository
  ) {
    super(providerRepository, entityRepository);
  }

  @Override
  protected StopPointInJourneyPattern createNewEntity(ArgumentWrapper input) {
    return new StopPointInJourneyPattern();
  }

  @Override
  protected void populateEntityFromInput(
    StopPointInJourneyPattern entity,
    ArgumentWrapper input
  ) {
    input.applyReference(
      FIELD_FLEXIBLE_STOP_PLACE_REF,
      flexibleStopPlaceRepository,
      entity::setFlexibleStopPlace
    );
    input.apply(FIELD_QUAY_REF, this::getVerifiedQuayRef, entity::setQuayRef);
    input.apply(
      FIELD_BOOKING_ARRANGEMENT,
      bookingArrangementMapper::map,
      entity::setBookingArrangement
    );
    input.apply(
      FIELD_DESTINATION_DISPLAY,
      destinationDisplayMapper::map,
      entity::setDestinationDisplay
    );
    input.apply(FIELD_FOR_BOARDING, entity::setForBoarding);
    input.apply(FIELD_FOR_ALIGHTING, entity::setForAlighting);
    input.applyList(FIELD_NOTICES, noticeMapper::mapList, entity::setNotices);
  }

  protected String getVerifiedQuayRef(String quayRef) {
    return stopPlaceRegistry.getStopPlaceByQuayRef(quayRef).isPresent() ? quayRef : null;
  }
}
