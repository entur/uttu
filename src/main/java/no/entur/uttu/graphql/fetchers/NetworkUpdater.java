package no.entur.uttu.graphql.fetchers;

import no.entur.uttu.graphql.ArgumentWrapper;
import no.entur.uttu.model.Network;
import no.entur.uttu.repository.generic.ProviderEntityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static no.entur.uttu.graphql.GraphQLNames.FIELD_AUTHORITY_REF;

@Service("networkUpdater")
@Transactional
public class NetworkUpdater extends AbstractGroupOfEntitiesUpdater<Network> {

    private static final Logger logger = LoggerFactory.getLogger(NetworkUpdater.class);


    public NetworkUpdater(@Autowired ProviderEntityRepository<Network> repository) {
        super(repository);
    }

    @Override
    protected Network createNewEntity(ArgumentWrapper input) {
        return new Network();
    }

    @Override
    protected void populateEntityFromInput(Network entity, ArgumentWrapper input) {
        super.populateEntityFromInput(entity, input);
        input.apply(FIELD_AUTHORITY_REF, entity::setAuthorityRef);

    }
}
