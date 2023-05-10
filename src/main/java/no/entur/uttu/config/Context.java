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

package no.entur.uttu.config;

import no.entur.uttu.util.Preconditions;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

public class Context {

  private static ThreadLocal<String> providerPerThread = new ThreadLocal<>();

  public static void setProvider(String providerCode) {
    Preconditions.checkArgument(
      providerCode != null,
      "Attempt to set providerCode = null for session"
    );
    providerPerThread.set(providerCode);
  }

  public static String getProvider() {
    return providerPerThread.get();
  }

  public static void clear() {
    providerPerThread.remove();
  }

  public static String getUsername() {
    String user = null;
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (
      auth != null && auth.getPrincipal() != null && auth.getPrincipal() instanceof Jwt
    ) {
      user = ((Jwt) auth.getPrincipal()).getClaimAsString("preferred_username");
    }
    return (user == null) ? "unknown" : user;
  }

  public static String getVerifiedProviderCode() {
    String providerCode = Context.getProvider();
    Preconditions.checkArgument(providerCode != null, "Provider not set for session");
    return providerCode;
  }
}
