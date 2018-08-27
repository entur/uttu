package no.entur.uttu.model;

public enum PurchaseWhenEnumeration {
    TIME_OF_TRAVEL_ONLY("timeOfTravelOnly"),
    DAY_OF_TRAVEL_ONLY("dayOfTravelOnly"),
    UNTIL_PREVIOUS_DAY("untilPreviousDay"),
    ADVANCE_AND_DAY_OF_TRAVEL("advanceAndDayOfTravel");

    private final String value;

    PurchaseWhenEnumeration(String v) {
        this.value = v;
    }

    public String value() {
        return this.value;
    }

    public static PurchaseWhenEnumeration fromValue(String v) {
        PurchaseWhenEnumeration[] var1 = values();
        int var2 = var1.length;

        for (int var3 = 0; var3 < var2; ++var3) {
            PurchaseWhenEnumeration c = var1[var3];
            if (c.value.equals(v)) {
                return c;
            }
        }

        throw new IllegalArgumentException(v);
    }
}
