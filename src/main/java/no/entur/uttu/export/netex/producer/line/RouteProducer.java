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

package no.entur.uttu.export.netex.producer.line;

import no.entur.uttu.export.netex.NetexExportContext;
import no.entur.uttu.export.netex.producer.NetexObjectFactory;
import no.entur.uttu.model.FixedLine;
import no.entur.uttu.model.FlexibleLine;
import no.entur.uttu.model.JourneyPattern;
import no.entur.uttu.model.Line;
import no.entur.uttu.model.Ref;
import no.entur.uttu.model.StopPointInJourneyPattern;
import org.rutebanken.netex.model.DirectionTypeEnumeration;
import org.rutebanken.netex.model.FlexibleLineRefStructure;
import org.rutebanken.netex.model.LineRefStructure;
import org.rutebanken.netex.model.PointOnRoute;
import org.rutebanken.netex.model.PointsOnRoute_RelStructure;
import org.rutebanken.netex.model.Route;
import org.rutebanken.netex.model.RoutePointRefStructure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.xml.bind.JAXBElement;
import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class RouteProducer {

    @Autowired
    private NetexObjectFactory objectFactory;

    public List<Route> produce(Line line, NetexExportContext context) {
        return line.getJourneyPatterns().stream().map(jp -> mapRoute(jp, context)).collect(Collectors.toList());
    }

    /**
     * Create Route from JourneyPattern.
     * <p>
     * User first and last StopPointInJourneyPattern as RoutePoints
     */
    private Route mapRoute(JourneyPattern journeyPattern, NetexExportContext context) {
        StopPointInJourneyPattern firstStopPointInJP = journeyPattern.getPointsInSequence().get(0);
        StopPointInJourneyPattern lastStopPointInJP = journeyPattern.getPointsInSequence().get(journeyPattern.getPointsInSequence().size() - 1);

        PointsOnRoute_RelStructure pointsOnRoute_relStructure = new PointsOnRoute_RelStructure().withPointOnRoute().withPointOnRoute(
                mapPointOnRoute(firstStopPointInJP, 1, context),
                mapPointOnRoute(lastStopPointInJP, 2, context)
        );

        no.entur.uttu.model.Line line = journeyPattern.getLine();

        String name = journeyPattern.getName();
        if (name == null) {
            name = line.getName();
        }

        LineVisitor lineVisitor = new LineVisitor();
        line.accept(lineVisitor);
        LineRefStructure lineRefStructure = lineVisitor.getLine();

        JAXBElement<LineRefStructure> lineRef = objectFactory.wrapAsJAXBElement(
        objectFactory.populateRefStructure(lineRefStructure, journeyPattern.getLine().getRef(), true));

        return objectFactory.populateId(new Route(), journeyPattern.getRef())
                       .withLineRef(lineRef)
                       .withName(objectFactory.createMultilingualString(name))
                       .withDirectionType(objectFactory.mapEnum(journeyPattern.getDirectionType(), DirectionTypeEnumeration.class))
                       .withPointsInSequence(pointsOnRoute_relStructure);
    }

    private PointOnRoute mapPointOnRoute(StopPointInJourneyPattern stopPoint, int order, NetexExportContext context) {
        Ref ref;
        if (stopPoint.getFlexibleStopPlace() != null) {
            ref = stopPoint.getFlexibleStopPlace().getRef();
        } else {
            ref = objectFactory.createScheduledStopPointRefFromQuayRef(stopPoint.getQuayRef(), context);
        }

        context.routePointRefs.add(ref);

        return objectFactory.populateId(new PointOnRoute(), stopPoint.getRef())
                       .withOrder(BigInteger.valueOf(order))
                       .withPointRef(objectFactory.wrapRefStructure(new RoutePointRefStructure(), ref, false));
    }

    private static class LineVisitor implements no.entur.uttu.model.LineVisitor {
        private LineRefStructure line;

        public LineRefStructure getLine() {
            return line;
        }

        @Override
        public void visitFixedLine(FixedLine fixedLine) {
            line = new LineRefStructure();
        }

        @Override
        public void visitFlexibleLine(FlexibleLine flexibleLine) {
            line = new FlexibleLineRefStructure();
        }
    }
}
