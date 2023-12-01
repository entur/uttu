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
import javax.validation.constraints.Size;

@Entity
public class Contact extends IdentifiedEntity {

  @Size(max = 256)
  private String email;

  @Size(max = 256)
  private String phone;

  @Size(max = 256)
  private String url;

  @Size(max = 256)
  private String contactPerson;

  @Size(max = 4000)
  private String furtherDetails;

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getContactPerson() {
    return contactPerson;
  }

  public void setContactPerson(String contactPerson) {
    this.contactPerson = contactPerson;
  }

  public String getFurtherDetails() {
    return furtherDetails;
  }

  public void setFurtherDetails(String furtherDetails) {
    this.furtherDetails = furtherDetails;
  }
}
