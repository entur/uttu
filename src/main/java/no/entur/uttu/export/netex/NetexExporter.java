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

package no.entur.uttu.export.netex;

import static jakarta.xml.bind.JAXBContext.newInstance;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.PostConstruct;
import no.entur.uttu.error.codederror.CodedError;
import no.entur.uttu.error.codes.ErrorCodeEnumeration;
import no.entur.uttu.export.model.ExportException;
import no.entur.uttu.export.netex.producer.common.NetexCommonFileProducer;
import no.entur.uttu.export.netex.producer.line.NetexLineFileProducer;
import no.entur.uttu.model.Line;
import no.entur.uttu.model.ProviderEntity;
import no.entur.uttu.model.job.Export;
import no.entur.uttu.model.job.ExportLineAssociation;
import no.entur.uttu.repository.FixedLineRepository;
import no.entur.uttu.repository.FlexibleLineRepository;
import no.entur.uttu.repository.generic.ProviderEntityRepository;
import no.entur.uttu.util.Preconditions;
import org.rutebanken.netex.model.PublicationDeliveryStructure;
import org.rutebanken.netex.validation.NeTExValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NetexExporter {

  @Autowired
  private FlexibleLineRepository flexibleLineRepository;

  @Autowired
  private FixedLineRepository fixedLineRepository;

  @Autowired
  private NetexLineFileProducer netexLineFileProducer;

  @Autowired
  private NetexCommonFileProducer commonFileProducer;

  private JAXBContext jaxbContext;

  private NeTExValidator netexValidator;

  @PostConstruct
  public void asyncInit() {
    new Thread(this::assertInit).start();
  }

  public void exportDataSet(
    Export export,
    DataSetProducer dataSetProducer,
    boolean validateAgainstSchema
  ) {
    NetexExportContext exportContext = new NetexExportContext(export);

    List<no.entur.uttu.model.FlexibleLine> flexibleLines =
      findAllValidEntitiesFromRepository(flexibleLineRepository, exportContext);
    List<no.entur.uttu.model.FixedLine> fixedLines = findAllValidEntitiesFromRepository(
      fixedLineRepository,
      exportContext
    );

    List<Line> lines = Stream
      .concat(flexibleLines.stream(), fixedLines.stream())
      .collect(Collectors.toList());

    List<Line> linesToExport = findLinesToExport(
      export.getExportLineAssociations(),
      lines
    );

    linesToExport
      .stream()
      .map(line -> netexLineFileProducer.toNetexFile(line, exportContext))
      .forEach(netexFile ->
        marshalToFile(netexFile, dataSetProducer, validateAgainstSchema)
      );

    marshalToFile(
      commonFileProducer.toCommonFile(exportContext),
      dataSetProducer,
      validateAgainstSchema
    );
  }

  protected List<Line> findLinesToExport(
    Collection<ExportLineAssociation> exportLineAssociations,
    List<Line> lines
  ) {
    List<Line> linesToExport = lines;

    if (null != exportLineAssociations && !exportLineAssociations.isEmpty()) {
      linesToExport =
        linesToExport
          .stream()
          .filter(line ->
            exportLineAssociations.stream().anyMatch(la -> la.getLine() == line)
          )
          .collect(Collectors.toList());
    }

    Preconditions.checkArgument(
      !linesToExport.isEmpty(),
      CodedError.fromErrorCode(ErrorCodeEnumeration.NO_VALID_LINES_IN_DATA_SPACE),
      "No valid lines in data space"
    );

    return linesToExport;
  }

  private <T extends ProviderEntity> List<T> findAllValidEntitiesFromRepository(
    ProviderEntityRepository<T> repository,
    NetexExportContext exportContext
  ) {
    return repository
      .findAll()
      .stream()
      .filter(exportContext::isValid)
      .collect(Collectors.toList());
  }

  private void marshalToFile(
    NetexFile file,
    DataSetProducer dataSetProducer,
    boolean validateAgainstSchema
  ) {
    assertInit();
    try {
      Marshaller marshaller = jaxbContext.createMarshaller();

      marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

      if (validateAgainstSchema) {
        marshaller.setSchema(netexValidator.getSchema());
      }
      OutputStream outputStream = dataSetProducer.addFile(file.getFileName());
      marshaller.marshal(file.getPublicationDeliveryStructure(), outputStream);
    } catch (Exception e) {
      throw new ExportException(
        "Failed to marshal NeTEx XML to file: " + e.getMessage(),
        e
      );
    }
  }

  private void assertInit() {
    if (jaxbContext == null) {
      try {
        jaxbContext = newInstance(PublicationDeliveryStructure.class);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
    if (netexValidator == null) {
      try {
        netexValidator = new NeTExValidator();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }
}
