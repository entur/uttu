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

package no.entur.uttu.model;

import static no.entur.uttu.error.codes.ErrorCodeEnumeration.FLEXIBLE_STOP_PLACE_NOT_ALLOWED;

import jakarta.persistence.Entity;
import no.entur.uttu.error.codederror.CodedError;
import no.entur.uttu.util.Preconditions;

@Entity
public class FixedLine extends Line {

  private static final String NETEX_NAME = "Line";

  @Override
  public String getNetexName() {
    return NETEX_NAME;
  }

  @Override
  public void checkPersistable() {
    super.checkPersistable();

    this.getJourneyPatterns().forEach(this::checkPersistableStopPointInPatterns);
  }

  private void checkPersistableStopPointInPatterns(JourneyPattern jp) {
    jp
      .getPointsInSequence()
      .forEach(v -> {
        Preconditions.checkArgument(
          v.getFlexibleStopPlace() == null,
          CodedError.fromErrorCode(FLEXIBLE_STOP_PLACE_NOT_ALLOWED),
          "Tried to set flexible stop place on StopPointInPattern on a fixed line journey pattern: %s",
          jp.name
        );
      });
  }

  @Override
  public void accept(LineVisitor lineVisitor) {
    lineVisitor.visitFixedLine(this);
  }
}
