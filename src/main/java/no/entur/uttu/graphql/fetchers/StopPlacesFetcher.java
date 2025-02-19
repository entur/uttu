package no.entur.uttu.graphql.fetchers;

import static no.entur.uttu.graphql.GraphQLNames.FIELD_NORTH_EAST_LAT;
import static no.entur.uttu.graphql.GraphQLNames.FIELD_NORTH_EAST_LNG;
import static no.entur.uttu.graphql.GraphQLNames.FIELD_SEARCH_TEXT;
import static no.entur.uttu.graphql.GraphQLNames.FIELD_SOUTH_WEST_LAT;
import static no.entur.uttu.graphql.GraphQLNames.FIELD_SOUTH_WEST_LNG;
import static no.entur.uttu.graphql.GraphQLNames.FIELD_TRANSPORT_MODE;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import no.entur.uttu.graphql.model.StopPlace;
import no.entur.uttu.stopplace.filter.BoundingBoxFilter;
import no.entur.uttu.stopplace.filter.QuayIdFilter;
import no.entur.uttu.stopplace.filter.SearchTextStopPlaceFilter;
import no.entur.uttu.stopplace.filter.StopPlaceFilter;
import no.entur.uttu.stopplace.filter.TransportModeStopPlaceFilter;
import no.entur.uttu.stopplace.spi.StopPlaceRegistry;
import org.rutebanken.netex.model.AllVehicleModesOfTransportEnumeration;
import org.rutebanken.netex.model.Quay;
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

    BigDecimal northEastLat = environment.getArgument(FIELD_NORTH_EAST_LAT);
    BigDecimal northEastLng = environment.getArgument(FIELD_NORTH_EAST_LNG);
    BigDecimal southWestLat = environment.getArgument(FIELD_SOUTH_WEST_LAT);
    BigDecimal southWestLng = environment.getArgument(FIELD_SOUTH_WEST_LNG);

    if (
      northEastLat != null &&
      northEastLng != null &&
      southWestLat != null &&
      southWestLng != null
    ) {
      filters.add(
        new BoundingBoxFilter(northEastLat, northEastLng, southWestLat, southWestLng)
      );
    }

    List<String> quayIds = environment.getArgument("quayIds");
    if (quayIds != null) {
      filters.add(new QuayIdFilter(quayIds));
    }

    return stopPlaceRegistry
      .getStopPlaces(filters)
      .stream()
      .map(this::mapStopPlace)
      .toList();
  }

  public StopPlace mapStopPlace(org.rutebanken.netex.model.StopPlace stopPlace) {
    List<Quay> quays = stopPlace
      .getQuays()
      .getQuayRefOrQuay()
      .stream()
      .map(jaxbElement -> (org.rutebanken.netex.model.Quay) jaxbElement.getValue())
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
