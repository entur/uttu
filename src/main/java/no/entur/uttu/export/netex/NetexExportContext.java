package no.entur.uttu.export.netex;


import no.entur.uttu.model.Provider;
import org.rutebanken.netex.model.FlexibleStopPlace;
import org.rutebanken.netex.model.Network;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class NetexExportContext {

    public Provider provider;

    public Instant publicationTimestamp;

    public Map<String, Network> networks = new HashMap<>();

    public Map<String, FlexibleStopPlace> flexibleStopPlaces = new HashMap<>();

    public NetexExportContext(Provider provider) {
        this.provider = provider;
        this.publicationTimestamp = Instant.now();

    }

}
