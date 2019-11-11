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

package no.entur.uttu.error.codedexception;

import no.entur.uttu.error.ErrorCode;
import no.entur.uttu.error.SubCode;
import no.entur.uttu.error.codederror.CodedError;

import java.util.Map;

public class CodedIllegalArgumentException extends IllegalArgumentException implements CodedException {
    private final ErrorCode code;
    private final SubCode subCode;
    private final Map<String, Object> metadata;

    public CodedIllegalArgumentException(String message, CodedError codedError) {
        super(message);
        this.code = codedError.getErrorCode();
        this.subCode = codedError.getSubCode();
        this.metadata = codedError.getMetadata();
    }

    public ErrorCode getCode() {
        return code;
    }

    public SubCode getSubCode() {
        return subCode;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }
}
