package no.entur.uttu.graphql.fetchers;

import static no.entur.uttu.graphql.GraphQLNames.FIELD_ID;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import no.entur.uttu.graphql.model.StopPlace;
import no.entur.uttu.stopplace.spi.StopPlaceRegistry;
import org.rutebanken.netex.model.Quay;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("quayRefSearchFetcher")
public class QuayRefSearchFetcher
  implements DataFetcher<StopPlace> {

  @Autowired
  private StopPlaceRegistry stopPlaceRegistry;

  @Override
  public StopPlace get(DataFetchingEnvironment environment)
    throws Exception {
    return stopPlaceRegistry
      .getStopPlaceByQuayRef(environment.getArgument(FIELD_ID))
      .map(this::mapStopPlace)
      .orElse(null);
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
