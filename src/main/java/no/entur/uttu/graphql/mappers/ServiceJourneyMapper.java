package no.entur.uttu.graphql.mappers;

import no.entur.uttu.graphql.ArgumentWrapper;
import no.entur.uttu.model.ServiceJourney;
import no.entur.uttu.repository.ProviderRepository;
import no.entur.uttu.repository.generic.ProviderEntityRepository;
import org.springframework.stereotype.Component;

import static no.entur.uttu.graphql.GraphQLNames.*;

@Component
public class ServiceJourneyMapper extends AbstractProviderEntityMapper<ServiceJourney> {


    private BookingArrangementMapper bookingArrangementMapper;

    private DayTypeMapper dayTypeMapper;

    private TimetabledPassingTimeMapper timetabledPassingTimeMapper;

    public ServiceJourneyMapper(ProviderRepository providerRepository, ProviderEntityRepository<ServiceJourney> repository,
                                       BookingArrangementMapper bookingArrangementMapper, DayTypeMapper dayTypeMapper,
                                       TimetabledPassingTimeMapper timetabledPassingTimeMapper) {
        super(providerRepository, repository);
        this.bookingArrangementMapper = bookingArrangementMapper;
        this.dayTypeMapper = dayTypeMapper;
        this.timetabledPassingTimeMapper = timetabledPassingTimeMapper;
    }

    @Override
    protected ServiceJourney createNewEntity(ArgumentWrapper input) {
        return new ServiceJourney();
    }

    @Override
    protected void populateEntityFromInput(ServiceJourney entity, ArgumentWrapper input) {
        input.apply(FIELD_PUBLIC_CODE, entity::setPublicCode);
        input.apply(FIELD_OPERATOR_REF, entity::setOperatorRef);
        input.apply(FIELD_BOOKING_ARRANGEMENT, bookingArrangementMapper::map, entity::setBookingArrangement);
        input.applyList(FIELD_POINTS_IN_SEQUENCE, timetabledPassingTimeMapper::map, entity::setPointsInSequence);
        input.applyList(FIELD_DAY_TYPES, dayTypeMapper::map, entity::setDayTypes);
    }


}
