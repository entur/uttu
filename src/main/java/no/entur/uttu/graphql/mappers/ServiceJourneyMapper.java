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
import no.entur.uttu.model.ServiceJourney;
import no.entur.uttu.organisation.OrganisationRegistry;
import no.entur.uttu.repository.ProviderRepository;
import no.entur.uttu.repository.generic.ProviderEntityRepository;
import org.springframework.stereotype.Component;

import static no.entur.uttu.graphql.GraphQLNames.*;

@Component
public class ServiceJourneyMapper extends AbstractGroupOfEntitiesMapper<ServiceJourney> {


    private BookingArrangementMapper bookingArrangementMapper;

    private TimetabledPassingTimeMapper timetabledPassingTimeMapper;

    private NoticeMapper noticeMapper;

    private OrganisationRegistry organisationRegistry;

    private ProviderEntityRepository<DayType> dayTypeRepository;

    public ServiceJourneyMapper(ProviderRepository providerRepository, ProviderEntityRepository<ServiceJourney> repository,
                                       BookingArrangementMapper bookingArrangementMapper,
                                       TimetabledPassingTimeMapper timetabledPassingTimeMapper, NoticeMapper noticeMapper,
                                        OrganisationRegistry organisationRegistry, ProviderEntityRepository<DayType> dayTypeRepository) {
        super(providerRepository, repository);
        this.organisationRegistry = organisationRegistry;
        this.dayTypeRepository = dayTypeRepository;
        this.bookingArrangementMapper = bookingArrangementMapper;
        this.timetabledPassingTimeMapper = timetabledPassingTimeMapper;
        this.noticeMapper = noticeMapper;
    }

    @Override
    protected ServiceJourney createNewEntity(ArgumentWrapper input) {
        return new ServiceJourney();
    }

    @Override
    protected void populateEntityFromInput(ServiceJourney entity, ArgumentWrapper input) {
        input.apply(FIELD_PUBLIC_CODE, entity::setPublicCode);
        input.apply(FIELD_OPERATOR_REF, organisationRegistry::getVerifiedOperatorRef, entity::setOperatorRef);
        input.apply(FIELD_BOOKING_ARRANGEMENT, bookingArrangementMapper::map, entity::setBookingArrangement);
        input.applyList(FIELD_PASSING_TIMES, timetabledPassingTimeMapper::map, entity::setPassingTimes);
        input.applyList(FIELD_DAY_TYPES_REFS, dayTypeRepository::getOne, entity::updateDayTypes);
        input.applyList(FIELD_NOTICES, noticeMapper::map, entity::setNotices);
    }
}
