package no.entur.uttu.stopplace;

import no.entur.uttu.util.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

@Component
public class StopPlaceServiceImpl implements StopPlaceService {
    private final StopPlaceRegistry stopPlaceRegistry;

    public StopPlaceServiceImpl(@Autowired StopPlaceRegistry stopPlaceRegistry) {
        this.stopPlaceRegistry = stopPlaceRegistry;
    }

    private static final Logger logger = LoggerFactory.getLogger(StopPlaceServiceImpl.class);
    public boolean isValidQuayRef(String quayRef) {
        if (quayRef == null) {
            return false;
        }
        if (!quayRef.contains(":Quay:")) {
            return false;
        }

        try {
            StopPlace stopPlace = getStopPlaceByQuayRef(quayRef);
            return stopPlace != null && stopPlace.getId() != null;
        } catch (RestClientException e) {
            logger.warn("Error checking quay ref {}: {}", quayRef, e.getMessage());
            return false;
        }
    }

    /**
     * Return provided quayRef if valid, else throw exception.
     */
    public String getVerifiedQuayRef(String quayRef) {
        if (quayRef == null) {
            return null;
        }

        // Check that quayRef is a valid Quay id. To avoid getting hits on stop place / street / municipality whatever, as stop place registry query matches anything
        Preconditions.checkArgument(isValidQuayRef(quayRef), "%s is not a valid quayRef", quayRef);

        return quayRef;
    }

    @Override
    public StopPlace getStopPlaceByQuayRef(String quayRef) {
        return stopPlaceRegistry.getStopPlaceByQuayRef(quayRef);
    }
}
