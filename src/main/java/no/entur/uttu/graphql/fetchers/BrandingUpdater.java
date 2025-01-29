package no.entur.uttu.graphql.fetchers;

import no.entur.uttu.graphql.mappers.AbstractProviderEntityMapper;
import no.entur.uttu.model.Branding;
import no.entur.uttu.repository.generic.ProviderEntityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("brandingUpdater")
@Transactional
public class BrandingUpdater extends AbstractProviderEntityUpdater<Branding> {

  public BrandingUpdater(
    AbstractProviderEntityMapper<Branding> mapper,
    ProviderEntityRepository<Branding> repository
  ) {
    super(mapper, repository);
  }
}
