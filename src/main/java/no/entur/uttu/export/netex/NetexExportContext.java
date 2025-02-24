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

package no.entur.uttu.export.netex;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import no.entur.uttu.export.model.AvailabilityPeriod;
import no.entur.uttu.export.model.ServiceLinkExportContext;
import no.entur.uttu.model.Branding;
import no.entur.uttu.model.DayType;
import no.entur.uttu.model.DestinationDisplay;
import no.entur.uttu.model.FlexibleStopPlace;
import no.entur.uttu.model.IdentifiedEntity;
import no.entur.uttu.model.Network;
import no.entur.uttu.model.Notice;
import no.entur.uttu.model.Provider;
import no.entur.uttu.model.Ref;
import no.entur.uttu.model.job.Export;
import no.entur.uttu.model.job.ExportMessage;
import no.entur.uttu.model.job.SeverityEnumeration;

public class NetexExportContext {

  private AvailabilityPeriod availabilityPeriod;

  public Provider provider;

  public Instant publicationTimestamp;

  public Set<Network> networks = new HashSet<>();

  public Set<FlexibleStopPlace> flexibleStopPlaces = new HashSet<>();

  public Set<Ref> scheduledStopPointRefs = new HashSet<>();
  public Set<String> quayRefs = new HashSet<>();

  public Set<Ref> routePointRefs = new HashSet<>();

  public Set<String> operatorRefs = new HashSet<>();

  public Set<Notice> notices = new HashSet<>();

  public Set<DayType> dayTypes = new HashSet<>();

  public Set<DestinationDisplay> destinationDisplays = new HashSet<>();

  public Set<ServiceLinkExportContext> serviceLinks = new HashSet();

  public Set<Branding> brandings = new HashSet<>();

  private Map<String, AtomicLong> idSequences = new HashMap<>();

  private Export export;

  public NetexExportContext(Export export) {
    this.publicationTimestamp = Instant.now();
    this.export = export;
    this.provider = export.getProvider();
  }

  public void updateAvailabilityPeriod(AvailabilityPeriod newPeriod) {
    availabilityPeriod = newPeriod.union(availabilityPeriod);
  }

  public AvailabilityPeriod getAvailabilityPeriod() {
    return availabilityPeriod;
  }

  public long getAndIncrementIdSequence(String entityName) {
    AtomicLong sequence = idSequences.get(entityName);
    if (sequence == null) {
      synchronized (idSequences) {
        sequence = idSequences.get(entityName);

        if (sequence == null) {
          sequence = new AtomicLong(1);
          idSequences.put(entityName, sequence);
        }
      }
    }
    return sequence.getAndIncrement();
  }

  public <I extends IdentifiedEntity> boolean isValid(I entity) {
    return entity != null;
  }

  public void addExportMessage(
    SeverityEnumeration severity,
    String message,
    Object... params
  ) {
    export.addMessage(new ExportMessage(severity, message, params));
  }

  public boolean shouldGenerateServiceLinks() {
    return export.isGenerateServiceLinks();
  }
}
