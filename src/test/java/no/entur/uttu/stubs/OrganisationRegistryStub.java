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

import com.google.common.collect.Sets;
import no.entur.uttu.organisation.Organisation;
import no.entur.uttu.organisation.OrganisationRegistry;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
public class OrganisationRegistryStub implements OrganisationRegistry {

    @Override
    public Organisation getOrganisation(String organisationId) {
        Organisation organisation = new Organisation();
        organisation.id = organisationId;

        organisation.types = Sets.newHashSet(Organisation.AUTHORITY_TYPE, Organisation.OPERATOR_TYPE);

        organisation.references = new HashMap<>();
        organisation.references.put(Organisation.NETEX_AUTHORITY_ID_REFEFRENCE_KEY, "TST:Authority:TstAuth");
        organisation.references.put(Organisation.NETEX_OPERATOR_ID_REFEFRENCE_KEY, "TST:Operator:TstOper");

        organisation.name="OrgName";
        organisation.version="1";
        return organisation;
    }

    @Override
    public String getVerifiedOperatorRef(String operatorRef) {
        return operatorRef;
    }

    @Override
    public String getVerifiedAuthorityRef(String authorityRef) {
        return authorityRef;
    }
}
