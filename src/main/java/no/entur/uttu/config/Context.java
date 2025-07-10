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

public class Context {

  private static ThreadLocal<String> providerPerThread = new ThreadLocal<>();
  private static ThreadLocal<String> userNamePerThread = new ThreadLocal<>();

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

  public static String getVerifiedProviderCode() {
    String providerCode = Context.getProvider();
    Preconditions.checkArgument(providerCode != null, "Provider not set for session");
    return providerCode;
  }

  public static void setUserName(String userName) {
    Preconditions.checkArgument(
      userName != null,
      "Attempt to set userName = null for session"
    );
    userNamePerThread.set(userName);
  }

  public static String getVerifiedUsername() {
    String userName = userNamePerThread.get();
    Preconditions.checkArgument(userName != null, "Username not set for session");
    return userName;
  }

  public static void clear() {
    providerPerThread.remove();
    userNamePerThread.remove();
  }
}
