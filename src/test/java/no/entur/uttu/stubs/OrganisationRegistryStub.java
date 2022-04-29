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
import no.entur.uttu.error.codederror.CodedError;
import no.entur.uttu.organisation.Organisation;
import no.entur.uttu.organisation.OrganisationContact;
import no.entur.uttu.organisation.OrganisationRegistry;
import no.entur.uttu.error.codedexception.CodedIllegalArgumentException;
import no.entur.uttu.error.codes.ErrorCodeEnumeration;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

@Component
public class OrganisationRegistryStub implements OrganisationRegistry {

    private List<String> validOperators = Collections.singletonList("22");

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

        organisation.contact = new OrganisationContact();
        organisation.contact.url = "https://name.org";

        return organisation;
    }

    @Override
    public List<Organisation> getOrganisations() {
        return null;
    }

    @Override
    public String getVerifiedOperatorRef(String operatorRef) {
        if (!validOperators.contains(operatorRef)) {
            throw new CodedIllegalArgumentException("", CodedError.fromErrorCode(ErrorCodeEnumeration.ORGANISATION_NOT_VALID_OPERATOR));
        }
        return operatorRef;
    }

    @Override
    public String getVerifiedAuthorityRef(String authorityRef) {
        return authorityRef;
    }
}
