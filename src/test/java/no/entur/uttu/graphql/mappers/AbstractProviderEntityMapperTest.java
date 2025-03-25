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

package no.entur.uttu.graphql.mappers;

import static no.entur.uttu.graphql.GraphQLNames.FIELD_ID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import no.entur.uttu.config.Context;
import no.entur.uttu.graphql.ArgumentWrapper;
import no.entur.uttu.model.Provider;
import no.entur.uttu.model.ProviderEntity;
import no.entur.uttu.repository.ProviderRepository;
import no.entur.uttu.repository.generic.ProviderEntityRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class AbstractProviderEntityMapperTest {

  private TestProviderEntityMapper mapper;
  private ProviderRepository providerRepository;
  private ProviderEntityRepository<TestProviderEntity> repository;
  private Provider provider;

  @Before
  public void setUp() {
    providerRepository = mock(ProviderRepository.class);
    repository = mock(ProviderEntityRepository.class);
    mapper = new TestProviderEntityMapper(providerRepository, repository);

    provider = new Provider();
    provider.setCode("TEST");
    when(providerRepository.getOne(anyString())).thenReturn(provider);

    // Setup context for provider code
    Context.setProvider("TEST");
  }

  @Test
  public void mapList_withNewEntities_shouldCreateAndPopulateEntities() {
    // Given
    List<Object> inputObjs = new ArrayList<>();

    // New entities without IDs
    Map<String, Object> input1 = new HashMap<>();
    input1.put("testField", "value1");
    inputObjs.add(input1);

    Map<String, Object> input2 = new HashMap<>();
    input2.put("testField", "value2");
    inputObjs.add(input2);

    // When
    List<TestProviderEntity> result = mapper.mapList(inputObjs);

    // Then
    assertEquals(2, result.size());

    // Verify entities were created and populated
    TestProviderEntity entity1 = result.get(0);
    assertNull(entity1.getNetexId()); // New entity, no ID yet
    assertEquals("value1", entity1.getTestField());
    assertEquals(provider, entity1.getProvider());

    TestProviderEntity entity2 = result.get(1);
    assertNull(entity2.getNetexId()); // New entity, no ID yet
    assertEquals("value2", entity2.getTestField());
    assertEquals(provider, entity2.getProvider());
  }

  @Test
  public void mapList_withExistingEntities_shouldUpdateEntities() {
    // Given
    List<Object> inputObjs = new ArrayList<>();

    // Existing entities with IDs
    Map<String, Object> input1 = new HashMap<>();
    input1.put(FIELD_ID, "existing-id-1");
    input1.put("testField", "updated-value1");
    inputObjs.add(input1);

    Map<String, Object> input2 = new HashMap<>();
    input2.put(FIELD_ID, "existing-id-2");
    input2.put("testField", "updated-value2");
    inputObjs.add(input2);

    // Mock existing entities
    TestProviderEntity existingEntity1 = new TestProviderEntity();
    existingEntity1.setNetexId("existing-id-1");
    existingEntity1.setTestField("original-value1");

    TestProviderEntity existingEntity2 = new TestProviderEntity();
    existingEntity2.setNetexId("existing-id-2");
    existingEntity2.setTestField("original-value2");

    when(repository.findByIds(anyList()))
      .thenReturn(Arrays.asList(existingEntity1, existingEntity2));

    // When
    List<TestProviderEntity> result = mapper.mapList(inputObjs);

    // Then
    assertEquals(2, result.size());

    // Verify entities were updated
    TestProviderEntity entity1 = result.get(0);
    assertEquals("existing-id-1", entity1.getNetexId());
    assertEquals("updated-value1", entity1.getTestField());

    TestProviderEntity entity2 = result.get(1);
    assertEquals("existing-id-2", entity2.getNetexId());
    assertEquals("updated-value2", entity2.getTestField());

    // Verify repository was called with correct IDs
    ArgumentCaptor<List<String>> idsCaptor = ArgumentCaptor.forClass(List.class);
    verify(repository).findByIds(idsCaptor.capture());
    List<String> capturedIds = idsCaptor.getValue();
    assertEquals(2, capturedIds.size());
    assertEquals("existing-id-1", capturedIds.get(0));
    assertEquals("existing-id-2", capturedIds.get(1));
  }

  @Test
  public void mapList_withMixedEntities_shouldHandleBothNewAndExisting() {
    // Given
    List<Object> inputObjs = new ArrayList<>();

    // New entity without ID
    Map<String, Object> input1 = new HashMap<>();
    input1.put("testField", "new-value");
    inputObjs.add(input1);

    // Existing entity with ID
    Map<String, Object> input2 = new HashMap<>();
    input2.put(FIELD_ID, "existing-id");
    input2.put("testField", "updated-value");
    inputObjs.add(input2);

    // Mock existing entity
    TestProviderEntity existingEntity = new TestProviderEntity();
    existingEntity.setNetexId("existing-id");
    existingEntity.setTestField("original-value");

    when(repository.findByIds(anyList())).thenReturn(Arrays.asList(existingEntity));

    // When
    List<TestProviderEntity> result = mapper.mapList(inputObjs);

    // Then
    assertEquals(2, result.size());

    // Verify new entity was created
    TestProviderEntity newEntity = result.get(0);
    assertNull(newEntity.getNetexId()); // New entity, no ID yet
    assertEquals("new-value", newEntity.getTestField());
    assertEquals(provider, newEntity.getProvider());

    // Verify existing entity was updated
    TestProviderEntity updatedEntity = result.get(1);
    assertEquals("existing-id", updatedEntity.getNetexId());
    assertEquals("updated-value", updatedEntity.getTestField());
  }

  // Test implementation of AbstractProviderEntityMapper
  private static class TestProviderEntityMapper
    extends AbstractProviderEntityMapper<TestProviderEntity> {

    public TestProviderEntityMapper(
      ProviderRepository providerRepository,
      ProviderEntityRepository<TestProviderEntity> repository
    ) {
      super(providerRepository, repository);
    }

    @Override
    protected TestProviderEntity createNewEntity(ArgumentWrapper input) {
      return new TestProviderEntity();
    }

    @Override
    protected void populateEntityFromInput(
      TestProviderEntity entity,
      ArgumentWrapper input
    ) {
      input.apply("testField", entity::setTestField);
    }
  }

  // Test implementation of ProviderEntity
  private static class TestProviderEntity extends ProviderEntity {

    private String netexId;
    private String testField;
    private Provider provider;

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
    public void setProvider(Provider provider) {
      this.provider = provider;
    }

    @Override
    public Provider getProvider() {
      return provider;
    }

    public String getTestField() {
      return testField;
    }

    public void setTestField(String testField) {
      this.testField = testField;
    }
  }
}
