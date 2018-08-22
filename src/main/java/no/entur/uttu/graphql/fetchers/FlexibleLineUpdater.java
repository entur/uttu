package no.entur.uttu.graphql.fetchers;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import no.entur.uttu.model.FlexibleLine;
import no.entur.uttu.repository.FlexibleLineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("flexibleLineUpdater")
@Transactional
public class FlexibleLineUpdater implements DataFetcher<FlexibleLine> {

    @Autowired
    private FlexibleLineRepository flexibleLineRepository;

    @Override
    public FlexibleLine get(DataFetchingEnvironment env) {
        return null;
    }
}
