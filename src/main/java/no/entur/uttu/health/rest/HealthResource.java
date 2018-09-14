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

package no.entur.uttu.health.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import no.entur.uttu.health.repository.DbStatusChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

@Component
@Produces("application/json")
@Path("")
@Api(tags = {"Application status resource"}, produces = "text/plain")
public class HealthResource {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private DbStatusChecker dbStatusChecker;

    @GET
    @Path("/ready")
    @ApiOperation(value = "Checks application readiness, including db connection", response = Void.class)
    @ApiResponses(value = {
                                  @ApiResponse(code = 200, message = "application is ready"),
                                  @ApiResponse(code = 500, message = "application is not ready")
    })
    public Response isReady() {
        logger.debug("Checking readiness...");
        if (dbStatusChecker.isDbUp()) {
            return Response.ok().build();
        } else {
            return Response.serverError().build();
        }
    }

    @GET
    @Path("/live")
    @ApiOperation(value = "Returns OK if application is running", response = Void.class)
    @ApiResponses(value = {
                                  @ApiResponse(code = 200, message = "application is running")
    })
    public Response isLive() {
        return Response.ok().build();
    }

}
