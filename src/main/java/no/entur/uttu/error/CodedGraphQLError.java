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

import graphql.ExceptionWhileDataFetching;
import graphql.execution.ResultPath;
import no.entur.uttu.error.codedexception.CodedException;

import java.util.HashMap;
import java.util.Map;

public class CodedGraphQLError extends ExceptionWhileDataFetching {
    public CodedGraphQLError(ExceptionWhileDataFetching inner) {
        super(ResultPath.fromList(inner.getPath()), inner.getException(), inner.getLocations().get(0));
    }

    @Override
    public Map<String, Object> getExtensions() {
        Map<String, Object> extensions = super.getExtensions();
        if (extensions == null) {
            extensions = new HashMap<>();
        }

        if (this.getException() instanceof CodedException) {
            extensions.put("code", ((CodedException) this.getException()).getCode());
            extensions.put("subCode", ((CodedException) this.getException()).getSubCode());
            extensions.put("metadata", ((CodedException) this.getException()).getMetadata());
        }

        return extensions;
    }
}
