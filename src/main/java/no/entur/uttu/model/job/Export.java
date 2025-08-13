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

package no.entur.uttu.model.job;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import no.entur.uttu.model.ProviderEntity;

@Entity
public class Export extends ProviderEntity {

  private String name;

  @NotNull
  @Enumerated(EnumType.STRING)
  private ExportStatusEnumeration exportStatus = ExportStatusEnumeration.IN_PROGRESS;

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ExportMessage> messages = new ArrayList<>();

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  private String fileName;

  private boolean dryRun;

  private boolean generateServiceLinks;

  private boolean includeDatedServiceJourneys;

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "export")
  private Collection<ExportLineAssociation> exportLineAssociations;

  public Collection<ExportLineAssociation> getExportLineAssociations() {
    return exportLineAssociations;
  }

  public void setExportLineAssociations(
    Collection<ExportLineAssociation> exportLineAssociations
  ) {
    this.exportLineAssociations = exportLineAssociations;
  }

  public void markAsFinished() {
    if (
      messages.stream().anyMatch(m -> SeverityEnumeration.ERROR.equals(m.getSeverity()))
    ) {
      exportStatus = ExportStatusEnumeration.FAILED;
    } else {
      exportStatus = ExportStatusEnumeration.SUCCESS;
    }
  }

  public void addMessage(ExportMessage message) {
    this.messages.add(message);
  }

  public SortedSet<ExportMessage> getMessages() {
    return new TreeSet<>(messages);
  }

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public boolean isDryRun() {
    return dryRun;
  }

  public void setDryRun(boolean dryRun) {
    this.dryRun = dryRun;
  }

  public boolean isGenerateServiceLinks() {
    return generateServiceLinks;
  }

  public void setGenerateServiceLinks(boolean generateServiceLinks) {
    this.generateServiceLinks = generateServiceLinks;
  }

  public boolean isIncludeDatedServiceJourneys() {
    return includeDatedServiceJourneys;
  }

  public void setIncludeDatedServiceJourneys(boolean includeDatedServiceJourneys) {
    this.includeDatedServiceJourneys = includeDatedServiceJourneys;
  }

  @Override
  public String toString() {
    return (
      "Export{" +
      super.toString() +
      ", name='" +
      name +
      '\'' +
      ", exportStatus=" +
      exportStatus +
      ", messages=" +
      messages +
      '}'
    );
  }
}
