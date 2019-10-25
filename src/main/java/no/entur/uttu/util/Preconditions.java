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

import com.google.common.base.Strings;
import no.entur.uttu.error.codederror.CodedError;
import org.checkerframework.checker.nullness.qual.Nullable;
import no.entur.uttu.error.codedexception.CodedIllegalArgumentException;

public final class Preconditions {
    public static void checkArgument(boolean expression, CodedError codedError, @Nullable String errorMessageTemplate, @Nullable Object... errorMessageArgs) {
        if (!expression) {
            throw new CodedIllegalArgumentException(
                    Strings.lenientFormat(errorMessageTemplate, errorMessageArgs),
                    codedError
            );
        }
    }

    public static void checkArgument(boolean expression, @Nullable String errorMessageTemplate, @Nullable Object... errorMessageArgs) {
        com.google.common.base.Preconditions.checkArgument(expression, errorMessageTemplate, errorMessageArgs);
    }
}
