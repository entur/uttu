package no.entur.uttu.graphql.mappers;

import no.entur.uttu.graphql.ArgumentWrapper;
import no.entur.uttu.model.Network;
import no.entur.uttu.organisation.OrganisationRegistry;
import no.entur.uttu.repository.ProviderRepository;
import no.entur.uttu.repository.generic.ProviderEntityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static no.entur.uttu.graphql.GraphQLNames.FIELD_AUTHORITY_REF;

@Component
public class NetworkMapper extends AbstractGroupOfEntitiesMapper<Network> {

    @Autowired
    private OrganisationRegistry organisationRegistry;

    public NetworkMapper(ProviderRepository providerRepository, ProviderEntityRepository<Network> repository) {
        super(providerRepository, repository);
    }

    @Override
    protected Network createNewEntity(ArgumentWrapper input) {
        return new Network();
    }

    @Override
    protected void populateEntityFromInput(Network entity, ArgumentWrapper input) {
        input.apply(FIELD_AUTHORITY_REF, organisationRegistry::getVerifiedAuthorityRef, entity::setAuthorityRef);
    }
}
