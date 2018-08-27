package no.entur.uttu.model;

public enum BookingMethodEnumeration {
    CALL_DRIVER("callDriver"),
    CALL_OFFICE("callOffice"),
    ONLINE("online"),
    PHONE_AT_STOP("phoneAtStop"),
    TEXT("text");

    private final String value;

    BookingMethodEnumeration(String v) {
        this.value = v;
    }

    public String value() {
        return this.value;
    }

    public static BookingMethodEnumeration fromValue(String v) {
        BookingMethodEnumeration[] var1 = values();
        int var2 = var1.length;

        for (int var3 = 0; var3 < var2; ++var3) {
            BookingMethodEnumeration c = var1[var3];
            if (c.value.equals(v)) {
                return c;
            }
        }

        throw new IllegalArgumentException(v);
    }
}
