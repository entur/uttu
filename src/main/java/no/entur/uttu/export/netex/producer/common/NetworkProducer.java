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

package no.entur.uttu.export.netex.producer.common;

import java.util.List;
import java.util.stream.Collectors;
import no.entur.uttu.export.netex.NetexExportContext;
import no.entur.uttu.export.netex.producer.NetexObjectFactory;
import no.entur.uttu.model.Network;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NetworkProducer {

  @Autowired
  private NetexObjectFactory objectFactory;

  @Autowired
  private OrganisationProducer organisationProducer;

  public List produce(NetexExportContext context) {
    return context.networks
      .stream()
      .map(local -> mapNetwork(local, context))
      .collect(Collectors.toList());
  }

  private org.rutebanken.netex.model.Network mapNetwork(
    Network local,
    NetexExportContext context
  ) {
    return new org.rutebanken.netex.model.Network()
      .withId(local.getNetexId())
      .withVersion(local.getNetexVersion())
      .withName(objectFactory.createMultilingualString(local.getName()))
      .withDescription(objectFactory.createMultilingualString(local.getDescription()))
      .withTransportOrganisationRef(
        objectFactory.wrapAsJAXBElement(
          organisationProducer.produceAuthorityRef(local.getAuthorityRef(), true, context)
        )
      );
  }
}
