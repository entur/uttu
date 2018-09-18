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

package no.entur.uttu.stubs;

import no.entur.uttu.organisation.Organisation;
import no.entur.uttu.organisation.OrganisationRegistry;
import org.springframework.stereotype.Component;

@Component
public class OrganisationRegistryStub implements OrganisationRegistry {

    @Override
    public Organisation getOrganisation(Long organisationId) {
        Organisation organisation=new Organisation();
        organisation.id=organisationId;

        // TODO need to diff between auth and operator?
        return organisation;
    }

    @Override
    public Long getVerifiedOperatorRef(Long operatorRef) {
        return operatorRef;
    }

    @Override
    public Long getVerifiedAuthorityRef(Long authorityRef) {
        return authorityRef;
    }
}
