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

import org.junit.Rule;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

@RunWith(SpringRunner.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = UttuTestApp.class
)
@ActiveProfiles({"google-pubsub-emulator", "test"})
@EnableAspectJAutoProxy(proxyTargetClass = true)
public abstract class UttuIntegrationTest {

    @Rule
    public GenericContainer db = new GenericContainer(DockerImageName.parse("postgis/postgis:13-3.3"))
            .withEnv("POSTGRES_USER", "uttu")
            .withEnv("POSTGRES_PASSWORD", "uttu")
            .withEnv("POSTGRES_DB", "uttu")
            .withExposedPorts(5432);


    @Value("${local.server.port}")
    protected int port;
}
