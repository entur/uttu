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

package no.entur.uttu.graphql.fetchers;

import no.entur.uttu.util.Preconditions;
import graphql.schema.DataFetchingEnvironment;
import no.entur.uttu.graphql.mappers.AbstractProviderEntityMapper;
import no.entur.uttu.model.Network;
import no.entur.uttu.repository.FlexibleLineRepository;
import no.entur.uttu.repository.generic.ProviderEntityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("networkUpdater")
@Transactional
public class NetworkUpdater extends AbstractProviderEntityUpdater<Network> {

    @Autowired
    private FlexibleLineRepository flexibleLineRepository;

    public NetworkUpdater(AbstractProviderEntityMapper<Network> mapper, ProviderEntityRepository<Network> repository) {
        super(mapper, repository);
    }


    @Override
    protected Network deleteEntity(DataFetchingEnvironment env) {
        return super.deleteEntity(env);
    }

    @Override
    protected void verifyDeleteAllowed(String id) {
        Network network = repository.getOne(id);
        if (network != null) {
            int noOfLines = flexibleLineRepository.countByNetwork(network);
            Preconditions.checkArgument(noOfLines == 0, "%s cannot be deleted as it is referenced by %s line(s)", network.identity(), noOfLines);
        }
        super.verifyDeleteAllowed(id);
    }
}
