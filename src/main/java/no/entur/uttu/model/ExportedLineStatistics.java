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

package no.entur.uttu.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import no.entur.uttu.model.job.Export;

@Entity
public class ExportedLineStatistics {

  @Id
  @GeneratedValue(generator = "sequence_per_table_generator")
  protected Long id;

  @NotNull
  protected String lineName;

  @NotNull
  private LocalDate operatingPeriodFrom;

  @NotNull
  private LocalDate operatingPeriodTo;

  private String publicCode;

  @ManyToOne
  @NotNull
  private Export export;

  @OneToMany(
    mappedBy = "exportedLineStatistics",
    cascade = CascadeType.ALL,
    orphanRemoval = true,
    fetch = FetchType.EAGER
  )
  @NotNull
  private final List<ExportedDayTypeStatistics> exportedDayTypesStatistics =
    new ArrayList<>();

  public String getLineName() {
    return lineName;
  }

  public void setLineName(String lineName) {
    this.lineName = lineName;
  }

  public LocalDate getOperatingPeriodFrom() {
    return operatingPeriodFrom;
  }

  public void setOperatingPeriodFrom(LocalDate fromDate) {
    this.operatingPeriodFrom = fromDate;
  }

  public LocalDate getOperatingPeriodTo() {
    return operatingPeriodTo;
  }

  public void setOperatingPeriodTo(LocalDate toDate) {
    this.operatingPeriodTo = toDate;
  }

  public String getPublicCode() {
    return publicCode;
  }

  public void setPublicCode(String publicCode) {
    this.publicCode = publicCode;
  }

  public Long getId() {
    return id;
  }

  public Export getExport() {
    return export;
  }

  public void setExport(Export export) {
    this.export = export;
  }

  public boolean isValid(LocalDate from, LocalDate to) {
    return !(operatingPeriodFrom.isAfter(to) || operatingPeriodTo.isBefore(from));
  }

  public List<ExportedDayTypeStatistics> getExportedDayTypesStatistics() {
    return exportedDayTypesStatistics;
  }

  public void addExportedDayTypesStatistics(
    ExportedDayTypeStatistics exportedDayTypesStatisticsToAdd
  ) {
    exportedDayTypesStatisticsToAdd.setExportedLineStatistics(this);
    exportedDayTypesStatistics.add(exportedDayTypesStatisticsToAdd);
  }
}
