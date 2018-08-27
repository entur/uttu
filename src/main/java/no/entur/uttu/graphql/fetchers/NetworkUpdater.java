package no.entur.uttu.graphql.fetchers;

import no.entur.uttu.graphql.mappers.AbstractProviderEntityMapper;
import no.entur.uttu.model.Network;
import no.entur.uttu.repository.generic.ProviderEntityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("networkUpdater")
@Transactional
public class NetworkUpdater extends AbstractProviderEntityUpdater<Network> {
    public NetworkUpdater(AbstractProviderEntityMapper<Network> mapper, ProviderEntityRepository<Network> repository) {
        super(mapper, repository);
    }
}
