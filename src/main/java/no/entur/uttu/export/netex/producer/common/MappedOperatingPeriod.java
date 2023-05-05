package no.entur.uttu.export.netex.producer.common;

import org.rutebanken.netex.model.OperatingDay;
import org.rutebanken.netex.model.OperatingPeriod;

import java.util.List;

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
