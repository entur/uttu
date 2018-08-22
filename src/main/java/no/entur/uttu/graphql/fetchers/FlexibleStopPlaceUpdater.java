package no.entur.uttu.graphql.fetchers;

import com.google.common.base.Preconditions;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import no.entur.uttu.graphql.ArgumentWrapper;
import no.entur.uttu.graphql.mappers.GeometryMapper;
import no.entur.uttu.model.FlexibleStopPlace;
import no.entur.uttu.repository.FlexibleStopPlaceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static no.entur.uttu.graphql.GraphQLNames.*;

@Service("flexibleStopPlaceUpdater")
@Transactional
public class FlexibleStopPlaceUpdater implements DataFetcher<FlexibleStopPlace> {


    private static final Logger logger = LoggerFactory.getLogger(FlexibleStopPlaceUpdater.class);

    @Autowired
    private FlexibleStopPlaceRepository flexibleStopPlaceRepository;

    @Autowired
    private GeometryMapper geometryMapper;

    @Override
    public FlexibleStopPlace get(DataFetchingEnvironment env) {

        ArgumentWrapper input = new ArgumentWrapper(env.getArgument("flexibleStopPlace"));

        String netexId = input.get(FIELD_ID);
        FlexibleStopPlace flexibleStopPlace;
        if (netexId == null) {
            flexibleStopPlace = new FlexibleStopPlace();
        } else {
            flexibleStopPlace = flexibleStopPlaceRepository.getOne(netexId);
            Preconditions.checkArgument(flexibleStopPlace != null,
                    "Attempting to update FlexibleStopPlace [pk = %s], but StopPlace does not exist.", netexId);
            logger.info("Updating FlexibleStopArea[{}]", netexId);
        }

        populateFlexibleStopPlaceFromInput(flexibleStopPlace, input);

        flexibleStopPlaceRepository.save(flexibleStopPlace);
        return flexibleStopPlace;
    }


    private void populateFlexibleStopPlaceFromInput(FlexibleStopPlace flexibleStopPlace, ArgumentWrapper input) {
        input.apply(FIELD_NAME, flexibleStopPlace::setName);
        input.apply(FIELD_FLEXIBLE_AREA, g -> flexibleStopPlace.setPolygon(geometryMapper.createJTSPolygon((Map) g)));
        input.apply(FIELD_TRANSPORT_MODE,flexibleStopPlace::setTransportMode);

    }


}

