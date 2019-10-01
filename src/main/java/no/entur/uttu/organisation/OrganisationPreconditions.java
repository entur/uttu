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

package no.entur.uttu.organisation;

import com.google.common.base.Strings;
import no.entur.uttu.util.CodedIllegalArgumentException;

public final class OrganisationPreconditions {

    public static final String INVALID_OPERATOR = "INVALID_OPERATOR";

    public static void checkValidOperator(Organisation organisation, String authorityRef) {
        if (organisation.getOperatorNetexId() == null) {
            throw new CodedIllegalArgumentException(
                    Strings.lenientFormat("Organisation with ref %s is not a valid operator", authorityRef),
                    INVALID_OPERATOR
            );
        }
    }
}
