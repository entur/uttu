package no.entur.uttu.model;

import java.time.LocalDate;
import java.util.List;

public class ExportedPublicLine {

  private LocalDate operatingPeriodFrom;
  private LocalDate operatingPeriodTo;
  private String publicCode;
  private String providerCode;
  private List<ExportedLineStatistics> lines;

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

  public String getPublicCode() {
    return publicCode;
  }

  public void setPublicCode(String publicCode) {
    this.publicCode = publicCode;
  }

  public List<ExportedLineStatistics> getLines() {
    return lines;
  }

  public void setLines(List<ExportedLineStatistics> lines) {
    this.lines = lines;
  }

  public String getProviderCode() {
    return providerCode;
  }

  public void setProviderCode(String providerCode) {
    this.providerCode = providerCode;
  }
}
