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