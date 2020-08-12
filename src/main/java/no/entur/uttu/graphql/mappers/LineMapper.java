package no.entur.uttu.graphql.mappers;

import no.entur.uttu.graphql.ArgumentWrapper;
import no.entur.uttu.model.Line;
import no.entur.uttu.organisation.OrganisationRegistry;
import no.entur.uttu.repository.NetworkRepository;
import no.entur.uttu.repository.ProviderRepository;
import no.entur.uttu.repository.generic.ProviderEntityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static no.entur.uttu.graphql.GraphQLNames.*;
import static no.entur.uttu.graphql.GraphQLNames.FIELD_NOTICES;

@Component
public abstract class LineMapper<T extends Line> extends AbstractGroupOfEntitiesMapper<T>  {

    @Autowired
    private NetworkRepository networkRepository;

    @Autowired
    private JourneyPatternMapper journeyPatternMapper;

    @Autowired
    private NoticeMapper noticeMapper;

    @Autowired
    private OrganisationRegistry organisationRegistry;

    public LineMapper(ProviderRepository providerRepository, ProviderEntityRepository<T> repository) {
        super(providerRepository, repository);
    }

    @Override
    protected void populateEntityFromInput(T entity, ArgumentWrapper input) {
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
