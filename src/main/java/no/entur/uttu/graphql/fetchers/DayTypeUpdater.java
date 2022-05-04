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
import no.entur.uttu.graphql.mappers.AbstractProviderEntityMapper;
import no.entur.uttu.model.DayType;
import no.entur.uttu.repository.ServiceJourneyRepository;
import no.entur.uttu.repository.generic.ProviderEntityRepository;
import no.entur.uttu.util.Preconditions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("dayTypeUpdater")
@Transactional
public class DayTypeUpdater extends AbstractProviderEntityUpdater<DayType> {
    private final ServiceJourneyRepository serviceJourneyRepository;

    public DayTypeUpdater(
            AbstractProviderEntityMapper<DayType> mapper,
            ProviderEntityRepository<DayType> repository,
            @Autowired ServiceJourneyRepository serviceJourneyRepository
    ) {
        super(mapper, repository);
        this.serviceJourneyRepository = serviceJourneyRepository;
    }

    @Override
    protected DayType deleteEntity(DataFetchingEnvironment env) {
        return super.deleteEntity(env);
    }

    @Override
    protected void verifyDeleteAllowed(String id) {
        DayType dayType = repository.getOne(id);
        if (dayType != null) {
            long noOfServiceJourneys = serviceJourneyRepository.countByDayTypePk(dayType.getPk());
            Preconditions.checkArgument(noOfServiceJourneys == 0, "%s cannot be deleted as it is referenced by %s serviceJourney(s)", dayType.identity(), noOfServiceJourneys);
        }
        super.verifyDeleteAllowed(id);
    }
}
