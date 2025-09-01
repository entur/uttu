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

package no.entur.uttu.stopplace.loader;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.time.Instant;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import no.entur.uttu.stopplace.spi.StopPlaceDataLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class NetexFileStopPlaceLoaderTest {

  @TempDir
  File tempDir;

  private NetexFileStopPlaceLoader loader;

  @BeforeEach
  void setUp() {
    loader = new NetexFileStopPlaceLoader();
  }

  @Test
  void testLoadFromValidXmlFile() throws Exception {
    // Create a valid NeTEx XML file
    File xmlFile = new File(tempDir, "test-stops.xml");
    String validXmlContent = createValidNetexXml();
    writeToFile(xmlFile, validXmlContent);

    setNetexFileUri(xmlFile.getAbsolutePath());

    StopPlaceDataLoader.LoadResult result = loader.loadStopPlaces();

    assertNotNull(result);
    assertEquals(2, result.stopPlaces().size());
    assertNotNull(result.publicationTime());

    // Check stop place details
    assertEquals("NSR:StopPlace:1", result.stopPlaces().get(0).getId());
    assertEquals("Oslo S", result.stopPlaces().get(0).getName().getValue());
    assertEquals("NSR:StopPlace:2", result.stopPlaces().get(1).getId());
    assertEquals("Bergen", result.stopPlaces().get(1).getName().getValue());
  }

  @Test
  void testLoadFromValidZipFile() throws Exception {
    // Create a zip file containing valid NeTEx XML
    File zipFile = new File(tempDir, "test-stops.zip");
    String validXmlContent = createValidNetexXml();
    createZipWithXmlContent(zipFile, "stops.xml", validXmlContent);

    setNetexFileUri(zipFile.getAbsolutePath());

    StopPlaceDataLoader.LoadResult result = loader.loadStopPlaces();

    assertNotNull(result);
    assertEquals(2, result.stopPlaces().size());
    assertNotNull(result.publicationTime());
  }

  @Test
  void testLoadFromEmptyZipFile() throws Exception {
    // Create zip file with no entries (truly empty)
    File emptyZipFile = new File(tempDir, "empty.zip");
    createEmptyZipFile(emptyZipFile);

    setNetexFileUri(emptyZipFile.getAbsolutePath());

    RuntimeException exception = assertThrows(
      RuntimeException.class,
      () -> loader.loadStopPlaces()
    );

    // The exception will be from XML parsing since it falls back after ZIP fails
    assertTrue(exception.getMessage().contains("Unable to load stop places from"));
  }

  @Test
  void testLoadFromZipWithEmptyEntry() throws Exception {
    // Create zip file with an entry but no content
    File zipFile = new File(tempDir, "empty-entry.zip");
    createZipWithXmlContent(zipFile, "empty.xml", "");

    setNetexFileUri(zipFile.getAbsolutePath());

    RuntimeException exception = assertThrows(
      RuntimeException.class,
      () -> loader.loadStopPlaces()
    );

    assertTrue(exception.getMessage().contains("Failed to unmarshal publication delivery from zip"));
  }

  @Test
  void testLoadFromNonExistentFile() throws Exception {
    String nonExistentPath = new File(tempDir, "non-existent.xml").getAbsolutePath();
    setNetexFileUri(nonExistentPath);

    RuntimeException exception = assertThrows(
      RuntimeException.class,
      () -> loader.loadStopPlaces()
    );

    assertTrue(exception.getMessage().contains("Unable to load stop places from"));
    assertTrue(exception.getCause().getMessage().contains("File not found"));
  }

  @Test
  void testLoadFromInvalidXmlFile() throws Exception {
    // Create invalid XML file
    File invalidXmlFile = new File(tempDir, "invalid.xml");
    writeToFile(invalidXmlFile, "<invalid>not netex xml</invalid>");

    setNetexFileUri(invalidXmlFile.getAbsolutePath());

    RuntimeException exception = assertThrows(
      RuntimeException.class,
      () -> loader.loadStopPlaces()
    );

    assertTrue(exception.getMessage().contains("Unable to load stop places from"));
  }

  @Test
  void testLoadFromZipWithInvalidXml() throws Exception {
    // Create zip file with invalid XML content
    File zipFile = new File(tempDir, "invalid-content.zip");
    createZipWithXmlContent(zipFile, "invalid.xml", "<invalid>not netex xml</invalid>");

    setNetexFileUri(zipFile.getAbsolutePath());

    RuntimeException exception = assertThrows(
      RuntimeException.class,
      () -> loader.loadStopPlaces()
    );

    assertTrue(exception.getMessage().contains("Failed to unmarshal publication delivery from zip"));
  }

  @Test
  void testLoadFromXmlFileWithNoStopPlaces() throws Exception {
    // Create valid NeTEx XML but with no stop places
    File xmlFile = new File(tempDir, "empty-stops.xml");
    String emptyNetexXml = createEmptyNetexXml();
    writeToFile(xmlFile, emptyNetexXml);

    setNetexFileUri(xmlFile.getAbsolutePath());

    StopPlaceDataLoader.LoadResult result = loader.loadStopPlaces();

    assertNotNull(result);
    assertTrue(result.stopPlaces().isEmpty());
    assertNotNull(result.publicationTime());
  }

  @Test
  void testLoadFromZipWithMultipleEntries() throws Exception {
    // Create zip file with multiple entries - should use first one
    File zipFile = new File(tempDir, "multiple-entries.zip");

    try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
      // First entry with valid content
      ZipEntry entry1 = new ZipEntry("stops1.xml");
      zos.putNextEntry(entry1);
      zos.write(createValidNetexXml().getBytes());
      zos.closeEntry();

      // Second entry (should be ignored)
      ZipEntry entry2 = new ZipEntry("stops2.xml");
      zos.putNextEntry(entry2);
      zos.write(createValidNetexXml().getBytes());
      zos.closeEntry();
    }

    setNetexFileUri(zipFile.getAbsolutePath());

    StopPlaceDataLoader.LoadResult result = loader.loadStopPlaces();

    assertNotNull(result);
    assertEquals(2, result.stopPlaces().size()); // Should only load from first entry
  }

  @Test
  void testFallbackFromZipToXml() throws Exception {
    // Create a file with .zip extension but XML content
    // This should fail as zip first, then succeed as XML
    File xmlFileWithZipName = new File(tempDir, "actually-xml.zip");
    String validXmlContent = createValidNetexXml();
    writeToFile(xmlFileWithZipName, validXmlContent);

    setNetexFileUri(xmlFileWithZipName.getAbsolutePath());

    StopPlaceDataLoader.LoadResult result = loader.loadStopPlaces();

    assertNotNull(result);
    assertEquals(2, result.stopPlaces().size());
  }

  // Helper methods
  private void setNetexFileUri(String path) throws Exception {
    Field field = NetexFileStopPlaceLoader.class.getDeclaredField("netexFileUri");
    field.setAccessible(true);
    field.set(loader, path);
  }

  private String createValidNetexXml() {
    return """
    <?xml version="1.0" encoding="UTF-8"?>
    <PublicationDelivery xmlns="http://www.netex.org.uk/netex"
                         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                         version="1.0"
                         xsi:schemaLocation="http://www.netex.org.uk/netex">
      <PublicationTimestamp>2024-01-15T10:30:00</PublicationTimestamp>
      <dataObjects>
        <SiteFrame version="1" id="NSR:SiteFrame:1">
          <stopPlaces>
            <StopPlace version="1" id="NSR:StopPlace:1">
              <Name>Oslo S</Name>
            </StopPlace>
            <StopPlace version="1" id="NSR:StopPlace:2">
              <Name>Bergen</Name>
            </StopPlace>
          </stopPlaces>
        </SiteFrame>
      </dataObjects>
    </PublicationDelivery>
    """;
  }

  private String createEmptyNetexXml() {
    return """
    <?xml version="1.0" encoding="UTF-8"?>
    <PublicationDelivery xmlns="http://www.netex.org.uk/netex"
                         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                         version="1.0"
                         xsi:schemaLocation="http://www.netex.org.uk/netex">
      <PublicationTimestamp>2024-01-15T10:30:00</PublicationTimestamp>
      <dataObjects>
        <SiteFrame version="1" id="NSR:SiteFrame:1">
          <stopPlaces>
          </stopPlaces>
        </SiteFrame>
      </dataObjects>
    </PublicationDelivery>
    """;
  }

  private void writeToFile(File file, String content) throws IOException {
    try (FileOutputStream fos = new FileOutputStream(file)) {
      fos.write(content.getBytes());
    }
  }

  private void createZipWithXmlContent(File zipFile, String entryName, String xmlContent)
    throws IOException {
    try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
      ZipEntry entry = new ZipEntry(entryName);
      zos.putNextEntry(entry);
      zos.write(xmlContent.getBytes());
      zos.closeEntry();
    }
  }

  private void createEmptyZipFile(File zipFile) throws IOException {
    try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
      // Don't add any entries
    }
  }
}
