package no.entur.uttu.graphql;

import java.util.Map;
import java.util.function.Consumer;

public class ArgumentWrapper {

    private Map<String, Object> map;

    public ArgumentWrapper(Map<String, Object> map) {
        this.map = map;
    }

    public <T> T get(String name) {
        return (T) map.get(name);
    }

    public <T> void apply(String name, Consumer<T> func) {
        T val = get(name);
        if (val != null) {
            func.accept(val);
        }
    }

}
