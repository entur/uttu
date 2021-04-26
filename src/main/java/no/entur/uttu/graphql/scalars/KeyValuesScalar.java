package no.entur.uttu.graphql.scalars;

import no.entur.uttu.model.Value;

import java.util.Set;

public class KeyValuesScalar {
    public String key;
    public Set<String> values;

    public KeyValuesScalar(String key, Value value) {
        this.key = key;
        if (value != null) {
            this.values = value.getItems();
        }
    }
}
