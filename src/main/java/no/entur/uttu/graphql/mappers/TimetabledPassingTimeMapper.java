package no.entur.uttu.graphql.mappers;

import no.entur.uttu.graphql.ArgumentWrapper;
import no.entur.uttu.model.TimetabledPassingTime;
import no.entur.uttu.repository.ProviderRepository;
import no.entur.uttu.repository.generic.ProviderEntityRepository;
import org.springframework.stereotype.Component;

import static no.entur.uttu.graphql.GraphQLNames.*;

@Component
public class TimetabledPassingTimeMapper extends AbstractProviderEntityMapper<TimetabledPassingTime> {

    public TimetabledPassingTimeMapper(ProviderRepository providerRepository, ProviderEntityRepository<TimetabledPassingTime> entityRepository) {
        super(providerRepository, entityRepository);
    }

    @Override
    protected TimetabledPassingTime createNewEntity(ArgumentWrapper input) {
        return new TimetabledPassingTime();
    }

    @Override
    protected void populateEntityFromInput(TimetabledPassingTime entity, ArgumentWrapper input) {
        input.apply(FIELD_ARRIVAL_DAY_OFFSET, entity::setArrivalDayOffset);
        input.apply(FIELD_ARRIVAL_TIME, entity::setArrivalTime);
        input.apply(FIELD_DEPARTURE_DAY_OFFSET, entity::setDepartureDayOffset);
        input.apply(FIELD_DEPARTURE_TIME, entity::setDepartureTime);
        input.apply(FIELD_LATEST_ARRIVAL_DAY_OFFSET, entity::setLatestArrivalDayOffset);
        input.apply(FIELD_LATEST_ARRIVAL_TIME, entity::setLatestArrivalTime);
        input.apply(FIELD_EARLIEST_DEPARTURE_DAY_OFFSET, entity::setEarliestDepartureDayOffset);
        input.apply(FIELD_EARLIEST_DEPARTURE_TIME, entity::setEarliestDepartureTime);
    }
}
