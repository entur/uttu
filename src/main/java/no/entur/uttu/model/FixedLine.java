package no.entur.uttu.model;

import no.entur.uttu.error.codederror.CodedError;
import no.entur.uttu.util.Preconditions;

import javax.persistence.Entity;

import static no.entur.uttu.error.codes.ErrorCodeEnumeration.FLEXIBLE_STOP_PLACE_NOT_ALLOWED;

@Entity
public class FixedLine extends Line {

    @Override
    public void checkPersistable() {
        super.checkPersistable();

        this.getJourneyPatterns().forEach(this::checkPersistableStopPointInPatterns);
    }

    private void checkPersistableStopPointInPatterns(JourneyPattern jp) {
        jp.getPointsInSequence().forEach(v -> {
            Preconditions.checkArgument(
                    v.getFlexibleStopPlace() == null,
                    CodedError.fromErrorCode(FLEXIBLE_STOP_PLACE_NOT_ALLOWED),
                    "Tried to set flexible stop place on StopPointInPattern on a fixed line journey pattern: %s",
                    jp.name
            );
        });
    }
}
