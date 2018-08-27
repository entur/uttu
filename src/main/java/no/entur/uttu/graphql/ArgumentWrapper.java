package no.entur.uttu.graphql;

import no.entur.uttu.model.ProviderEntity;
import no.entur.uttu.repository.generic.ProviderEntityRepository;

import javax.persistence.EntityNotFoundException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ArgumentWrapper {

    private Map<String, Object> map;

    public ArgumentWrapper(Map<String, Object> map) {
        this.map = map;
    }

    public <T> T get(String name) {
        return (T) map.get(name);
    }

    /**
     * Extract field from input if exists and apply to consumer func. Ignore of field is  not set.
     */
    public <T> void apply(String name, Consumer<T> func) {
        T val = get(name);
        if (val != null) {
            func.accept(val);
        }
    }

    /**
     * Extract an entity reference from input, look up entity from repository and apply to consumer function.
     * Ignore if reference field is not set in input.
     */
    public <T extends ProviderEntity> void applyReference(String name, ProviderEntityRepository<T> repository, Consumer<T> func) {
        this.apply(name, reference -> func.accept(Optional.ofNullable(repository.getOne((String) reference)).orElseThrow(() -> new EntityNotFoundException("Referred entity not found: " + reference))));
    }

    public <T, V> void apply(String name, Function<T, V> mapper, Consumer<V> func) {
        T val = get(name);
        if (val != null) {
            func.accept(mapper.apply(val));
        }
    }

    public <T, V> void applyList(String name, Function<T, V> mapper, Consumer<List<V>> func) {
        T val = get(name);
        if (val instanceof Collection) {
            func.accept(((Collection<T>) val).stream().map(t -> mapper.apply(t)).collect(Collectors.toList()));
        }
    }

}
