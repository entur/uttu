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

import static no.entur.uttu.model.ModelTestUtil.assertCheckPersistableFails;

import org.junit.Test;

public class StopPointInJourneyPatternTest {

  @Test
  public void checkPersistable_bothQuayRefAndFlexibleStopPlace_givesException() {
    StopPointInJourneyPattern stopPointInJourneyPattern = new StopPointInJourneyPattern();
    stopPointInJourneyPattern.setQuayRef("quayRef");
    stopPointInJourneyPattern.setFlexibleStopPlace(new FlexibleStopPlace());

    assertCheckPersistableFails(stopPointInJourneyPattern);
  }

  @Test
  public void checkPersistable_neitherQuayRefNorFlexibleStopPlace_givesException() {
    StopPointInJourneyPattern stopPointInJourneyPattern = new StopPointInJourneyPattern();
    assertCheckPersistableFails(stopPointInJourneyPattern);
  }

  @Test
  public void checkPersistable_neitherBoardingNorAlightingAllowed_givesException() {
    StopPointInJourneyPattern stopPointInJourneyPattern = new StopPointInJourneyPattern();
    stopPointInJourneyPattern.setQuayRef("quayRef");
    stopPointInJourneyPattern.setForAlighting(false);
    stopPointInJourneyPattern.setForBoarding(false);
    assertCheckPersistableFails(stopPointInJourneyPattern);
  }

  @Test
  public void checkPersistable_withFlexibleStopPlace_success() {
    StopPointInJourneyPattern stopPointInJourneyPattern = new StopPointInJourneyPattern();
    stopPointInJourneyPattern.setFlexibleStopPlace(new FlexibleStopPlace());
    stopPointInJourneyPattern.checkPersistable();
  }

  @Test
  public void checkPersistable_withQuayRef_success() {
    StopPointInJourneyPattern stopPointInJourneyPattern = new StopPointInJourneyPattern();
    stopPointInJourneyPattern.setQuayRef("quayRef");
    stopPointInJourneyPattern.checkPersistable();
  }
}
