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

package no.entur.uttu.stopplace;

import com.google.common.base.Preconditions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class StopPlaceRegistryImpl implements StopPlaceRegistry {

    private RestTemplate restTemplate = new RestTemplate();


    private static final String ET_CLIENT_ID_HEADER = "ET-Client-ID";
    private static final String ET_CLIENT_NAME_HEADER = "ET-Client-Name";

    private static final String SUCCESS_MATCHER = "\"id\"";
    @Value("${http.client.name:uttu}")
    private String clientName;

    @Value("${http.client.id:uttu}")
    private String clientId;

    @Value("${stopplace.registry.url:https://api.entur.org/stop_places/1.0/graphql}")
    private String stopPlaceRegistryUrl = "https://api.entur.org/stop_places/1.0/graphql";


    public boolean isValidQuayRef(String quayRef) {
        if (quayRef == null) {
            return false;
        }
        if (!quayRef.contains(":Quay:")) {
            return false;
        }
        String rsp =
                restTemplate.exchange(stopPlaceRegistryUrl, HttpMethod.POST, createQueryHttpEntity(quayRef), String.class).getBody();

        // Look for "id" field in response, indicating hit. To a void parsing response
        return rsp != null && rsp.contains(SUCCESS_MATCHER);
    }

    /**
     * Return provided quayRef if valid, else throw exception.
     */
    public String getVerifiedQuayRef(String quayRef) {
        if (quayRef == null) {
            return null;
        }

        // Check that quayRef is a valid Quay id. To avoid getting hits on stop place / street / municipality whatever, as stop place registry query matches anything
        Preconditions.checkArgument(isValidQuayRef(quayRef), "%s is not a valid quayRef", quayRef);

        return quayRef;
    }


    private HttpEntity<String> createQueryHttpEntity(String quayRef) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, "application/json");
        headers.set(ET_CLIENT_NAME_HEADER, clientName);
        headers.set(ET_CLIENT_ID_HEADER, clientId);

        String query = buildQuery(quayRef);
        return new HttpEntity<>(query, headers);
    }

    private String buildQuery(String queryRef) {
        return "{\"operationName\":\"findStop\", \"query\": \"{ stopPlace(query: \\\"" + queryRef + "\\\",versionValidity:CURRENT_FUTURE) {id }}\", \"variables\":null}";
    }

}
