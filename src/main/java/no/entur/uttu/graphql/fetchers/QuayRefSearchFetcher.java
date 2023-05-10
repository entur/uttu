package no.entur.uttu.graphql.fetchers;

import static no.entur.uttu.graphql.GraphQLNames.FIELD_ID;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import java.util.stream.Collectors;
import no.entur.uttu.model.TimetabledPassingTime;
import no.entur.uttu.stopplace.StopPlaceRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("quayRefSearchFetcher")
public class QuayRefSearchFetcher
  implements DataFetcher<TimetabledPassingTime.StopPlace> {

  @Autowired
  private StopPlaceRegistry stopPlaceRegistry;

  @Override
  public TimetabledPassingTime.StopPlace get(DataFetchingEnvironment environment)
    throws Exception {
    return stopPlaceRegistry
      .getStopPlaceByQuayRef(environment.getArgument(FIELD_ID))
      .map(this::mapStopPlace)
      .orElse(null);
  }

  public TimetabledPassingTime.StopPlace mapStopPlace(
    org.rutebanken.netex.model.StopPlace stopPlace
  ) {
    TimetabledPassingTime.StopPlace mapped = new TimetabledPassingTime.StopPlace();
    mapped.setId(stopPlace.getId());
    mapped.setName(mapMultilingualString(stopPlace.getName()));
    mapped.setQuays(
      stopPlace
        .getQuays()
        .getQuayRefOrQuay()
        .stream()
        .map(v -> (org.rutebanken.netex.model.Quay) v)
        .map(this::mapQuay)
        .collect(Collectors.toList())
    );
    return mapped;
  }

  private TimetabledPassingTime.MultilingualString mapMultilingualString(
    org.rutebanken.netex.model.MultilingualString multilingualString
  ) {
    TimetabledPassingTime.MultilingualString mapped =
      new TimetabledPassingTime.MultilingualString();
    mapped.setLang(multilingualString.getLang());
    mapped.setValue(multilingualString.getValue());
    return mapped;
  }

  private TimetabledPassingTime.Quay mapQuay(org.rutebanken.netex.model.Quay quay) {
    TimetabledPassingTime.Quay mapped = new TimetabledPassingTime.Quay();
    mapped.setId(quay.getId());
    mapped.setPublicCode(quay.getPublicCode());
    return mapped;
  }
}
