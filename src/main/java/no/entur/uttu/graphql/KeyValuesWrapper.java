package no.entur.uttu.graphql;

import no.entur.uttu.model.Value;

import java.util.Set;

public class KeyValuesWrapper {
    private String key;
    private Set<String> values;

    public KeyValuesWrapper(String key, Value value) {
        this.key = key;
        if (value != null) {
            this.values = value.getItems();
        }
    }

    public String getKey() {
        return key;
    }

    public Set<String> getValues() {
        return values;
    }
}
