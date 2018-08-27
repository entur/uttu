package no.entur.uttu.graphql.mappers;

import no.entur.uttu.graphql.ArgumentWrapper;
import no.entur.uttu.model.DayType;
import no.entur.uttu.model.DayTypeAssignment;
import no.entur.uttu.model.OperatingPeriod;
import no.entur.uttu.repository.ProviderRepository;
import no.entur.uttu.repository.generic.ProviderEntityRepository;
import org.springframework.stereotype.Component;

import java.util.Map;

import static no.entur.uttu.graphql.GraphQLNames.*;

@Component
public class DayTypeMapper extends AbstractProviderEntityMapper<DayType> {

    public DayTypeMapper(ProviderRepository providerRepository, ProviderEntityRepository<DayType> repository) {
        super(providerRepository,repository);
    }

    @Override
    protected DayType createNewEntity(ArgumentWrapper input) {
        return new DayType();
    }

    @Override
    protected void populateEntityFromInput(DayType entity, ArgumentWrapper input) {
        input.applyList(FIELD_DAY_TYPE_ASSIGNMENTS, this::mapDayTypeAssignment, entity::setDayTypeAssignments);
        input.apply(FIELD_DAYS_OF_WEEK, entity::setDaysOfWeek);
    }


    public DayTypeAssignment mapDayTypeAssignment(Map<String, Object> inputMap) {
        ArgumentWrapper input = new ArgumentWrapper(inputMap);
        DayTypeAssignment dayTypeAssignment = new DayTypeAssignment();
        input.apply(FIELD_OPERATING_PERIOD, this::mapOperatingPeriod, dayTypeAssignment::setOperatingPeriod);
        input.apply(FIELD_DATE, dayTypeAssignment::setDate);
        input.apply(FIELD_IS_AVAILABLE, dayTypeAssignment::setAvailable);
        return dayTypeAssignment;
    }

    public OperatingPeriod mapOperatingPeriod(Map<String, Object> inputMap) {
        ArgumentWrapper input = new ArgumentWrapper(inputMap);
        OperatingPeriod operatingPeriod = new OperatingPeriod();
        input.apply(FIELD_FROM_DATE, operatingPeriod::setFromDate);
        input.apply(FIELD_TO_DATE, operatingPeriod::setToDate);
        return operatingPeriod;
    }


}
