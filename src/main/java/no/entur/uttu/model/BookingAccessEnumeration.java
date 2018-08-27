package no.entur.uttu.model;

public enum BookingAccessEnumeration {

    PUBLIC("public"),
    AUTHORISED_PUBLIC("authorisedPublic"),
    STAFF("staff");

    private final String value;

    BookingAccessEnumeration(String v) {
        this.value = v;
    }

    public String value() {
        return this.value;
    }

    public static BookingAccessEnumeration fromValue(String v) {
        BookingAccessEnumeration[] var1 = values();
        int var2 = var1.length;

        for (int var3 = 0; var3 < var2; ++var3) {
            BookingAccessEnumeration c = var1[var3];
            if (c.value.equals(v)) {
                return c;
            }
        }

        throw new IllegalArgumentException(v);
    }
}

