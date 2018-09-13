package no.entur.uttu.stopplace;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class StopPlaceRegistry {

    private String stopPlaceRegistryUrl;

    public StopPlaceRegistry(@Value("${stopplace.registry.url:https://api-test.entur.org/stop_places/1.0/graphql}") String stopPlaceRegistryUrl) {
        this.stopPlaceRegistryUrl = stopPlaceRegistryUrl;
    }
    /**
     * Return provided quayRef if valid, else throw exception.
     */
    public String getVerifiedQuayRef(String quayRef) {
        if (quayRef == null) {
            return null;
        }

        // TODO
        return quayRef;
    }
}
