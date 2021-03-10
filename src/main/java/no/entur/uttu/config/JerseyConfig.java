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

import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.jaxrs.listing.ApiListingResource;
import io.swagger.jaxrs.listing.SwaggerSerializers;
import no.entur.uttu.export.resource.ExportFileDownloadResource;
import no.entur.uttu.graphql.resource.DataIntegrityViolationExceptionMapper;
import no.entur.uttu.graphql.resource.LinesGraphQLResource;
import no.entur.uttu.graphql.resource.GeneralExceptionMapper;
import no.entur.uttu.graphql.resource.ProviderGraphQLResource;
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
                = new ServletRegistrationBean(new ServletContainer(new LinesAPI()));
        publicJersey.addUrlMappings("/services/flexible-lines/*");
        publicJersey.setName("FlexibleLinesAPI");
        publicJersey.setLoadOnStartup(0);
        return publicJersey;
    }

    private class LinesAPI extends ResourceConfig {

        public LinesAPI() {
            register(DataIntegrityViolationExceptionMapper.class);
            register(GeneralExceptionMapper.class);
            register(LinesGraphQLResource.class);
            register(ProviderGraphQLResource.class);
            register(ExportFileDownloadResource.class);
        }
    }

}
