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
import no.entur.uttu.stopplace.filter.params.BoundingBoxFilterParams;
import no.entur.uttu.stopplace.filter.params.LimitStopPlacesQuantityFilterParams;
import no.entur.uttu.stopplace.filter.params.QuayIdFilterParams;
import no.entur.uttu.stopplace.filter.params.SearchTextStopPlaceFilterParams;
import no.entur.uttu.stopplace.filter.params.StopPlaceFilterParams;
import no.entur.uttu.stopplace.filter.params.TransportModeStopPlaceFilterParams;
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
    List<StopPlaceFilterParams> filters = new ArrayList<>();
    AllVehicleModesOfTransportEnumeration transportMode = environment.getArgument(
      FIELD_TRANSPORT_MODE
    );
    filters.add(new TransportModeStopPlaceFilterParams(transportMode));

    String searchText = environment.getArgument(FIELD_SEARCH_TEXT);
    if (searchText != null) {
      filters.add(new SearchTextStopPlaceFilterParams(searchText));
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
        new BoundingBoxFilterParams(
          northEastLat,
          northEastLng,
          southWestLat,
          southWestLng
        )
      );
    }

    List<String> quayIds = environment.getArgument("quayIds");
    if (quayIds != null) {
      filters.add(new QuayIdFilterParams(quayIds));
    }

    Integer limit = environment.getArgument("limit");
    if (limit != null) {
      filters.add(new LimitStopPlacesQuantityFilterParams(limit));
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
