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

import no.entur.uttu.error.codederror.CodedError;
import no.entur.uttu.error.codedexception.CodedIllegalArgumentException;
import no.entur.uttu.error.codes.ErrorCodeEnumeration;
import no.entur.uttu.organisation.OrganisationRegistry;
import org.rutebanken.netex.model.ContactStructure;
import org.rutebanken.netex.model.GeneralOrganisation;
import org.rutebanken.netex.model.MultilingualString;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
public class OrganisationRegistryStub implements OrganisationRegistry {

    private List<String> validOperators = Collections.singletonList("22");

    @Override
    public Optional<GeneralOrganisation> getOrganisation(String organisationId) {
        GeneralOrganisation generalOrganisation = new GeneralOrganisation()
                .withId(organisationId)
                .withVersion("1")
                .withName(new MultilingualString().withValue("OrgName"))
                .withContactDetails(
                        new ContactStructure()
                                .withUrl("https://name.org")
                );

        return Optional.of(generalOrganisation);
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

    @Override
    public List<GeneralOrganisation> getOrganisations() {
        return null;
    }

}
