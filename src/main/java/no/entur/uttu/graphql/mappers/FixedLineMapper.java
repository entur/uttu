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
import no.entur.uttu.model.FixedLine;
import no.entur.uttu.organisation.OrganisationRegistry;
import no.entur.uttu.repository.NetworkRepository;
import no.entur.uttu.repository.ProviderRepository;
import no.entur.uttu.repository.generic.ProviderEntityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static no.entur.uttu.graphql.GraphQLNames.*;
import static no.entur.uttu.graphql.GraphQLNames.FIELD_NOTICES;

@Component
public class FixedLineMapper extends AbstractGroupOfEntitiesMapper<FixedLine> {

    @Autowired
    private NetworkRepository networkRepository;

    @Autowired
    private JourneyPatternMapper journeyPatternMapper;

    @Autowired
    private NoticeMapper noticeMapper;

    @Autowired
    private OrganisationRegistry organisationRegistry;

    public FixedLineMapper(ProviderRepository providerRepository, ProviderEntityRepository<FixedLine> repository,
                              NetworkRepository networkRepository,
                              NoticeMapper noticeMapper) {
        super(providerRepository, repository);
        this.networkRepository = networkRepository;
        this.noticeMapper = noticeMapper;
    }

    @Override
    protected FixedLine createNewEntity(ArgumentWrapper input) {
        return new FixedLine();
    }

    @Override
    protected void populateEntityFromInput(FixedLine entity, ArgumentWrapper input) {
        input.apply(FIELD_NAME, entity::setName);
        input.apply(FIELD_PUBLIC_CODE, entity::setPublicCode);
        input.apply(FIELD_TRANSPORT_MODE, entity::setTransportMode);
        input.apply(FIELD_TRANSPORT_SUBMODE, entity::setTransportSubmode);
        input.applyReference(FIELD_NETWORK_REF, networkRepository, entity::setNetwork);
        input.apply(FIELD_OPERATOR_REF, organisationRegistry::getVerifiedOperatorRef, entity::setOperatorRef);
        input.applyList(FIELD_JOURNEY_PATTERNS, journeyPatternMapper::map, entity::setJourneyPatterns);
        input.applyList(FIELD_NOTICES, noticeMapper::map, entity::setNotices);
    }

}
