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
import no.entur.uttu.graphql.GraphQLNames;
import no.entur.uttu.model.FlexibleLine;
import no.entur.uttu.repository.ProviderRepository;
import no.entur.uttu.repository.generic.ProviderEntityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FlexibleLineMapper extends LineMapper<FlexibleLine> {

    @Autowired
    private BookingArrangementMapper bookingArrangementMapper;

    public FlexibleLineMapper(ProviderRepository providerRepository, ProviderEntityRepository<FlexibleLine> repository) {
        super(providerRepository, repository);
    }

    @Override
    protected FlexibleLine createNewEntity(ArgumentWrapper input) {
        return new FlexibleLine();
    }

    @Override
    protected void populateEntityFromInput(FlexibleLine entity, ArgumentWrapper input) {
        super.populateEntityFromInput(entity, input);
        input.apply(GraphQLNames.FIELD_FLEXIBLE_LINE_TYPE, entity::setFlexibleLineType);
        input.apply(GraphQLNames.FIELD_BOOKING_ARRANGEMENT, bookingArrangementMapper::map, entity::setBookingArrangement);
    }
}