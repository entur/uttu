package no.entur.uttu.export.linestatistics;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import no.entur.uttu.model.*;
import org.junit.Test;

public class ExportedLineStatisticsServiceTest {

  @Test
  public void dayTypeWithOperatingPeriodToExportedDayTypeStatisticsDoneRight() {
    OperatingPeriod operatingPeriod = new OperatingPeriod();
    operatingPeriod.setFromDate(LocalDate.of(2022, 1, 1));
    operatingPeriod.setToDate(LocalDate.of(2022, 3, 4));

    DayTypeAssignment dayTypeAssignment1 = new DayTypeAssignment();
    dayTypeAssignment1.setAvailable(true);
    dayTypeAssignment1.setOperatingPeriod(operatingPeriod);

    DayType dayType = new DayType();
    dayType.setDayTypeAssignments(List.of(dayTypeAssignment1));

    ExportedDayTypeStatistics exportedDayTypeStatisticsForDayType =
      ExportedLineStatisticsService.getExportedDayTypeStatisticsForDayType(dayType);

    assertEquals(
      LocalDate.of(2022, 1, 1),
      exportedDayTypeStatisticsForDayType.getOperatingPeriodFrom()
    );
    assertEquals(
      LocalDate.of(2022, 3, 4),
      exportedDayTypeStatisticsForDayType.getOperatingPeriodTo()
    );
  }

  @Test
  public void dayTypeWithMultipleDayTypeAssignmentsToExportedDayTypeStatisticsDoneRight() {
    OperatingPeriod operatingPeriod1 = new OperatingPeriod();
    operatingPeriod1.setFromDate(LocalDate.of(2022, 1, 1));
    operatingPeriod1.setToDate(LocalDate.of(2022, 3, 4));

    OperatingPeriod operatingPeriod2 = new OperatingPeriod();
    operatingPeriod2.setFromDate(LocalDate.of(2022, 5, 4));
    operatingPeriod2.setToDate(LocalDate.of(2022, 8, 5));

    DayTypeAssignment dayTypeAssignment1 = new DayTypeAssignment();
    dayTypeAssignment1.setAvailable(true);
    dayTypeAssignment1.setOperatingPeriod(operatingPeriod1);

    DayTypeAssignment dayTypeAssignment2 = new DayTypeAssignment();
    dayTypeAssignment2.setAvailable(true);
    dayTypeAssignment2.setOperatingPeriod(operatingPeriod2);

    DayType dayType = new DayType();
    dayType.setDayTypeAssignments(List.of(dayTypeAssignment1, dayTypeAssignment2));

    ExportedDayTypeStatistics exportedDayTypeStatisticsForDayType =
      ExportedLineStatisticsService.getExportedDayTypeStatisticsForDayType(dayType);

    assertEquals(
      LocalDate.of(2022, 1, 1),
      exportedDayTypeStatisticsForDayType.getOperatingPeriodFrom()
    );
    assertEquals(
      LocalDate.of(2022, 8, 5),
      exportedDayTypeStatisticsForDayType.getOperatingPeriodTo()
    );
  }

  @Test
  public void dayTypeWithDateToExportedDayTypeStatisticsDoneRight() {
    DayTypeAssignment dayTypeAssignment1 = new DayTypeAssignment();
    dayTypeAssignment1.setAvailable(true);
    dayTypeAssignment1.setDate(LocalDate.of(2022, 1, 1));

    DayTypeAssignment dayTypeAssignment2 = new DayTypeAssignment();
    dayTypeAssignment2.setAvailable(true);
    dayTypeAssignment2.setDate(LocalDate.of(2022, 5, 4));

    DayType dayType = new DayType();
    dayType.setDayTypeAssignments(List.of(dayTypeAssignment1, dayTypeAssignment2));

    ExportedDayTypeStatistics exportedDayTypeStatisticsForDayType =
      ExportedLineStatisticsService.getExportedDayTypeStatisticsForDayType(dayType);

    assertEquals(
      LocalDate.of(2022, 1, 1),
      exportedDayTypeStatisticsForDayType.getOperatingPeriodFrom()
    );
    assertEquals(
      LocalDate.of(2022, 5, 4),
      exportedDayTypeStatisticsForDayType.getOperatingPeriodTo()
    );
  }

  @Test
  public void dayTypeAssignmentsWithoutDateOrOperatingPeriodShouldBeIgnored() {
    OperatingPeriod operatingPeriod1 = new OperatingPeriod();
    operatingPeriod1.setFromDate(LocalDate.of(2022, 1, 1));
    operatingPeriod1.setToDate(LocalDate.of(2022, 3, 4));

    OperatingPeriod operatingPeriod2 = new OperatingPeriod();
    operatingPeriod2.setFromDate(LocalDate.of(2022, 5, 4));
    operatingPeriod2.setToDate(LocalDate.of(2022, 8, 5));

    DayTypeAssignment dayTypeAssignment1 = new DayTypeAssignment();
    dayTypeAssignment1.setAvailable(true);
    dayTypeAssignment1.setOperatingPeriod(operatingPeriod1);

    DayTypeAssignment dayTypeAssignment2 = new DayTypeAssignment();
    dayTypeAssignment2.setAvailable(true);

    DayTypeAssignment dayTypeAssignment3 = new DayTypeAssignment();
    dayTypeAssignment3.setAvailable(true);
    dayTypeAssignment3.setOperatingPeriod(operatingPeriod2);

    DayType dayType = new DayType();
    dayType.setDayTypeAssignments(
      List.of(dayTypeAssignment1, dayTypeAssignment2, dayTypeAssignment3)
    );

    ExportedDayTypeStatistics exportedDayTypeStatisticsForDayType =
      ExportedLineStatisticsService.getExportedDayTypeStatisticsForDayType(dayType);

    assertEquals(
      LocalDate.of(2022, 1, 1),
      exportedDayTypeStatisticsForDayType.getOperatingPeriodFrom()
    );
    assertEquals(
      LocalDate.of(2022, 8, 5),
      exportedDayTypeStatisticsForDayType.getOperatingPeriodTo()
    );
  }

  @Test
  public void lineToExportedDayTypeStatisticsDoneRight() {
    Line line = new FlexibleLine();
    OperatingPeriod operatingPeriod1 = new OperatingPeriod();
    operatingPeriod1.setFromDate(LocalDate.of(2022, 3, 1));
    operatingPeriod1.setToDate(LocalDate.of(2022, 3, 7));
    OperatingPeriod operatingPeriod2 = new OperatingPeriod();
    operatingPeriod2.setFromDate(LocalDate.of(2022, 3, 10));
    operatingPeriod2.setToDate(LocalDate.of(2022, 3, 15));
    line.setJourneyPatterns(
      List.of(
        createJourneyPatternForGivenOperatingPeriods(operatingPeriod1, operatingPeriod2)
      )
    );

    List<ExportedDayTypeStatistics> exportedDayTypeStatistics =
      ExportedLineStatisticsService.calculateExportedDayTypesStatisticsForLine(line);

    assertEquals(2, exportedDayTypeStatistics.size());
    assertEquals(
      operatingPeriod1.getFromDate(),
      exportedDayTypeStatistics.get(0).getOperatingPeriodFrom()
    );
    assertEquals(
      operatingPeriod1.getToDate(),
      exportedDayTypeStatistics.get(0).getOperatingPeriodTo()
    );

    assertEquals(
      operatingPeriod2.getFromDate(),
      exportedDayTypeStatistics.get(1).getOperatingPeriodFrom()
    );
    assertEquals(
      operatingPeriod2.getToDate(),
      exportedDayTypeStatistics.get(1).getOperatingPeriodTo()
    );
  }

  @Test
  public void lineToExportedLineStatisticsDoneRight() {
    Line line = new FlexibleLine();
    OperatingPeriod operatingPeriod1 = new OperatingPeriod();
    operatingPeriod1.setFromDate(LocalDate.of(2022, 3, 1));
    operatingPeriod1.setToDate(LocalDate.of(2022, 3, 7));
    OperatingPeriod operatingPeriod2 = new OperatingPeriod();
    operatingPeriod2.setFromDate(LocalDate.of(2022, 3, 10));
    operatingPeriod2.setToDate(LocalDate.of(2022, 3, 15));
    line.setJourneyPatterns(
      List.of(
        createJourneyPatternForGivenOperatingPeriods(operatingPeriod1, operatingPeriod2)
      )
    );

    ExportedLineStatistics exportedLineStatistics =
      ExportedLineStatisticsService.toExportedLineStatistics(line);

    assertEquals(
      operatingPeriod1.getFromDate(),
      exportedLineStatistics.getOperatingPeriodFrom()
    );
    assertEquals(
      operatingPeriod2.getToDate(),
      exportedLineStatistics.getOperatingPeriodTo()
    );
  }

  private JourneyPattern createJourneyPatternForGivenOperatingPeriods(
    OperatingPeriod... operatingPeriods
  ) {
    JourneyPattern journeyPattern = new JourneyPattern();
    journeyPattern.setServiceJourneys(
      Stream
        .of(operatingPeriods)
        .map(operatingPeriod -> {
          ServiceJourney serviceJourney = new ServiceJourney();
          DayType dayType = new DayType();
          DayTypeAssignment dayTypeAssignment = new DayTypeAssignment();
          dayTypeAssignment.setOperatingPeriod(operatingPeriod);
          dayType.setDayTypeAssignments(List.of(dayTypeAssignment));
          serviceJourney.updateDayTypes(List.of(dayType));
          return serviceJourney;
        })
        .collect(Collectors.toList())
    );
    return journeyPattern;
  }
}
