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


import no.entur.uttu.export.model.AvailabilityPeriod;
import no.entur.uttu.export.model.ExportError;
import no.entur.uttu.model.DayType;
import no.entur.uttu.model.DestinationDisplay;
import no.entur.uttu.model.FlexibleStopPlace;
import no.entur.uttu.model.Network;
import no.entur.uttu.model.Notice;
import no.entur.uttu.model.Provider;
import no.entur.uttu.model.Ref;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

public class NetexExportContext {

    private AvailabilityPeriod availabilityPeriod;

    public Provider provider;

    public Instant publicationTimestamp;

    public Set<Network> networks = new HashSet<>();

    public Set<FlexibleStopPlace> flexibleStopPlaces = new HashSet<>();

    public Set<Ref> scheduledStopPointRefs = new HashSet<>();
    public Set<String> quayRefs = new HashSet<>();

    public Set<Ref> routePointRefs = new HashSet<>();

    public Set<Long> operatorRefs = new HashSet<>();

    public Set<ExportError> errors = new HashSet<>();

    public Set<Notice> notices = new HashSet<>();

    public Set<DayType> dayTypes = new HashSet<>();

    public Set<DestinationDisplay> destinationDisplays = new HashSet<>();

    private Map<String, AtomicLong> idSequences = new HashMap<>();

    public NetexExportContext(Provider provider) {
        this.provider = provider;
        this.publicationTimestamp = Instant.now();

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

    public String getFileNamePrefix() {
        return provider.getCodespace().getXmlns().toUpperCase();
    }
}
