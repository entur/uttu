package no.entur.uttu.stopplace;

public interface StopPlaceService {

    boolean isValidQuayRef(String quayRef);

    String getVerifiedQuayRef(String quayRef);

    StopPlace getStopPlaceByQuayRef(String quayRef);
}
