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

package no.entur.uttu.export.model;

import java.time.LocalDate;

public class AvailabilityPeriod {

  private LocalDate from;

  private LocalDate to;

  public AvailabilityPeriod(LocalDate from, LocalDate to) {
    this.from = from;
    this.to = to;
  }

  public LocalDate getFrom() {
    return from;
  }

  public LocalDate getTo() {
    return to;
  }

  public AvailabilityPeriod union(AvailabilityPeriod other) {
    if (other == null) {
      return this;
    }
    LocalDate first = from.isBefore(other.from) ? from : other.from;
    LocalDate last = to.isAfter(other.to) ? to : other.to;
    return new AvailabilityPeriod(first, last);
  }
}
