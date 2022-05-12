package no.entur.uttu.stubs;

import no.entur.uttu.model.FlexibleStopPlace;
import no.entur.uttu.model.StopPointInJourneyPattern;
import no.entur.uttu.repository.StopPointInJourneyPatternRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Primary
public class StopPointInJourneyPatternRepositoryStub implements StopPointInJourneyPatternRepository {
    int nextCountByFlexibleStopPlace = 0;

    @Override
    public int countByFlexibleStopPlace(FlexibleStopPlace flexibleStopPlace) {
        return nextCountByFlexibleStopPlace;
    }

    public void setNextCountByFlexibleStopPlace(int nextCountByFlexibleStopPlace) {
        this.nextCountByFlexibleStopPlace = nextCountByFlexibleStopPlace;
    }

    @Override
    public List<StopPointInJourneyPattern> findAll() {
        return null;
    }

    @Override
    public List<StopPointInJourneyPattern> findByIds(List<String> ids) {
        return null;
    }

    @Override
    public StopPointInJourneyPattern getOne(String netexId) {
        return null;
    }

    @Override
    public <S extends StopPointInJourneyPattern> S save(S entity) {
        return null;
    }

    @Override
    public StopPointInJourneyPattern delete(String netexId) {
        return null;
    }

    @Override
    public void deleteAll() {

    }


}
