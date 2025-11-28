package no.entur.uttu.export.netex.producer;

import static org.junit.jupiter.api.Assertions.*;

import java.util.stream.Stream;
import no.entur.uttu.config.AdditionalCodespacesConfig;
import no.entur.uttu.config.ExportTimeZone;
import no.entur.uttu.model.Ref;
import no.entur.uttu.model.VehicleModeEnumeration;
import no.entur.uttu.model.VehicleSubmodeEnumeration;
import no.entur.uttu.util.DateUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.rutebanken.netex.model.*;

public class NetexObjectFactoryTest {

  private NetexObjectFactory factory;

  @BeforeEach
  void setUp() {
    factory = new NetexObjectFactory(
      new DateUtils(),
      new ExportTimeZone(),
      new AdditionalCodespacesConfig()
    );
  }

  @Test
  void populateIdReturnsValidIdForVersionedChildStructureElements() {
    assertEquals(
      "ENT:ServiceLinkInJourneyPattern:1",
      factory
        .populateId(
          new ServiceLinkInJourneyPattern_VersionedChildStructure(),
          new Ref("ENT:ServiceLink:1", "1")
        )
        .getId()
    );
  }

  @Test
  void mapTransportSubmodeStructureReturnsNullForNullInput() {
    assertNull(factory.mapTransportSubmodeStructure(null));
  }

  @ParameterizedTest
  @EnumSource(VehicleSubmodeEnumeration.class)
  void mapTransportSubmodeStructureReturnsNonNullForAllSubmodes(
    VehicleSubmodeEnumeration submode
  ) {
    TransportSubmodeStructure result = factory.mapTransportSubmodeStructure(submode);
    assertNotNull(result);
  }

  static Stream<Arguments> submodeToExpectedMappingProvider() {
    return Stream.of(
      // Air submodes
      Arguments.of(
        VehicleSubmodeEnumeration.DOMESTIC_FLIGHT,
        VehicleModeEnumeration.AIR,
        AirSubmodeEnumeration.DOMESTIC_FLIGHT
      ),
      Arguments.of(
        VehicleSubmodeEnumeration.HELICOPTER_SERVICE,
        VehicleModeEnumeration.AIR,
        AirSubmodeEnumeration.HELICOPTER_SERVICE
      ),
      // Bus submodes
      Arguments.of(
        VehicleSubmodeEnumeration.LOCAL_BUS,
        VehicleModeEnumeration.BUS,
        BusSubmodeEnumeration.LOCAL_BUS
      ),
      Arguments.of(
        VehicleSubmodeEnumeration.AIRPORT_LINK_BUS,
        VehicleModeEnumeration.BUS,
        BusSubmodeEnumeration.AIRPORT_LINK_BUS
      ),
      // Coach submodes
      Arguments.of(
        VehicleSubmodeEnumeration.INTERNATIONAL_COACH,
        VehicleModeEnumeration.COACH,
        CoachSubmodeEnumeration.INTERNATIONAL_COACH
      ),
      Arguments.of(
        VehicleSubmodeEnumeration.TOURIST_COACH,
        VehicleModeEnumeration.COACH,
        CoachSubmodeEnumeration.TOURIST_COACH
      ),
      // Tram submodes
      Arguments.of(
        VehicleSubmodeEnumeration.CITY_TRAM,
        VehicleModeEnumeration.TRAM,
        TramSubmodeEnumeration.CITY_TRAM
      ),
      Arguments.of(
        VehicleSubmodeEnumeration.LOCAL_TRAM,
        VehicleModeEnumeration.TRAM,
        TramSubmodeEnumeration.LOCAL_TRAM
      ),
      // Water submodes
      Arguments.of(
        VehicleSubmodeEnumeration.LOCAL_CAR_FERRY,
        VehicleModeEnumeration.WATER,
        WaterSubmodeEnumeration.LOCAL_CAR_FERRY
      ),
      Arguments.of(
        VehicleSubmodeEnumeration.HIGH_SPEED_PASSENGER_SERVICE,
        VehicleModeEnumeration.WATER,
        WaterSubmodeEnumeration.HIGH_SPEED_PASSENGER_SERVICE
      ),
      // Rail submodes
      Arguments.of(
        VehicleSubmodeEnumeration.LOCAL,
        VehicleModeEnumeration.RAIL,
        RailSubmodeEnumeration.LOCAL
      ),
      Arguments.of(
        VehicleSubmodeEnumeration.LONG_DISTANCE,
        VehicleModeEnumeration.RAIL,
        RailSubmodeEnumeration.LONG_DISTANCE
      ),
      // Funicular submodes
      Arguments.of(
        VehicleSubmodeEnumeration.FUNICULAR,
        VehicleModeEnumeration.FUNICULAR,
        FunicularSubmodeEnumeration.FUNICULAR
      ),
      // Metro submodes
      Arguments.of(
        VehicleSubmodeEnumeration.METRO,
        VehicleModeEnumeration.METRO,
        MetroSubmodeEnumeration.METRO
      ),
      Arguments.of(
        VehicleSubmodeEnumeration.URBAN_RAILWAY,
        VehicleModeEnumeration.METRO,
        MetroSubmodeEnumeration.URBAN_RAILWAY
      ),
      // Cableway/Telecabin submodes
      Arguments.of(
        VehicleSubmodeEnumeration.TELECABIN,
        VehicleModeEnumeration.CABLEWAY,
        TelecabinSubmodeEnumeration.TELECABIN
      ),
      Arguments.of(
        VehicleSubmodeEnumeration.CHAIR_LIFT,
        VehicleModeEnumeration.CABLEWAY,
        TelecabinSubmodeEnumeration.CHAIR_LIFT
      ),
      // Taxi submodes
      Arguments.of(
        VehicleSubmodeEnumeration.CHARTER_TAXI,
        VehicleModeEnumeration.TAXI,
        TaxiSubmodeEnumeration.CHARTER_TAXI
      ),
      Arguments.of(
        VehicleSubmodeEnumeration.COMMUNAL_TAXI,
        VehicleModeEnumeration.TAXI,
        TaxiSubmodeEnumeration.COMMUNAL_TAXI
      ),
      // Snow and ice submodes
      Arguments.of(
        VehicleSubmodeEnumeration.SNOW_COACH,
        VehicleModeEnumeration.SNOW_AND_ICE,
        SnowAndIceSubmodeEnumeration.SNOW_COACH
      )
    );
  }

  @ParameterizedTest
  @MethodSource("submodeToExpectedMappingProvider")
  void mapTransportSubmodeStructureMapsToCorrectSubmodeField(
    VehicleSubmodeEnumeration inputSubmode,
    VehicleModeEnumeration expectedMode,
    Enum<?> expectedNetexSubmode
  ) {
    TransportSubmodeStructure result = factory.mapTransportSubmodeStructure(inputSubmode);

    assertNotNull(result);
    assertEquals(expectedMode, inputSubmode.getVehicleMode());

    switch (expectedMode) {
      case AIR -> {
        assertEquals(expectedNetexSubmode, result.getAirSubmode());
        assertOtherSubmodesAreNull(result, "air");
      }
      case BUS, TROLLEY_BUS -> {
        assertEquals(expectedNetexSubmode, result.getBusSubmode());
        assertOtherSubmodesAreNull(result, "bus");
      }
      case COACH -> {
        assertEquals(expectedNetexSubmode, result.getCoachSubmode());
        assertOtherSubmodesAreNull(result, "coach");
      }
      case TRAM -> {
        assertEquals(expectedNetexSubmode, result.getTramSubmode());
        assertOtherSubmodesAreNull(result, "tram");
      }
      case WATER -> {
        assertEquals(expectedNetexSubmode, result.getWaterSubmode());
        assertOtherSubmodesAreNull(result, "water");
      }
      case RAIL -> {
        assertEquals(expectedNetexSubmode, result.getRailSubmode());
        assertOtherSubmodesAreNull(result, "rail");
      }
      case FUNICULAR -> {
        assertEquals(expectedNetexSubmode, result.getFunicularSubmode());
        assertOtherSubmodesAreNull(result, "funicular");
      }
      case METRO -> {
        assertEquals(expectedNetexSubmode, result.getMetroSubmode());
        assertOtherSubmodesAreNull(result, "metro");
      }
      case CABLEWAY -> {
        assertEquals(expectedNetexSubmode, result.getTelecabinSubmode());
        assertOtherSubmodesAreNull(result, "telecabin");
      }
      case TAXI -> {
        assertEquals(expectedNetexSubmode, result.getTaxiSubmode());
        assertOtherSubmodesAreNull(result, "taxi");
      }
      case SNOW_AND_ICE -> {
        assertEquals(expectedNetexSubmode, result.getSnowAndIceSubmode());
        assertOtherSubmodesAreNull(result, "snowAndIce");
      }
      default -> fail("Unexpected vehicle mode: " + expectedMode);
    }
  }

  private void assertOtherSubmodesAreNull(
    TransportSubmodeStructure result,
    String expectedNonNullSubmode
  ) {
    if (!"air".equals(expectedNonNullSubmode)) {
      assertNull(result.getAirSubmode(), "AirSubmode should be null");
    }
    if (!"bus".equals(expectedNonNullSubmode)) {
      assertNull(result.getBusSubmode(), "BusSubmode should be null");
    }
    if (!"coach".equals(expectedNonNullSubmode)) {
      assertNull(result.getCoachSubmode(), "CoachSubmode should be null");
    }
    if (!"tram".equals(expectedNonNullSubmode)) {
      assertNull(result.getTramSubmode(), "TramSubmode should be null");
    }
    if (!"water".equals(expectedNonNullSubmode)) {
      assertNull(result.getWaterSubmode(), "WaterSubmode should be null");
    }
    if (!"rail".equals(expectedNonNullSubmode)) {
      assertNull(result.getRailSubmode(), "RailSubmode should be null");
    }
    if (!"funicular".equals(expectedNonNullSubmode)) {
      assertNull(result.getFunicularSubmode(), "FunicularSubmode should be null");
    }
    if (!"metro".equals(expectedNonNullSubmode)) {
      assertNull(result.getMetroSubmode(), "MetroSubmode should be null");
    }
    if (!"telecabin".equals(expectedNonNullSubmode)) {
      assertNull(result.getTelecabinSubmode(), "TelecabinSubmode should be null");
    }
    if (!"taxi".equals(expectedNonNullSubmode)) {
      assertNull(result.getTaxiSubmode(), "TaxiSubmode should be null");
    }
    if (!"snowAndIce".equals(expectedNonNullSubmode)) {
      assertNull(result.getSnowAndIceSubmode(), "SnowAndIceSubmode should be null");
    }
  }
}
