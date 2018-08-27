package no.entur.uttu.model;

public enum PurchaseMomentEnumeration {
    ON_RESERVATION("onReservation"),
    BEFORE_BOARDING("beforeBoarding"),
    AFTER_BOARDING("afterBoarding"),
    ON_CHECK_OUT("onCheckOut");

    private final String value;

    PurchaseMomentEnumeration(String v) {
        this.value = v;
    }

    public String value() {
        return this.value;
    }

    public static PurchaseMomentEnumeration fromValue(String v) {
        PurchaseMomentEnumeration[] var1 = values();
        int var2 = var1.length;

        for (int var3 = 0; var3 < var2; ++var3) {
            PurchaseMomentEnumeration c = var1[var3];
            if (c.value.equals(v)) {
                return c;
            }
        }

        throw new IllegalArgumentException(v);
    }
}