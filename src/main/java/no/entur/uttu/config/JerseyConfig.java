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
 *
 */

package no.entur.uttu.config;

import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.jaxrs.listing.ApiListingResource;
import io.swagger.jaxrs.listing.SwaggerSerializers;
import no.entur.uttu.export.ExportResource;
import no.entur.uttu.graphql.resource.FlexibleLinesGraphQLResource;
import no.entur.uttu.graphql.resource.ProviderGraphQLResource;
import no.entur.uttu.health.rest.HealthResource;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JerseyConfig {

    @Bean
    public ServletRegistrationBean publicAPIJerseyConfig() {
        ServletRegistrationBean publicJersey
                = new ServletRegistrationBean(new ServletContainer(new FlexibleLinesAPI()));
        publicJersey.addUrlMappings("/services/timetable-flexible/*");
        publicJersey.setName("FlexibleLinesAPI");
        publicJersey.setLoadOnStartup(0);
        return publicJersey;
    }


    @Bean
    public ServletRegistrationBean privateJersey() {
        ServletRegistrationBean privateJersey
                = new ServletRegistrationBean(new ServletContainer(new HealthConfig()));
        privateJersey.addUrlMappings("/health/*");
        privateJersey.setName("PrivateJersey");
        privateJersey.setLoadOnStartup(0);
        return privateJersey;
    }


    private class FlexibleLinesAPI extends ResourceConfig {

        public FlexibleLinesAPI() {
            register(CorsResponseFilter.class);

            register(FlexibleLinesGraphQLResource.class);
            register(ProviderGraphQLResource.class);
            register(ExportResource.class);
        }

    }

    private class HealthConfig extends ResourceConfig {

        public HealthConfig() {
            register(HealthResource.class);
            configureSwagger();
        }


        private void configureSwagger() {
            // Available at localhost:port/api/swagger.json
            this.register(ApiListingResource.class);
            this.register(SwaggerSerializers.class);

            BeanConfig config = new BeanConfig();
            config.setConfigId("uttu-health-swagger-doc");
            config.setTitle("Uttu Health API");
            config.setVersion("v1");
            config.setSchemes(new String[]{"http", "https"});
            config.setBasePath("/health");
            config.setResourcePackage("no.entur.uttu.health");
            config.setPrettyPrint(true);
            config.setScan(true);
            config.setScannerId("health-scanner");

        }
    }
}
