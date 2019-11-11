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

package no.entur.uttu.graphql.resource;

import no.entur.uttu.error.codederror.CodedError;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@XmlRootElement
public class ErrorResponseEntity {

    public ErrorResponseEntity() {
    }

    public ErrorResponseEntity(String message) {
        errors.add(new Error(message));
    }

    public ErrorResponseEntity(String message, CodedError codedError) {
        errors.add(
            new Error(message,
                    Map.of("code", codedError.getErrorCode(),
                            "metadata", codedError.getMetadata()
                    )
            )
        );
    }

    public List<Error> errors = new ArrayList<>();


    public static class Error {
        public String message;
        public Map<String, Object> extensions;

        public Error(String message) {
            this.message = message;
        }

        public Error(String message, Map<String, Object> extensions) {
            this.message = message;
            this.extensions = extensions;
        }
    }
}
