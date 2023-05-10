package no.entur.uttu.export.netex.producer.common;

import java.util.List;
import org.rutebanken.netex.model.OperatingDay;
import org.rutebanken.netex.model.OperatingPeriod;

public class MappedOperatingPeriod {

  private final OperatingPeriod operatingPeriod;
  private final List<OperatingDay> operatingDays;

  public MappedOperatingPeriod(
    OperatingPeriod operatingPeriod,
    List<OperatingDay> operatingDays
  ) {
    this.operatingPeriod = operatingPeriod;
    this.operatingDays = operatingDays;
  }

  public OperatingPeriod getOperatingPeriod() {
    return operatingPeriod;
  }

  public List<OperatingDay> getOperatingDays() {
    return operatingDays;
  }
}
