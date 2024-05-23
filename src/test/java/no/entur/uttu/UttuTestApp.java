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

package no.entur.uttu;

import java.util.Map;
import no.entur.uttu.repository.generic.ProviderEntityRepositoryImpl;
import no.entur.uttu.security.UttuSecurityConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(
  basePackages = { "no.entur.uttu.repository" },
  repositoryBaseClass = ProviderEntityRepositoryImpl.class
)
@ComponentScan(
  excludeFilters = {
    @ComponentScan.Filter(
      type = FilterType.ASSIGNABLE_TYPE,
      value = UttuSecurityConfiguration.class
    ),
    @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = App.class),
  }
)
public class UttuTestApp extends SpringApplication {

  public static void main(String[] args) {
    SpringApplication.run(UttuTestApp.class, args);
  }

  @Override
  public void setDefaultProperties(Map<String, Object> defaultProperties) {
    defaultProperties.put("spring.cloud.gcp.pubsub.enabled", false);
    super.setDefaultProperties(defaultProperties);
  }
}
