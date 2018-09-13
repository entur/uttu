package no.entur.uttu.model;

public enum VehicleSubmodeEnumeration {

    // Air
    INTERNATIONAL_FLIGHT("internationalFlight", VehicleModeEnumeration.AIR),
    DOMESTIC_FLIGHT("domesticFlight", VehicleModeEnumeration.AIR),
    HELICOPTER_SERVICE("helicopterService", VehicleModeEnumeration.AIR),
    INTERCONTINENTAL_CHARTER_FLIGHT("intercontinentalCharterFlight", VehicleModeEnumeration.AIR),
    INTERNATIONAL_CHARTER_FLIGHT("internationalCharterFlight", VehicleModeEnumeration.AIR),
    ROUND_TRIP_CHARTER_FLIGHT("roundTripCharterFlight", VehicleModeEnumeration.AIR),
    SIGHTSEEING_FLIGHT("sightseeingFlight", VehicleModeEnumeration.AIR),

    DOMESTIC_CHARTER_FLIGHT("domesticCharterFlight", VehicleModeEnumeration.AIR),
    SCHENGEN_AREA_FLIGHT("SchengenAreaFlight", VehicleModeEnumeration.AIR),
    AIRSHIP_SERVICE("airshipService", VehicleModeEnumeration.AIR),
    SHORT_HAUL_INTERNATIONAL_FLIGHT("shortHaulInternationalFlight", VehicleModeEnumeration.AIR),
    // Bus
    LOCAL_BUS("localBus", VehicleModeEnumeration.BUS),
    REGIONAL_BUS("regionalBus", VehicleModeEnumeration.BUS),
    EXPRESS_BUS("expressBus", VehicleModeEnumeration.BUS),
    NIGHT_BUS("nightBus", VehicleModeEnumeration.BUS),
    POST_BUS("postBus", VehicleModeEnumeration.BUS),
    SPECIAL_NEEDS_BUS("specialNeedsBus", VehicleModeEnumeration.BUS),
    MOBILITY_BUS("mobilityBus", VehicleModeEnumeration.BUS),
    MOBILITY_BUS_FOR_REGISTERED_DISABLED("mobilityBusForRegisteredDisabled", VehicleModeEnumeration.BUS),
    SIGHTSEEING_BUS("sightseeingBus", VehicleModeEnumeration.BUS),
    SHUTTLE_BUS("shuttleBus", VehicleModeEnumeration.BUS),
    HIGH_FREQUENCY_BUS("highFrequencyBus", VehicleModeEnumeration.BUS),
    DEDICATED_LANE_BUS("dedicatedLaneBus", VehicleModeEnumeration.BUS),
    SCHOOL_BUS("schoolBus", VehicleModeEnumeration.BUS),
    SCHOOL_AND_PUBLIC_SERVICE_BUS("schoolAndPublicServiceBus", VehicleModeEnumeration.BUS),
    RAIL_REPLACEMENT_BUS("railReplacementBus", VehicleModeEnumeration.BUS),
    DEMAND_AND_RESPONSE_BUS("demandAndResponseBus", VehicleModeEnumeration.BUS),
    AIRPORT_LINK_BUS("airportLinkBus", VehicleModeEnumeration.BUS),
    // Coach
    INTERNATIONAL_COACH("internationalCoach", VehicleModeEnumeration.COACH),
    NATIONAL_COACH("nationalCoach", VehicleModeEnumeration.COACH),
    SHUTTLE_COACH("shuttleCoach", VehicleModeEnumeration.COACH),
    REGIONAL_COACH("regionalCoach", VehicleModeEnumeration.COACH),
    SPECIAL_COACH("specialCoach", VehicleModeEnumeration.COACH),
    SCHOOL_COACH("schoolCoach", VehicleModeEnumeration.COACH),
    SIGHTSEEING_COACH("sightseeingCoach", VehicleModeEnumeration.COACH),
    TOURIST_COACH("touristCoach", VehicleModeEnumeration.COACH),
    COMMUTER_COACH("commuterCoach", VehicleModeEnumeration.COACH),
    // Funicular
    FUNICULAR("funicular", VehicleModeEnumeration.FUNICULAR),
    STREET_CABLE_CAR("streetCableCar", VehicleModeEnumeration.FUNICULAR),
    ALL_FUNICULAR_SERVICES("allFunicularServices", VehicleModeEnumeration.FUNICULAR),
    UNDEFINED_FUNICULAR("undefinedFunicular", VehicleModeEnumeration.FUNICULAR),
    // Metro
    METRO("metro", VehicleModeEnumeration.METRO),
    TUBE("tube", VehicleModeEnumeration.METRO),
    URBAN_RAILWAY("urbanRailway", VehicleModeEnumeration.METRO),
    // Tram
    CITY_TRAM("cityTram", VehicleModeEnumeration.TRAM),
    LOCAL_TRAM("localTram", VehicleModeEnumeration.TRAM),
    REGIONAL_TRAM("regionalTram", VehicleModeEnumeration.TRAM),
    SIGHTSEEING_TRAM("sightseeingTram", VehicleModeEnumeration.TRAM),
    SHUTTLE_TRAM("shuttleTram", VehicleModeEnumeration.TRAM),
    TRAIN_TRAM("trainTram", VehicleModeEnumeration.TRAM),
    // Telecabin
    TELECABIN("telecabin", VehicleModeEnumeration.CABLEWAY),
    CABLE_CAR("cableCar", VehicleModeEnumeration.CABLEWAY),
    LIFT("lift", VehicleModeEnumeration.CABLEWAY),
    CHAIR_LIFT("chairLift", VehicleModeEnumeration.CABLEWAY),
    DRAG_LIFT("dragLift", VehicleModeEnumeration.CABLEWAY),
    TELECABIN_LINK("telecabinLink", VehicleModeEnumeration.CABLEWAY),
    // Rail
    LOCAL("local", VehicleModeEnumeration.RAIL),
    HIGH_SPEED_RAIL("highSpeedRail", VehicleModeEnumeration.RAIL),
    SUBURBAN_RAILWAY("suburbanRailway", VehicleModeEnumeration.RAIL),
    REGIONAL_RAIL("regionalRail", VehicleModeEnumeration.RAIL),
    INTERREGIONAL_RAIL("interregionalRail", VehicleModeEnumeration.RAIL),
    LONG_DISTANCE("longDistance", VehicleModeEnumeration.RAIL),
    INTERNATIONAL("international", VehicleModeEnumeration.RAIL),
    SLEEPER_RAIL_SERVICE("sleeperRailService", VehicleModeEnumeration.RAIL),
    NIGHT_RAIL("nightRail", VehicleModeEnumeration.RAIL),
    CAR_TRANSPORT_RAIL_SERVICE("carTransportRailService", VehicleModeEnumeration.RAIL),
    TOURIST_RAILWAY("touristRailway", VehicleModeEnumeration.RAIL),
    AIRPORT_LINK_RAIL("airportLinkRail", VehicleModeEnumeration.RAIL),
    RAIL_SHUTTLE("railShuttle", VehicleModeEnumeration.RAIL),
    REPLACEMENT_RAIL_SERVICE("replacementRailService", VehicleModeEnumeration.RAIL),
    SPECIAL_TRAIN("specialTrain", VehicleModeEnumeration.RAIL),
    CROSS_COUNTRY_RAIL("crossCountryRail", VehicleModeEnumeration.RAIL),
    RACK_AND_PINION_RAILWAY("rackAndPinionRailway", VehicleModeEnumeration.RAIL),
    // Water
    INTERNATIONAL_CAR_FERRY("internationalCarFerry", VehicleModeEnumeration.WATER),
    NATIONAL_CAR_FERRY("nationalCarFerry", VehicleModeEnumeration.WATER),
    REGIONAL_CAR_FERRY("regionalCarFerry", VehicleModeEnumeration.WATER),
    LOCAL_CAR_FERRY("localCarFerry", VehicleModeEnumeration.WATER),
    INTERNATIONAL_PASSENGER_FERRY("internationalPassengerFerry", VehicleModeEnumeration.WATER),
    NATIONAL_PASSENGER_FERRY("nationalPassengerFerry", VehicleModeEnumeration.WATER),
    REGIONAL_PASSENGER_FERRY("regionalPassengerFerry", VehicleModeEnumeration.WATER),
    LOCAL_PASSENGER_FERRY("localPassengerFerry", VehicleModeEnumeration.WATER),
    POST_BOAT("postBoat", VehicleModeEnumeration.WATER),
    TRAIN_FERRY("trainFerry", VehicleModeEnumeration.WATER),
    ROAD_FERRY_LINK("roadFerryLink", VehicleModeEnumeration.WATER),
    AIRPORT_BOAT_LINK("airportBoatLink", VehicleModeEnumeration.WATER),
    HIGH_SPEED_VEHICLE_SERVICE("highSpeedVehicleService", VehicleModeEnumeration.WATER),
    HIGH_SPEED_PASSENGER_SERVICE("highSpeedPassengerService", VehicleModeEnumeration.WATER),
    SIGHTSEEING_SERVICE("sightseeingService", VehicleModeEnumeration.WATER),
    SCHOOL_BOAT("schoolBoat", VehicleModeEnumeration.WATER),
    CABLE_FERRY("cableFerry", VehicleModeEnumeration.WATER),
    RIVER_BUS("riverBus", VehicleModeEnumeration.WATER),
    SCHEDULED_FERRY("scheduledFerry", VehicleModeEnumeration.WATER),
    SHUTTLE_FERRY_SERVICE("shuttleFerryService", VehicleModeEnumeration.WATER);

    private final String value;

    private final VehicleModeEnumeration vehicleMode;

    VehicleSubmodeEnumeration(String v, VehicleModeEnumeration vehicleMode) {
        value = v;
        this.vehicleMode = vehicleMode;
    }

    public static VehicleSubmodeEnumeration fromValue(String v) {
        for (VehicleSubmodeEnumeration c : VehicleSubmodeEnumeration.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

    public String value() {
        return value;
    }

    public VehicleModeEnumeration getVehicleMode() {
        return vehicleMode;
    }
}
