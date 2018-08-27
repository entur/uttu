package no.entur.uttu.graphql.mappers;

import no.entur.uttu.graphql.ArgumentWrapper;
import no.entur.uttu.model.StopPointInJourneyPattern;
import no.entur.uttu.repository.FlexibleStopPlaceRepository;
import no.entur.uttu.repository.ProviderRepository;
import no.entur.uttu.repository.generic.ProviderEntityRepository;
import org.springframework.stereotype.Component;

import static no.entur.uttu.graphql.GraphQLNames.*;

@Component
public class StopPointInJourneyPatternMapper extends AbstractProviderEntityMapper<StopPointInJourneyPattern> {


    private BookingArrangementMapper bookingArrangementMapper;

    private FlexibleStopPlaceRepository flexibleStopPlaceRepository;

    private DestinationDisplayMapper destinationDisplayMapper;

    public StopPointInJourneyPatternMapper(ProviderRepository providerRepository, ProviderEntityRepository<StopPointInJourneyPattern> entityRepository, FlexibleStopPlaceRepository flexibleStopPlaceRepository,
                                                  BookingArrangementMapper bookingArrangementMapper, DestinationDisplayMapper destinationDisplayMapper) {
        super(providerRepository, entityRepository);
        this.bookingArrangementMapper = bookingArrangementMapper;
        this.flexibleStopPlaceRepository = flexibleStopPlaceRepository;
        this.destinationDisplayMapper = destinationDisplayMapper;
    }

    @Override
    protected StopPointInJourneyPattern createNewEntity(ArgumentWrapper input) {
        return new StopPointInJourneyPattern();
    }

    @Override
    protected void populateEntityFromInput(StopPointInJourneyPattern entity, ArgumentWrapper input) {
        input.applyReference(FIELD_FLEXIBLE_STOP_PLACE_REF, flexibleStopPlaceRepository, entity::setFlexibleStopPlace);

        // TODO handle order

        input.apply(FIELD_BOOKING_ARRANGEMENT, bookingArrangementMapper::map, entity::setBookingArrangement);
        input.apply(FIELD_DESTINATION_DISPLAY, destinationDisplayMapper::map, entity::setDestinationDisplay);
    }
}
