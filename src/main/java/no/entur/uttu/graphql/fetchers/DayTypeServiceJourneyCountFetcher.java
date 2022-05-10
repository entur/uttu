package no.entur.uttu.graphql.fetchers;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import no.entur.uttu.model.DayType;
import no.entur.uttu.repository.ServiceJourneyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DayTypeServiceJourneyCountFetcher implements DataFetcher<Long> {

    private final ServiceJourneyRepository serviceJourneyRepository;

    public DayTypeServiceJourneyCountFetcher(@Autowired ServiceJourneyRepository serviceJourneyRepository) {
        this.serviceJourneyRepository = serviceJourneyRepository;
    }

    @Override
    public Long get(DataFetchingEnvironment environment) throws Exception {
        DayType dayType = (DayType) environment.getSource();
        return serviceJourneyRepository.countByDayTypePk(dayType.getPk());
    }
}
