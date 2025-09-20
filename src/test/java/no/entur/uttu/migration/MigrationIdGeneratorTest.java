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

package no.entur.uttu.migration;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import no.entur.uttu.migration.LineMigrationService.ConflictResolutionStrategy;
import no.entur.uttu.migration.MigrationIdGenerator.ConflictSkippedException;
import no.entur.uttu.model.Codespace;
import no.entur.uttu.model.FixedLine;
import no.entur.uttu.model.FlexibleLine;
import no.entur.uttu.model.JourneyPattern;
import no.entur.uttu.model.Provider;
import no.entur.uttu.model.ProviderEntity;
import no.entur.uttu.model.ServiceJourney;
import no.entur.uttu.repository.FixedLineRepository;
import no.entur.uttu.repository.FlexibleLineRepository;
import no.entur.uttu.repository.JourneyPatternRepository;
import no.entur.uttu.repository.ServiceJourneyRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MigrationIdGeneratorTest {

  @Mock
  private FixedLineRepository fixedLineRepository;

  @Mock
  private FlexibleLineRepository flexibleLineRepository;

  @Mock
  private JourneyPatternRepository journeyPatternRepository;

  @Mock
  private ServiceJourneyRepository serviceJourneyRepository;

  private MigrationIdGenerator idGenerator;

  private Provider targetProvider;
  private Codespace codespace;

  @Before
  public void setUp() {
    idGenerator = new MigrationIdGenerator(
      fixedLineRepository,
      flexibleLineRepository,
      journeyPatternRepository,
      serviceJourneyRepository
    );

    codespace = new Codespace();
    codespace.setXmlns("TEST");

    targetProvider = new Provider();
    targetProvider.setCode("TEST_PROVIDER");
    targetProvider.setCodespace(codespace);
  }

  @Test
  public void testGenerateNetexId() {
    FixedLine line = new FixedLine();

    String generatedId = idGenerator.generateNetexId(line, targetProvider);

    assertNotNull(generatedId);
    assertTrue(generatedId.startsWith("TEST:Line:"));
    String[] parts = generatedId.split(":");
    assertEquals(3, parts.length);
    assertEquals("TEST", parts[0]);
    assertEquals("Line", parts[1]);
    assertNotNull(parts[2]);
  }

  @Test
  public void testGenerateNetexIdDifferentEntity() {
    JourneyPattern journeyPattern = new JourneyPattern();

    String generatedId = idGenerator.generateNetexId(journeyPattern, targetProvider);

    assertNotNull(generatedId);
    assertTrue(generatedId.startsWith("TEST:JourneyPattern:"));
  }

  @Test
  public void testGenerateUniqueNameNoConflict() {
    String originalName = "Test Line";
    when(
      fixedLineRepository.findByProviderAndName(targetProvider, originalName)
    ).thenReturn(null);
    when(
      flexibleLineRepository.findByProviderAndName(targetProvider, originalName)
    ).thenReturn(null);

    String result = idGenerator.generateUniqueNameWithConflictResolution(
      originalName,
      "Line",
      targetProvider,
      ConflictResolutionStrategy.FAIL
    );

    assertEquals(originalName, result);
  }

  @Test
  public void testGenerateUniqueNameConflictWithFail() {
    String originalName = "Test Line";
    FixedLine existingLine = new FixedLine();
    when(
      fixedLineRepository.findByProviderAndName(targetProvider, originalName)
    ).thenReturn(existingLine);

    try {
      idGenerator.generateUniqueNameWithConflictResolution(
        originalName,
        "Line",
        targetProvider,
        ConflictResolutionStrategy.FAIL
      );
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().contains("already exists in provider TEST_PROVIDER"));
    }
  }

  @Test
  public void testGenerateUniqueNameConflictWithRename() {
    String originalName = "Test Line";
    FixedLine existingLine = new FixedLine();
    when(
      fixedLineRepository.findByProviderAndName(targetProvider, originalName)
    ).thenReturn(existingLine);
    when(
      fixedLineRepository.findByProviderAndName(
        eq(targetProvider),
        startsWith("Test Line_migrated_")
      )
    ).thenReturn(null);
    when(
      flexibleLineRepository.findByProviderAndName(
        eq(targetProvider),
        startsWith("Test Line_migrated_")
      )
    ).thenReturn(null);

    String result = idGenerator.generateUniqueNameWithConflictResolution(
      originalName,
      "Line",
      targetProvider,
      ConflictResolutionStrategy.RENAME
    );

    assertNotNull(result);
    assertTrue(result.startsWith("Test Line_migrated_"));
    assertNotEquals(originalName, result);
  }

  @Test
  public void testGenerateUniqueNameConflictWithRenameMultipleAttempts() {
    String originalName = "Test Line";
    FixedLine existingLine = new FixedLine();

    when(
      fixedLineRepository.findByProviderAndName(targetProvider, originalName)
    ).thenReturn(existingLine);
    when(
      fixedLineRepository.findByProviderAndName(
        eq(targetProvider),
        startsWith("Test Line_migrated_")
      )
    )
      .thenReturn(existingLine)
      .thenReturn(existingLine)
      .thenReturn(null);
    when(
      flexibleLineRepository.findByProviderAndName(eq(targetProvider), anyString())
    ).thenReturn(null);

    String result = idGenerator.generateUniqueNameWithConflictResolution(
      originalName,
      "Line",
      targetProvider,
      ConflictResolutionStrategy.RENAME
    );

    assertNotNull(result);
    assertTrue(result.contains("_migrated_"));
    assertTrue(result.contains("_2") || result.contains("_3"));
  }

  @Test
  public void testGenerateUniqueNameConflictWithSkip() {
    String originalName = "Test Line";
    FixedLine existingLine = new FixedLine();
    when(
      fixedLineRepository.findByProviderAndName(targetProvider, originalName)
    ).thenReturn(existingLine);

    try {
      idGenerator.generateUniqueNameWithConflictResolution(
        originalName,
        "Line",
        targetProvider,
        ConflictResolutionStrategy.SKIP
      );
      fail("Expected ConflictSkippedException");
    } catch (ConflictSkippedException e) {
      assertTrue(e.getMessage().contains("skipping due to SKIP strategy"));
    }
  }

  @Test
  public void testJourneyPatternNameConflict() {
    String originalName = "Test Pattern";
    JourneyPattern existingPattern = new JourneyPattern();
    when(
      journeyPatternRepository.findByProviderAndName(targetProvider, originalName)
    ).thenReturn(existingPattern);

    try {
      idGenerator.generateUniqueNameWithConflictResolution(
        originalName,
        "JourneyPattern",
        targetProvider,
        ConflictResolutionStrategy.FAIL
      );
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().contains("JourneyPattern"));
    }
  }

  @Test
  public void testServiceJourneyNameConflict() {
    String originalName = "Test Journey";
    ServiceJourney existingJourney = new ServiceJourney();
    when(
      serviceJourneyRepository.findByProviderAndName(targetProvider, originalName)
    ).thenReturn(existingJourney);

    try {
      idGenerator.generateUniqueNameWithConflictResolution(
        originalName,
        "ServiceJourney",
        targetProvider,
        ConflictResolutionStrategy.FAIL
      );
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().contains("ServiceJourney"));
    }
  }

  @Test
  public void testFlexibleLineNameConflict() {
    String originalName = "Test Flexible Line";
    FlexibleLine existingLine = new FlexibleLine();
    when(
      fixedLineRepository.findByProviderAndName(targetProvider, originalName)
    ).thenReturn(null);
    when(
      flexibleLineRepository.findByProviderAndName(targetProvider, originalName)
    ).thenReturn(existingLine);

    try {
      idGenerator.generateUniqueNameWithConflictResolution(
        originalName,
        "Line",
        targetProvider,
        ConflictResolutionStrategy.FAIL
      );
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().contains("Line with name"));
    }
  }

  @Test
  public void testIdMappings() {
    String oldId = "OLD:Line:123";
    String newId = "NEW:Line:456";

    assertNull(idGenerator.getMappedId(oldId));

    idGenerator.addIdMapping(oldId, newId);
    assertEquals(newId, idGenerator.getMappedId(oldId));

    idGenerator.clearMappings();
    assertNull(idGenerator.getMappedId(oldId));
  }

  @Test
  public void testMultipleIdMappings() {
    String oldId1 = "OLD:Line:123";
    String newId1 = "NEW:Line:456";
    String oldId2 = "OLD:JourneyPattern:789";
    String newId2 = "NEW:JourneyPattern:012";

    idGenerator.addIdMapping(oldId1, newId1);
    idGenerator.addIdMapping(oldId2, newId2);

    assertEquals(newId1, idGenerator.getMappedId(oldId1));
    assertEquals(newId2, idGenerator.getMappedId(oldId2));

    idGenerator.clearMappings();
    assertNull(idGenerator.getMappedId(oldId1));
    assertNull(idGenerator.getMappedId(oldId2));
  }

  @Test
  public void testUnknownEntityTypeNoConflict() {
    String originalName = "Test Unknown";

    String result = idGenerator.generateUniqueNameWithConflictResolution(
      originalName,
      "UnknownType",
      targetProvider,
      ConflictResolutionStrategy.FAIL
    );

    assertEquals(originalName, result);
  }

  @Test
  public void testInvalidConflictResolutionStrategy() {
    String originalName = "Test Line";
    FixedLine existingLine = new FixedLine();
    when(
      fixedLineRepository.findByProviderAndName(targetProvider, originalName)
    ).thenReturn(existingLine);

    try {
      idGenerator.generateUniqueNameWithConflictResolution(
        originalName,
        "Line",
        targetProvider,
        null
      );
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().contains("Unknown conflict resolution strategy"));
    }
  }
}
