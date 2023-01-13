package no.entur.uttu.stopplace;

import java.util.Optional;

public interface StopPlaceService {

    boolean isValidQuayRef(String quayRef);

    String getVerifiedQuayRef(String quayRef);

    Optional<StopPlace> getStopPlaceByQuayRef(String quayRef);
}
