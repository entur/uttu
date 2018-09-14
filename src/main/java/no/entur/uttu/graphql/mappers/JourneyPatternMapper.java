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
import no.entur.uttu.model.JourneyPattern;
import no.entur.uttu.repository.ProviderRepository;
import no.entur.uttu.repository.generic.ProviderEntityRepository;
import org.springframework.stereotype.Component;

import static no.entur.uttu.graphql.GraphQLNames.FIELD_DIRECTION_TYPE;
import static no.entur.uttu.graphql.GraphQLNames.FIELD_POINTS_IN_SEQUENCE;
import static no.entur.uttu.graphql.GraphQLNames.FIELD_SERVICE_JOURNEYS;

@Component
public class JourneyPatternMapper extends AbstractProviderEntityMapper<JourneyPattern> {


    private ServiceJourneyMapper serviceJourneyMapper;

    private StopPointInJourneyPatternMapper stopPointInJourneyPatternMapper;


    public JourneyPatternMapper(ProviderRepository providerRepository, ProviderEntityRepository<JourneyPattern> repository,
                                       ServiceJourneyMapper serviceJourneyMapper, StopPointInJourneyPatternMapper stopPointInJourneyPatternMapper) {
        super(providerRepository, repository);
        this.serviceJourneyMapper = serviceJourneyMapper;
        this.stopPointInJourneyPatternMapper = stopPointInJourneyPatternMapper;
    }

    @Override
    protected JourneyPattern createNewEntity(ArgumentWrapper input) {
        return new JourneyPattern();
    }

    @Override
    protected void populateEntityFromInput(JourneyPattern entity, ArgumentWrapper input) {
        input.apply(FIELD_DIRECTION_TYPE, entity::setDirectionType);
        input.applyList(FIELD_POINTS_IN_SEQUENCE, stopPointInJourneyPatternMapper::map, entity::setPointsInSequence);
        input.applyList(FIELD_SERVICE_JOURNEYS, serviceJourneyMapper::map, entity::setServiceJourneys);
    }


}
