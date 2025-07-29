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

public class ValidationResult {

  private final boolean valid;
  private final String message;
  private final boolean hasWarnings;

  private ValidationResult(boolean valid, String message, boolean hasWarnings) {
    this.valid = valid;
    this.message = message;
    this.hasWarnings = hasWarnings;
  }

  public static ValidationResult valid() {
    return new ValidationResult(true, null, false);
  }

  public static ValidationResult invalid(String message) {
    return new ValidationResult(false, message, false);
  }

  public static ValidationResult withWarnings(String message) {
    return new ValidationResult(true, message, true);
  }

  public static ValidationResult withInfo(String message) {
    return new ValidationResult(true, message, false);
  }

  public boolean isValid() {
    return valid;
  }

  public String getMessage() {
    return message;
  }

  public boolean hasWarnings() {
    return hasWarnings;
  }
}
