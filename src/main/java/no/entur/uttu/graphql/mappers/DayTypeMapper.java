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
        input.apply(FIELD_NAME, entity::setName);
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
