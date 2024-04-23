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

package no.entur.uttu.config;

import no.entur.uttu.model.Provider;
import no.entur.uttu.repository.ProviderRepository;
import org.rutebanken.helper.organisation.RoleAssignment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class ProviderAuthenticationService {

  @Autowired
  private ProviderRepository providerRepository;

  public boolean hasRoleForProvider(
    Authentication authentication,
    String role,
    String providerCode
  ) {
    if (providerCode == null) {
      return false;
    }
    Provider provider = providerRepository.getOne(providerCode);
    if (provider == null) {
      return false;
    }

    //TODO permission-store
    return true;
  }

  private boolean match(RoleAssignment roleAssignment, String role, Provider provider) {
    return (
      role.equals(roleAssignment.getRole()) &&
      provider.getCodespace().getXmlns().equals(roleAssignment.getOrganisation())
    );
  }
}
