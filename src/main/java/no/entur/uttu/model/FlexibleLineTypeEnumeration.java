package no.entur.uttu.model;

import javax.xml.bind.annotation.XmlEnumValue;

public enum FlexibleLineTypeEnumeration {
    @XmlEnumValue("corridorService")
    CORRIDOR_SERVICE("corridorService"),
    @XmlEnumValue("mainRouteWithFlexibleEnds")
    MAIN_ROUTE_WITH_FLEXIBLE_ENDS("mainRouteWithFlexibleEnds"),
    @XmlEnumValue("flexibleAreasOnly")
    FLEXIBLE_AREAS_ONLY("flexibleAreasOnly"),
    @XmlEnumValue("hailAndRideSections")
    HAIL_AND_RIDE_SECTIONS("hailAndRideSections"),
    @XmlEnumValue("fixedStopAreaWide")
    FIXED_STOP_AREA_WIDE("fixedStopAreaWide"),
    @XmlEnumValue("freeAreaAreaWide")
    FREE_AREA_AREA_WIDE("freeAreaAreaWide"),
    @XmlEnumValue("mixedFlexible")
    MIXED_FLEXIBLE("mixedFlexible"),
    @XmlEnumValue("mixedFlexibleAndFixed")
    MIXED_FLEXIBLE_AND_FIXED("mixedFlexibleAndFixed"),
    @XmlEnumValue("fixed")
    FIXED("fixed"),
    @XmlEnumValue("other")
    OTHER("other");

    private final String value;

    FlexibleLineTypeEnumeration(String v) {
        this.value = v;
    }

    public static FlexibleLineTypeEnumeration fromValue(String v) {
        FlexibleLineTypeEnumeration[] var1 = values();
        int var2 = var1.length;

        for (int var3 = 0; var3 < var2; ++var3) {
            FlexibleLineTypeEnumeration c = var1[var3];
            if (c.value.equals(v)) {
                return c;
            }
        }

        throw new IllegalArgumentException(v);
    }

    public String value() {
        return value;
    }
}
