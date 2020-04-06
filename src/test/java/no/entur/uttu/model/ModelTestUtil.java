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

import no.entur.uttu.error.ErrorCode;
import no.entur.uttu.error.codedexception.CodedIllegalArgumentException;
import no.entur.uttu.error.codes.ErrorCodeEnumeration;
import org.junit.Assert;

import static org.junit.Assert.assertEquals;

public class ModelTestUtil
{


    public static void assertCheckPersistableFails(IdentifiedEntity entity) {
        try {
            entity.checkPersistable();
            Assert.fail("Expected exception for non-persistable entity");
        } catch (IllegalArgumentException iae) {
            //  OK
        }
    }

    public static void assertCheckPersistableFailsWithErrorCode(IdentifiedEntity entity, ErrorCodeEnumeration code) {
        try {
            entity.checkPersistable();
            Assert.fail("Expected exception for non-persistable entity");
        } catch (CodedIllegalArgumentException iae) {
            assertEquals(code.toString(), iae.getCode());
        }
    }
}
