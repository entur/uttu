package no.entur.uttu.graphql.mappers;

import no.entur.uttu.graphql.ArgumentWrapper;
import no.entur.uttu.model.JourneyPattern;
import no.entur.uttu.repository.ProviderRepository;
import no.entur.uttu.repository.generic.ProviderEntityRepository;
import org.springframework.stereotype.Component;

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

        input.applyList(FIELD_POINTS_IN_SEQUENCE, stopPointInJourneyPatternMapper::map, entity::setPointsInSequence);
        input.applyList(FIELD_SERVICE_JOURNEYS, serviceJourneyMapper::map, entity::setServiceJourneys);


    }


}
