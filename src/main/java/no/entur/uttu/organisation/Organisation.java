package no.entur.uttu.organisation;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;
import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Organisation {
    private static final String NETEX_AUTHORITY_ID_REFEFRENCE_KEY = "netexAuthorityId";
    private static final String NETEX_OPERATOR_ID_REFEFRENCE_KEY = "netexOperatorId";
    private static final String NETEX_ID_REFEFRENCE_KEY = "netexId";
    private static final String AUTHORITY_TYPE = "Authority";
    private static final String OPERATOR_TYPE = "Operator";

    private static final String COMPANY_NUMBER_REFERENCE_KEY = "companyNumber";
    public long id;
    public String name;
    public String version;

    public String legalName;
    public Map<String, String> references;
    public Set<String> types;


    public OrganisationContact contact;
    public OrganisationContact customerContact;

    public String getAuthorityNetexId() {
        if (!isAuthority() || references == null) {
            return null;
        }

        String netexId = references.get(NETEX_AUTHORITY_ID_REFEFRENCE_KEY);
        if (netexId == null) {
            netexId = references.get(NETEX_ID_REFEFRENCE_KEY);

        }
        return netexId;
    }

    public String getOperatorNetexId() {
        if (!isOperator() || references == null) {
            return null;
        }

        String netexId = references.get(NETEX_OPERATOR_ID_REFEFRENCE_KEY);

        if (netexId == null) {
            netexId = references.get(NETEX_ID_REFEFRENCE_KEY);
            if (netexId != null && netexId.contains(":Authority:")) {
                // TODO tmp hack until operator netex refs are registered in org reg.
                netexId = netexId.replaceFirst(":Authority:", ":Operator:");
            }
        }
        return netexId;
    }


    public String getCompanyNumber() {
        if (references == null) {
            return null;
        }
        return references.get(COMPANY_NUMBER_REFERENCE_KEY);
    }

    public boolean isOperator() {
        return types != null && types.contains(OPERATOR_TYPE);
    }

    public boolean isAuthority() {
        return types != null && types.contains(AUTHORITY_TYPE);
    }


}
