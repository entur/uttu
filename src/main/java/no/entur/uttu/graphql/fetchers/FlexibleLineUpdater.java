package no.entur.uttu.graphql.fetchers;

import no.entur.uttu.graphql.mappers.AbstractProviderEntityMapper;
import no.entur.uttu.model.FlexibleLine;
import no.entur.uttu.repository.generic.ProviderEntityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("flexibleLineUpdater")
@Transactional
public class FlexibleLineUpdater extends AbstractProviderEntityUpdater<FlexibleLine> {


    public FlexibleLineUpdater(AbstractProviderEntityMapper<FlexibleLine> mapper, ProviderEntityRepository<FlexibleLine> repository) {
        super(mapper, repository);
    }
}
