package no.entur.uttu.graphql.fetchers;

import static no.entur.uttu.graphql.GraphQLNames.FIELD_SEARCH_TEXT;
import static no.entur.uttu.graphql.GraphQLNames.FIELD_TRANSPORT_MODE;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import java.util.ArrayList;
import java.util.List;

import no.entur.uttu.graphql.model.Quay;
import no.entur.uttu.graphql.model.StopPlace;
import no.entur.uttu.stopplace.filter.SearchTextStopPlaceFilter;
import no.entur.uttu.stopplace.filter.StopPlaceFilter;
import no.entur.uttu.stopplace.filter.TransportModeStopPlaceFilter;
import no.entur.uttu.stopplace.spi.StopPlaceRegistry;
import org.rutebanken.netex.model.AllVehicleModesOfTransportEnumeration;
//import org.rutebanken.netex.model.Quay;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("stopPlacesFetcher")
public class StopPlacesFetcher implements DataFetcher<List<StopPlace>> {

  @Autowired
  private StopPlaceRegistry stopPlaceRegistry;

  @Override
  public List<StopPlace> get(DataFetchingEnvironment environment) throws Exception {
    List<StopPlaceFilter> filters = new ArrayList<>();
    AllVehicleModesOfTransportEnumeration transportMode = environment.getArgument(
      FIELD_TRANSPORT_MODE
    );
    filters.add(new TransportModeStopPlaceFilter(transportMode));

    String searchText = environment.getArgument(FIELD_SEARCH_TEXT);
    if (searchText != null) {
      filters.add(new SearchTextStopPlaceFilter(searchText));
    }

     return stopPlaceRegistry.getStopPlaces2(filters);
    /*return stopPlaceRegistry
            .getStopPlaces(filters)
            .stream()
            .map(StopPlacesFetcher::mapStopPlace)
            .toList();*/
  }

  List<StopPlace> mapStopPlaces(List<org.rutebanken.netex.model.StopPlace> stops) {
    return stops.stream().map(StopPlacesFetcher::mapStopPlace).toList();
  }

  public static StopPlace mapStopPlace(org.rutebanken.netex.model.StopPlace stopPlace) {
    List<Quay> quays = stopPlace
      .getQuays()
      .getQuayRefOrQuay()
      .stream()
      .map(jaxbElement -> {
        org.rutebanken.netex.model.Quay netexQuay = (org.rutebanken.netex.model.Quay) jaxbElement.getValue();
        return new Quay(netexQuay.getId(), netexQuay.getName(), netexQuay.getPublicCode(), netexQuay.getCentroid());
      })
      .toList();
    return new StopPlace(
      stopPlace.getId(),
      stopPlace.getName(),
      stopPlace.getTransportMode(),
      stopPlace.getCentroid(),
      quays
    );
  }
}
