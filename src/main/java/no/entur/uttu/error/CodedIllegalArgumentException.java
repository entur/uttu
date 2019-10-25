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

package no.entur.uttu.error;

import java.util.Map;

public class CodedIllegalArgumentException extends IllegalArgumentException implements CodedException {
    private final ErrorCodeEnumeration code;
    private final Map<String, Object> metadata;

    public CodedIllegalArgumentException(String message, ErrorCodeEnumeration code, Map<String, Object> metadata) {
        super(message);
        this.code = code;
        this.metadata = metadata;
    }

    public ErrorCodeEnumeration getCode() {
        return code;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }
}
