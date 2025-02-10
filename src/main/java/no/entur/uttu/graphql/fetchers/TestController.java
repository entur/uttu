package no.entur.uttu.graphql.fetchers;

import no.entur.uttu.graphql.model.StopPlace;
import no.entur.uttu.stopplace.filter.SearchTextStopPlaceFilter;
import no.entur.uttu.stopplace.filter.StopPlaceFilter;
import no.entur.uttu.stopplace.filter.TransportModeStopPlaceFilter;
import no.entur.uttu.stopplace.spi.StopPlaceRegistry;
import org.rutebanken.netex.model.AllVehicleModesOfTransportEnumeration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    private StopPlaceRegistry stopPlaceRegistry;

    public TestController() {
    }

    @GetMapping(path = "/stops")
    public ResponseEntity<List<StopPlace>> listContexts(@RequestParam(name = "transportMode") String transportMode, @RequestParam(name = "searchText") String searchText) {
        List<StopPlaceFilter> filters = new ArrayList<>();
        filters.add(new TransportModeStopPlaceFilter(AllVehicleModesOfTransportEnumeration.fromValue(transportMode)));
        if (searchText != null) {
            filters.add(new SearchTextStopPlaceFilter(searchText));
        }

        return ResponseEntity.ok(stopPlaceRegistry.getStopPlaces2(filters));
       /* return ResponseEntity.ok(stopPlaceRegistry.getStopPlaces(filters).stream()
               .map(StopPlacesFetcher::mapStopPlace)
                .toList());*/
    }
}
