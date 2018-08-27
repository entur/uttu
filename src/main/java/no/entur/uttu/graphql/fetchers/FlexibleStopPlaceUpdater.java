package no.entur.uttu.graphql.fetchers;

import no.entur.uttu.graphql.mappers.AbstractProviderEntityMapper;
import no.entur.uttu.model.FlexibleStopPlace;
import no.entur.uttu.repository.generic.ProviderEntityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("flexibleStopPlaceUpdater")
@Transactional
public class FlexibleStopPlaceUpdater extends AbstractProviderEntityUpdater<FlexibleStopPlace> {

    public FlexibleStopPlaceUpdater(AbstractProviderEntityMapper<FlexibleStopPlace> mapper, ProviderEntityRepository<FlexibleStopPlace> repository) {
        super(mapper, repository);
    }
}

