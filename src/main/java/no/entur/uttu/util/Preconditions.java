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

package no.entur.uttu.util;

import org.checkerframework.checker.nullness.qual.Nullable;
import no.entur.uttu.error.CodedIllegalArgumentException;
import no.entur.uttu.error.ErrorCodeEnumeration;

public final class Preconditions {
    public static void checkArgument(boolean expression, @Nullable Object errorMessage, ErrorCodeEnumeration errorCode) {
        if (!expression) {
            throw new CodedIllegalArgumentException(
                    String.valueOf(errorMessage),
                    errorCode
            );
        }
    }

    public static void checkArgument(boolean expression, @Nullable String errorMessageTemplate, @Nullable Object... errorMessageArgs) {
        com.google.common.base.Preconditions.checkArgument(expression, errorMessageTemplate, errorMessageArgs);
    }
}
