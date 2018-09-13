package no.entur.uttu.graphql.mappers;

import no.entur.uttu.graphql.ArgumentWrapper;
import no.entur.uttu.model.StopPointInJourneyPattern;
import no.entur.uttu.repository.FlexibleStopPlaceRepository;
import no.entur.uttu.repository.ProviderRepository;
import no.entur.uttu.repository.generic.ProviderEntityRepository;
import no.entur.uttu.stopplace.StopPlaceRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static no.entur.uttu.graphql.GraphQLNames.*;

@Component
public class StopPointInJourneyPatternMapper extends AbstractProviderEntityMapper<StopPointInJourneyPattern> {

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

    public StopPointInJourneyPatternMapper(@Autowired ProviderRepository providerRepository,
                                                  @Autowired ProviderEntityRepository<StopPointInJourneyPattern> entityRepository) {
        super(providerRepository, entityRepository);


    }

    @Override
    protected StopPointInJourneyPattern createNewEntity(ArgumentWrapper input) {
        return new StopPointInJourneyPattern();
    }

    @Override
    protected void populateEntityFromInput(StopPointInJourneyPattern entity, ArgumentWrapper input) {
        input.applyReference(FIELD_FLEXIBLE_STOP_PLACE_REF, flexibleStopPlaceRepository, entity::setFlexibleStopPlace);
        input.apply(FIELD_QUAY_REF, stopPlaceRegistry::getVerifiedQuayRef, entity::setQuayRef);
        input.apply(FIELD_BOOKING_ARRANGEMENT, bookingArrangementMapper::map, entity::setBookingArrangement);
        input.apply(FIELD_DESTINATION_DISPLAY, destinationDisplayMapper::map, entity::setDestinationDisplay);
        input.apply(FIELD_FOR_BOARDING, entity::setForBoarding);
        input.apply(FIELD_FOR_ALIGHTING, entity::setForAlighting);
        input.applyList(FIELD_NOTICES, noticeMapper::map, entity::setNotices);
    }
}
