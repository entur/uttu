package no.entur.uttu.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Entity
public class ExportedDayTypeStatistics {

  @Id
  @GeneratedValue(generator = "sequence_per_table_generator")
  protected Long id;

  @ManyToOne
  @NotNull
  private ExportedLineStatistics exportedLineStatistics;

  @NotNull
  protected String serviceJourneyName;

  @NotNull
  protected String dayTypeNetexId;

  @NotNull
  private LocalDate operatingPeriodFrom;

  @NotNull
  private LocalDate operatingPeriodTo;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public ExportedLineStatistics getExportedLineStatistics() {
    return exportedLineStatistics;
  }

  public void setExportedLineStatistics(ExportedLineStatistics exportedLineStatistics) {
    this.exportedLineStatistics = exportedLineStatistics;
  }

  public String getDayTypeNetexId() {
    return dayTypeNetexId;
  }

  public void setDayTypeNetexId(String dayTypeNetexId) {
    this.dayTypeNetexId = dayTypeNetexId;
  }

  public LocalDate getOperatingPeriodFrom() {
    return operatingPeriodFrom;
  }

  public void setOperatingPeriodFrom(LocalDate operatingPeriodFrom) {
    this.operatingPeriodFrom = operatingPeriodFrom;
  }

  public LocalDate getOperatingPeriodTo() {
    return operatingPeriodTo;
  }

  public void setOperatingPeriodTo(LocalDate operatingPeriodTo) {
    this.operatingPeriodTo = operatingPeriodTo;
  }

  public String getServiceJourneyName() {
    return serviceJourneyName;
  }

  public void setServiceJourneyName(String serviceJourneyName) {
    this.serviceJourneyName = serviceJourneyName;
  }
}
