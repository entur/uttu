/*
 * Licensed under the EUPL, Version 1.2 or u2013 as soon they will be approved by
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

import static no.entur.uttu.graphql.GraphQLNames.FIELD_DESCRIPTION;
import static no.entur.uttu.graphql.GraphQLNames.FIELD_ID;
import static no.entur.uttu.graphql.GraphQLNames.FIELD_NAME;
import static no.entur.uttu.graphql.GraphQLNames.FIELD_PRIVATE_CODE;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import no.entur.uttu.config.Context;
import no.entur.uttu.model.GroupOfEntities_VersionStructure;
import no.entur.uttu.model.Provider;
import no.entur.uttu.repository.ProviderRepository;
import no.entur.uttu.repository.generic.ProviderEntityRepository;
import org.junit.Before;
import org.junit.Test;

public class AbstractGroupOfEntitiesMapperTest {

  private TestGroupOfEntitiesMapper mapper;
  private ProviderRepository providerRepository;
  private ProviderEntityRepository<TestGroupOfEntities> repository;
  private Provider provider;

  @Before
  public void setUp() {
    providerRepository = mock(ProviderRepository.class);
    repository = mock(ProviderEntityRepository.class);
    mapper = new TestGroupOfEntitiesMapper(providerRepository, repository);

    provider = new Provider();
    provider.setCode("TEST");
    when(providerRepository.getOne(anyString())).thenReturn(provider);

    Context.setProvider("TEST");
  }

  @Test
  public void mapList_shouldMapAllEntitiesWithGroupOfEntitiesFields() {
    // Given
    List<Object> inputObjs = new ArrayList<>();

    // First entity - new entity
    Map<String, Object> input1 = new HashMap<>();
    input1.put(FIELD_NAME, "Entity 1");
    input1.put(FIELD_DESCRIPTION, "Description 1");
    input1.put(FIELD_PRIVATE_CODE, "PC1");
    inputObjs.add(input1);

    // Second entity - existing entity
    Map<String, Object> input2 = new HashMap<>();
    input2.put(FIELD_ID, "existing-id");
    input2.put(FIELD_NAME, "Entity 2");
    input2.put(FIELD_DESCRIPTION, "Description 2");
    input2.put(FIELD_PRIVATE_CODE, "PC2");
    inputObjs.add(input2);

    // Mock existing entity
    TestGroupOfEntities existingEntity = new TestGroupOfEntities();
    existingEntity.setNetexId("existing-id");
    when(repository.getOne("existing-id")).thenReturn(existingEntity);
    when(repository.findByIds(any())).thenReturn(Arrays.asList(existingEntity));

    // When
    List<TestGroupOfEntities> result = mapper.mapList(inputObjs);

    // Then
    assertEquals(2, result.size());

    // First entity (new)
    TestGroupOfEntities entity1 = result.get(0);
    assertEquals("Entity 1", entity1.getName());
    assertEquals("Description 1", entity1.getDescription());
    assertEquals("PC1", entity1.getPrivateCode());
    assertEquals(provider, entity1.getProvider());

    // Second entity (existing)
    TestGroupOfEntities entity2 = result.get(1);
    assertEquals("existing-id", entity2.getNetexId());
    assertEquals("Entity 2", entity2.getName());
    assertEquals("Description 2", entity2.getDescription());
    assertEquals("PC2", entity2.getPrivateCode());
  }

  // Test implementation of AbstractGroupOfEntitiesMapper
  private static class TestGroupOfEntitiesMapper
    extends AbstractGroupOfEntitiesMapper<TestGroupOfEntities> {

    public TestGroupOfEntitiesMapper(
      ProviderRepository providerRepository,
      ProviderEntityRepository<TestGroupOfEntities> repository
    ) {
      super(providerRepository, repository);
    }

    @Override
    protected TestGroupOfEntities createNewEntity(
      no.entur.uttu.graphql.ArgumentWrapper input
    ) {
      return new TestGroupOfEntities();
    }

    @Override
    protected void populateEntityFromInput(
      TestGroupOfEntities entity,
      no.entur.uttu.graphql.ArgumentWrapper input
    ) {
      // No additional fields to populate for test
    }
  }

  // Test implementation of GroupOfEntities_VersionStructure
  private static class TestGroupOfEntities extends GroupOfEntities_VersionStructure {
    // No additional implementation needed for test
  }
}
