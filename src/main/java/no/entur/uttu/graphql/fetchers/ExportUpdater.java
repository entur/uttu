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

import graphql.schema.DataFetchingEnvironment;
import no.entur.uttu.export.ExportService;
import no.entur.uttu.export.messaging.MessagingService;
import no.entur.uttu.graphql.mappers.AbstractProviderEntityMapper;
import no.entur.uttu.model.job.Export;
import no.entur.uttu.repository.generic.ProviderEntityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ExportUpdater extends AbstractProviderEntityUpdater<Export> {

    @Autowired
    private ExportService exportService;

    @Autowired
    private MessagingService messagingService;

    public ExportUpdater(AbstractProviderEntityMapper<Export> mapper, ProviderEntityRepository<Export> repository) {
        super(mapper, repository);
    }


    @Override
    protected Export saveEntity(DataFetchingEnvironment env) {
        Export export = super.saveEntity(env);

        // export dataset to the blob store
        exportService.exportDataSet(export);
        // notify Marduk that a new export is available
        messagingService.notifyExport(export.getProvider().getCode().toLowerCase());

        return repository.save(export);
    }
}