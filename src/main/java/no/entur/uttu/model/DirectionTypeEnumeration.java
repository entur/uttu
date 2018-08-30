package no.entur.uttu.model;

public enum DirectionTypeEnumeration {

    INBOUND("inbound"),
    OUTBOUND("outbound"),
    CLOCKWISE("clockwise"),
    ANTICLOCKWISE("anticlockwise");

    private final String value;

    DirectionTypeEnumeration(String v) {
        this.value = v;
    }

    public String value() {
        return this.value;
    }

    public static DirectionTypeEnumeration fromValue(String v) {
        DirectionTypeEnumeration[] var1 = values();
        int var2 = var1.length;

        for (int var3 = 0; var3 < var2; ++var3) {
            DirectionTypeEnumeration c = var1[var3];
            if (c.value.equals(v)) {
                return c;
            }
        }

        throw new IllegalArgumentException(v);
    }
}
