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

import jakarta.persistence.Entity;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import no.entur.uttu.error.codederror.CodedError;
import no.entur.uttu.error.codes.ErrorCodeEnumeration;
import no.entur.uttu.util.Preconditions;

@Entity
public class Notice extends ProviderEntity {

  @NotNull
  @Size(max = 4000)
  private String text;

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public Notice withText(String text) {
    this.text = text;
    return this;
  }

  @Override
  public void checkPersistable() {
    super.checkPersistable();
    Preconditions.checkArgument(
      text != null && !text.isEmpty(),
      CodedError.fromErrorCode(ErrorCodeEnumeration.NO_EMPTY_NOTICES),
      "Notice text cannot be empty"
    );
  }
}
