package no.entur.uttu.graphql.fetchers;

import static no.entur.uttu.graphql.GraphQLNames.FIELD_IDS;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import java.util.List;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import no.entur.uttu.model.DayType;
import no.entur.uttu.repository.generic.ProviderEntityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("dayTypesBulkUpdater")
@Transactional
public class DayTypesBulkUpdater implements DataFetcher<List<DayType>> {

  private final DayTypeUpdater dayTypeUpdater;
  private final ProviderEntityRepository<DayType> repository;

  @Autowired
  public DayTypesBulkUpdater(
    DayTypeUpdater dayTypeUpdater,
    ProviderEntityRepository<DayType> repository
  ) {
    this.dayTypeUpdater = dayTypeUpdater;
    this.repository = repository;
  }

  @Override
  public List<DayType> get(DataFetchingEnvironment environment) throws Exception {
    if (environment.getField().getName().equals("deleteDayTypes")) {
      return deleteEntities(environment);
    } else {
      return repository.findByIds(environment.getArgument(FIELD_IDS));
    }
  }

  protected List<DayType> deleteEntities(DataFetchingEnvironment env) {
    List<String> ids = env.getArgument(FIELD_IDS);
    ids.forEach(dayTypeUpdater::verifyDeleteAllowed);
    return ids.stream().map(repository::delete).collect(Collectors.toList());
  }
}
