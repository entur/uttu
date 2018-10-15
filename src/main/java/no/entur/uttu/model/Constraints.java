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

package no.entur.uttu.model;

/**
 * Constants representing constraint names in db. Add corresponding error msg to #DataIntegrityViolationExceptionMapper
 */
public class Constraints {

    public static final String FLEXIBLE_STOP_PLACE_UNIQUE_NAME = "flexible_stop_place_unique_name_constraint";

    public static final String FLEXIBLE_LINE_UNIQUE_NAME = "flexible_line_unique_name_constrain";

    public static final String JOURNEY_PATTERN_UNIQUE_NAME = "journey_pattern_unique_name_constraint";

    public static final String NETWORK_UNIQUE_NAME = "network_unique_name_constraint";

    public static final String SERVICE_JOURNEY_UNIQUE_NAME = "service_journey_unique_name_constraint";

    public static final String STOP_POINT_IN_JOURNEY_PATTERN_UNIQUE_ORDER = "stop_point_in_jp_unique_order_constraint";

    public static final String TIMETABLED_PASSING_TIME_UNIQUE_ORDER = "timetabled_passing_time_unique_order_constraint";

    public static final String PROVIDER_UNIQUE_CODE = "provider_unique_code_constraint";

    public static final String CODESPACE_UNIQUE_XMLNS = "codespace_unique_xmlns_constraint";

}
