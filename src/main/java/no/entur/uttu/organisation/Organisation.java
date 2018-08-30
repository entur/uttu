package no.entur.uttu.organisation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Map;
import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Organisation {

    private static final String NETEX_ID_REFEFRENCE_KEY = "netexId";
    private static final String AUTHORITY_TYPE="Authority";
    private static final String OPERATOR_TYPE="Operator";
    public long id;
    public String name;
    public String version;
    public Map<String, String> references;
    public Set<String> types;

    // TODO what i org is authority and operator? what will netexId look like? should we use netexId? or create netex id from id with appropriate type?
    public String getNetexId() {
        if (references == null) {
            return null;
        }
        return references.get(NETEX_ID_REFEFRENCE_KEY);
    }

    public boolean isOperator() {
        return types!=null && types.contains(OPERATOR_TYPE);
    }

    public boolean isAuthority() {
        return types!=null && types.contains(AUTHORITY_TYPE);
    }
}
