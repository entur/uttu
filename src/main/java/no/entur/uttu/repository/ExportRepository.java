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

package no.entur.uttu.repository;

import java.time.Instant;
import java.util.List;
import no.entur.uttu.model.job.Export;
import no.entur.uttu.repository.generic.ProviderEntityRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface ExportRepository extends ProviderEntityRepository<Export> {
  List<Export> findByCreatedAfterAndProviderCode(Instant from, String provider);

  Export findFirstByProviderCodeAndDryRunFalseOrderByCreatedDesc(String provider);

  @Query(
    "from Export export0_ where export0_.created in (select max(export1_.created) from Export export1_ where export1_.dryRun=false group by export1_.provider) order by export0_.created desc"
  )
  List<Export> getLatestExportByProviders();

  Export findByNetexIdAndProviderCode(String netexId, String provider);
}
