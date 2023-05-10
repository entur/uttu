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

import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class JourneyPatternTest {

  @Test
  public void checkPersistable_minFields_success() {
    validJourneyPattern().checkPersistable();
  }

  @Test
  public void checkPersistable_tooFewStopPointsInJourneyPattern_givesException() {
    JourneyPattern jp = validJourneyPattern();
    jp.getPointsInSequence().remove(1);
    assertCheckPersistableFails(jp);
  }

  @Test
  public void checkPersistable_noBoardingOnFirstStopPointsInJourneyPattern_givesException() {
    JourneyPattern jp = validJourneyPattern();
    jp.getPointsInSequence().get(0).setForBoarding(false);
    assertCheckPersistableFails(jp);
  }

  @Test
  public void checkPersistable_noAlightingOnLastStopPointsInJourneyPattern_givesException() {
    JourneyPattern jp = validJourneyPattern();
    jp
      .getPointsInSequence()
      .get(jp.getPointsInSequence().size() - 1)
      .setForAlighting(false);
    assertCheckPersistableFails(jp);
  }

  @Test
  public void checkPersistable_destinationDisplayOnLastStopPointsInJourneyPattern_givesException() {
    JourneyPattern jp = validJourneyPattern();
    jp
      .getPointsInSequence()
      .get(jp.getPointsInSequence().size() - 1)
      .setDestinationDisplay(new DestinationDisplay());
    assertCheckPersistableFails(jp);
  }

  @Test
  public void setPointsInSequence_assignsOrder() {
    JourneyPattern journeyPattern = new JourneyPattern();

    List<StopPointInJourneyPattern> stopPoints = Arrays.asList(
      new StopPointInJourneyPattern(),
      new StopPointInJourneyPattern(),
      new StopPointInJourneyPattern()
    );
    journeyPattern.setPointsInSequence(stopPoints);

    Assert.assertEquals(3, journeyPattern.getPointsInSequence().size());
    Assert.assertEquals(1, journeyPattern.getPointsInSequence().get(0).getOrder());
    Assert.assertEquals(2, journeyPattern.getPointsInSequence().get(1).getOrder());
    Assert.assertEquals(3, journeyPattern.getPointsInSequence().get(2).getOrder());
  }

  protected static JourneyPattern validJourneyPattern() {
    JourneyPattern journeyPattern = new JourneyPattern();

    StopPointInJourneyPattern firstPoint = new StopPointInJourneyPattern();
    firstPoint.setForBoarding(true);
    firstPoint.setQuayRef("quayRef");

    firstPoint.setDestinationDisplay(new DestinationDisplay());

    StopPointInJourneyPattern lastPoint = new StopPointInJourneyPattern();
    lastPoint.setForAlighting(true);
    lastPoint.setQuayRef("quayRef");
    journeyPattern.setPointsInSequence(Arrays.asList(firstPoint, lastPoint));
    return journeyPattern;
  }
}
