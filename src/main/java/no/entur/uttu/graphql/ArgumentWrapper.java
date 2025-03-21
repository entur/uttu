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

package no.entur.uttu.graphql;

import jakarta.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import no.entur.uttu.model.ProviderEntity;
import no.entur.uttu.repository.generic.ProviderEntityRepository;

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
    if (map.containsKey(name)) {
      func.accept(get(name));
    }
  }

  /**
   * Extract an entity reference from input, look up entity from repository and apply to consumer function.
   * Ignore if reference field is not set in input.
   */
  public <T extends ProviderEntity> void applyReference(
    String name,
    ProviderEntityRepository<T> repository,
    Consumer<T> func
  ) {
    this.apply(name, reference -> func.accept(resolveReference(repository, reference)));
  }

  private <T extends ProviderEntity> T resolveReference(
    ProviderEntityRepository<T> repository,
    Object reference
  ) {
    if (reference == null) {
      return null;
    }
    return Optional
      .ofNullable(repository.getOne((String) reference))
      .orElseThrow(() ->
        new EntityNotFoundException("Referred entity not found: " + reference)
      );
  }

  public <T, V> void apply(String name, Function<T, V> mapper, Consumer<V> func) {
    if (map.containsKey(name)) {
      T val = get(name);
      if (val != null) {
        func.accept(mapper.apply(val));
      } else {
        func.accept(null);
      }
    }
  }

  /**
   * Applies an argument list to a mapper. Assuming that the mapper handles the list efficiently
   * this is better than applying one argument at a time
   */
  public <T, V> void applyList(
    String name,
    Function<List<T>, List<V>> mapper,
    Consumer<List<V>> func
  ) {
    if (map.containsKey(name)) {
      Object val = get(name);
      if (val == null) {
        func.accept(Collections.emptyList());
        return;
      }

      if (val instanceof Collection) {
        List<T> collection = (List<T>) val;
        if (collection.isEmpty()) {
          func.accept(Collections.emptyList());
          return;
        }
        func.accept(mapper.apply(collection));
      } else {
        throw new RuntimeException(
          "Wrong datatype, expected Collection, got: " + val.getClass()
        );
      }
    }
  }

  /**
   * Extract a list of entity references from input, look them up in batch from repository,
   * and apply to consumer function. Ignore if reference field is not set in input.
   * This is more efficient than making individual getOne calls for each reference.
   */
  public <T extends ProviderEntity> void applyReferenceList(
    String name,
    ProviderEntityRepository<T> repository,
    Consumer<List<T>> func
  ) {
    if (map.containsKey(name)) {
      Object val = get(name);
      if (val == null) {
        func.accept(Collections.emptyList());
        return;
      }

      if (val instanceof Collection) {
        Collection<String> references = (Collection<String>) val;
        if (references.isEmpty()) {
          func.accept(Collections.emptyList());
          return;
        }

        // Fetch all entities in a single batch query
        List<T> entities = repository.findByIds(new ArrayList<>(references));
        if (entities.size() != references.size()) {
          // Some references were not found
          Set<String> foundIds = entities
            .stream()
            .map(ProviderEntity::getNetexId)
            .collect(Collectors.toSet());

          for (String ref : references) {
            if (!foundIds.contains(ref)) {
              throw new EntityNotFoundException("Referred entity not found: " + ref);
            }
          }
        }

        func.accept(entities);
      } else {
        throw new RuntimeException(
          "Wrong datatype, expected Collection, got: " + val.getClass()
        );
      }
    }
  }
}
