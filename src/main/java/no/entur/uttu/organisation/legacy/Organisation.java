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

package no.entur.uttu.organisation.legacy;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;
import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Organisation {
    public static final String NETEX_AUTHORITY_ID_REFEFRENCE_KEY = "netexAuthorityId";
    public static final String NETEX_OPERATOR_ID_REFEFRENCE_KEY = "netexOperatorId";
    public static final String AUTHORITY_TYPE = "authority";
    public static final String OPERATOR_TYPE = "operator";

    public static final String COMPANY_NUMBER_REFERENCE_KEY = "companyNumber";
    public String id;
    public String name;
    public String version;

    public String legalName;
    public Map<String, String> references;
    public Set<String> types;


    public OrganisationContact contact;
    public OrganisationContact customerContact;

    public String getAuthorityNetexId() {
        if (references == null) {
            return null;
        }

        return references.get(NETEX_AUTHORITY_ID_REFEFRENCE_KEY);
    }

    public String getOperatorNetexId() {
        if (references == null) {
            return null;
        }

        return references.get(NETEX_OPERATOR_ID_REFEFRENCE_KEY);
    }


    public String getCompanyNumber() {
        if (references == null) {
            return null;

        }
        return references.get(COMPANY_NUMBER_REFERENCE_KEY);
    }
}
