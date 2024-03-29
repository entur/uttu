/*
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *   https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */

package no.entur.uttu.model;

import jakarta.xml.bind.annotation.XmlEnumValue;

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
