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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityNotFoundException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import no.entur.uttu.model.ProviderEntity;
import no.entur.uttu.repository.generic.ProviderEntityRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class ArgumentWrapperTest {

  private ArgumentWrapper argumentWrapper;
  private Map<String, Object> inputMap;
  private ProviderEntityRepository<TestProviderEntity> repository;

  @Before
  public void setUp() {
    inputMap = new HashMap<>();
    argumentWrapper = new ArgumentWrapper(inputMap);
    repository = mock(ProviderEntityRepository.class);
  }

  @Test
  public void applyList_withValidList_shouldMapAndApply() {
    // Given
    String fieldName = "testList";
    List<String> inputList = Arrays.asList("value1", "value2", "value3");
    inputMap.put(fieldName, inputList);

    Function<List<String>, List<Integer>> mapper = list ->
      list.stream().map(String::length).toList();

    Consumer<List<Integer>> consumer = mock(Consumer.class);

    // When
    argumentWrapper.applyList(fieldName, mapper, consumer);

    // Then
    ArgumentCaptor<List<Integer>> captor = ArgumentCaptor.forClass(List.class);
    verify(consumer).accept(captor.capture());

    List<Integer> result = captor.getValue();
    assertEquals(3, result.size());
    assertEquals(Integer.valueOf(6), result.get(0)); // "value1" length
    assertEquals(Integer.valueOf(6), result.get(1)); // "value2" length
    assertEquals(Integer.valueOf(6), result.get(2)); // "value3" length
  }

  @Test
  public void applyList_withEmptyList_shouldApplyEmptyList() {
    // Given
    String fieldName = "testList";
    List<String> inputList = Collections.emptyList();
    inputMap.put(fieldName, inputList);

    Function<List<String>, List<Integer>> mapper = mock(Function.class);
    Consumer<List<Integer>> consumer = mock(Consumer.class);

    // When
    argumentWrapper.applyList(fieldName, mapper, consumer);

    // Then
    verify(mapper, never()).apply(anyList());
    ArgumentCaptor<List<Integer>> captor = ArgumentCaptor.forClass(List.class);
    verify(consumer).accept(captor.capture());
    assertTrue(captor.getValue().isEmpty());
  }

  @Test
  public void applyList_withNullValue_shouldApplyEmptyList() {
    // Given
    String fieldName = "testList";
    inputMap.put(fieldName, null);

    Function<List<String>, List<Integer>> mapper = mock(Function.class);
    Consumer<List<Integer>> consumer = mock(Consumer.class);

    // When
    argumentWrapper.applyList(fieldName, mapper, consumer);

    // Then
    verify(mapper, never()).apply(anyList());
    ArgumentCaptor<List<Integer>> captor = ArgumentCaptor.forClass(List.class);
    verify(consumer).accept(captor.capture());
    assertTrue(captor.getValue().isEmpty());
  }

  @Test
  public void applyList_withMissingField_shouldNotApply() {
    // Given
    String fieldName = "testList";
    // Field not present in inputMap

    Function<List<String>, List<Integer>> mapper = mock(Function.class);
    Consumer<List<Integer>> consumer = mock(Consumer.class);

    // When
    argumentWrapper.applyList(fieldName, mapper, consumer);

    // Then
    verify(mapper, never()).apply(anyList());
    verify(consumer, never()).accept(anyList());
  }

  @Test(expected = RuntimeException.class)
  public void applyList_withNonCollectionValue_shouldThrowException() {
    // Given
    String fieldName = "testList";
    inputMap.put(fieldName, "not a collection");

    Function<List<String>, List<Integer>> mapper = mock(Function.class);
    Consumer<List<Integer>> consumer = mock(Consumer.class);

    // When
    argumentWrapper.applyList(fieldName, mapper, consumer);
    // Then: exception expected
  }

  @Test
  public void applyReferenceList_withValidReferences_shouldResolveAndApply() {
    // Given
    String fieldName = "testRefList";
    List<String> references = Arrays.asList("ref1", "ref2", "ref3");
    inputMap.put(fieldName, references);

    TestProviderEntity entity1 = new TestProviderEntity("ref1");
    TestProviderEntity entity2 = new TestProviderEntity("ref2");
    TestProviderEntity entity3 = new TestProviderEntity("ref3");
    List<TestProviderEntity> entities = Arrays.asList(entity1, entity2, entity3);

    when(repository.findByIds(anyList())).thenReturn(entities);

    Consumer<List<TestProviderEntity>> consumer = mock(Consumer.class);

    // When
    argumentWrapper.applyReferenceList(fieldName, repository, consumer);

    // Then
    ArgumentCaptor<List<TestProviderEntity>> captor = ArgumentCaptor.forClass(List.class);
    verify(consumer).accept(captor.capture());

    List<TestProviderEntity> result = captor.getValue();
    assertEquals(3, result.size());
    assertEquals("ref1", result.get(0).getNetexId());
    assertEquals("ref2", result.get(1).getNetexId());
    assertEquals("ref3", result.get(2).getNetexId());
  }

  @Test
  public void applyReferenceList_withEmptyList_shouldApplyEmptyList() {
    // Given
    String fieldName = "testRefList";
    List<String> references = Collections.emptyList();
    inputMap.put(fieldName, references);

    Consumer<List<TestProviderEntity>> consumer = mock(Consumer.class);

    // When
    argumentWrapper.applyReferenceList(fieldName, repository, consumer);

    // Then
    verify(repository, never()).findByIds(anyList());
    ArgumentCaptor<List<TestProviderEntity>> captor = ArgumentCaptor.forClass(List.class);
    verify(consumer).accept(captor.capture());
    assertTrue(captor.getValue().isEmpty());
  }

  @Test
  public void applyReferenceList_withNullValue_shouldApplyEmptyList() {
    // Given
    String fieldName = "testRefList";
    inputMap.put(fieldName, null);

    Consumer<List<TestProviderEntity>> consumer = mock(Consumer.class);

    // When
    argumentWrapper.applyReferenceList(fieldName, repository, consumer);

    // Then
    verify(repository, never()).findByIds(anyList());
    ArgumentCaptor<List<TestProviderEntity>> captor = ArgumentCaptor.forClass(List.class);
    verify(consumer).accept(captor.capture());
    assertTrue(captor.getValue().isEmpty());
  }

  @Test
  public void applyReferenceList_withMissingField_shouldNotApply() {
    // Given
    String fieldName = "testRefList";
    // Field not present in inputMap

    Consumer<List<TestProviderEntity>> consumer = mock(Consumer.class);

    // When
    argumentWrapper.applyReferenceList(fieldName, repository, consumer);

    // Then
    verify(repository, never()).findByIds(anyList());
    verify(consumer, never()).accept(anyList());
  }

  @Test(expected = EntityNotFoundException.class)
  public void applyReferenceList_withMissingEntities_shouldThrowException() {
    // Given
    String fieldName = "testRefList";
    List<String> references = Arrays.asList("ref1", "ref2", "ref3");
    inputMap.put(fieldName, references);

    // Only return 2 entities when 3 are requested
    TestProviderEntity entity1 = new TestProviderEntity("ref1");
    TestProviderEntity entity2 = new TestProviderEntity("ref2");
    List<TestProviderEntity> entities = Arrays.asList(entity1, entity2);

    when(repository.findByIds(anyList())).thenReturn(entities);

    Consumer<List<TestProviderEntity>> consumer = mock(Consumer.class);

    // When
    argumentWrapper.applyReferenceList(fieldName, repository, consumer);
    // Then: exception expected
  }

  @Test(expected = RuntimeException.class)
  public void applyReferenceList_withNonCollectionValue_shouldThrowException() {
    // Given
    String fieldName = "testRefList";
    inputMap.put(fieldName, "not a collection");

    Consumer<List<TestProviderEntity>> consumer = mock(Consumer.class);

    // When
    argumentWrapper.applyReferenceList(fieldName, repository, consumer);
    // Then: exception expected
  }

  // Helper class for testing
  private static class TestProviderEntity extends ProviderEntity {

    private String netexId;

    public TestProviderEntity(String netexId) {
      this.netexId = netexId;
    }

    @Override
    public String getNetexId() {
      return netexId;
    }

    @Override
    public void setNetexId(String netexId) {
      this.netexId = netexId;
    }

    @Override
    public String getId() {
      return getNetexId();
    }

    @Override
    public void setProvider(no.entur.uttu.model.Provider provider) {
      // Not needed for tests
    }

    @Override
    public no.entur.uttu.model.Provider getProvider() {
      return null; // Not needed for tests
    }
  }
}
