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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.xml.transform.stream.StreamSource;
import no.entur.uttu.netex.NetexUnmarshaller;
import no.entur.uttu.stopplace.spi.StopPlaceDataLoader;
import org.rutebanken.netex.model.PublicationDeliveryStructure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Loads stop places from a NeTEx file (either XML or zipped XML).
 */
@Component
@ConditionalOnProperty(name = "uttu.stopplace.netex-file-uri")
public class NetexFileStopPlaceLoader implements StopPlaceDataLoader {

  private static final Logger logger = LoggerFactory.getLogger(
    NetexFileStopPlaceLoader.class
  );

  private final NetexUnmarshaller netexUnmarshaller = new NetexUnmarshaller(
    PublicationDeliveryStructure.class
  );

  @Value("${uttu.stopplace.netex-file-uri}")
  private String netexFileUri;

  @Override
  public LoadResult loadStopPlaces() {
    logger.info("Loading stop places from: {}", netexFileUri);

    // Try loading as zip file first
    try {
      return loadFromZipFile();
    } catch (IOException e) {
      logger.debug("Not a zip file, trying as plain XML", e);
    }

    // Try loading as plain XML file
    try {
      return loadFromXmlFile();
    } catch (Exception e) {
      throw new StopPlaceLoadingException(
        "Unable to load stop places from: " + netexFileUri,
        e
      );
    }
  }

  private LoadResult loadFromZipFile() throws IOException {
    try (ZipFile zipFile = new ZipFile(netexFileUri)) {
      Enumeration<? extends ZipEntry> entries = zipFile.entries();

      if (!entries.hasMoreElements()) {
        throw new IOException("Empty zip file");
      }

      ZipEntry entry = entries.nextElement();
      logger.info("Found zip entry: {}", entry.getName());

      try (InputStream inputStream = zipFile.getInputStream(entry)) {
        PublicationDeliveryStructure publicationDelivery;
        try {
          publicationDelivery = netexUnmarshaller.unmarshalFromSource(
            new StreamSource(inputStream)
          );
        } catch (Exception e) {
          throw new StopPlaceLoadingException(
            "Failed to unmarshal publication delivery from zip",
            e
          );
        }

        NetexStopPlaceExtractor.ExtractResult extractResult =
          NetexStopPlaceExtractor.extractAll(publicationDelivery);

        logger.info(
          "Loaded {} stop places from zip file",
          extractResult.stopPlaces().size()
        );

        return new LoadResult(
          extractResult.stopPlaces(),
          extractResult.publicationTime()
        );
      }
    }
  }

  private LoadResult loadFromXmlFile() {
    File file = new File(netexFileUri);

    if (!file.exists()) {
      throw new StopPlaceLoadingException("File not found: " + netexFileUri);
    }

    PublicationDeliveryStructure publicationDelivery;
    try {
      publicationDelivery = netexUnmarshaller.unmarshalFromSource(new StreamSource(file));
    } catch (Exception e) {
      throw new StopPlaceLoadingException(
        "Failed to unmarshal publication delivery from file",
        e
      );
    }

    NetexStopPlaceExtractor.ExtractResult extractResult =
      NetexStopPlaceExtractor.extractAll(publicationDelivery);

    logger.info("Loaded {} stop places from XML file", extractResult.stopPlaces().size());

    return new LoadResult(extractResult.stopPlaces(), extractResult.publicationTime());
  }
}
