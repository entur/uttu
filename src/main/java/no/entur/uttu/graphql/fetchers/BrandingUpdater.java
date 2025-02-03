package no.entur.uttu.graphql.fetchers;

import graphql.schema.DataFetchingEnvironment;
import no.entur.uttu.error.codederror.EntityHasReferencesCodedError;
import no.entur.uttu.graphql.mappers.AbstractProviderEntityMapper;
import no.entur.uttu.model.Branding;
import no.entur.uttu.repository.FixedLineRepository;
import no.entur.uttu.repository.FlexibleLineRepository;
import no.entur.uttu.repository.generic.ProviderEntityRepository;
import no.entur.uttu.util.Preconditions;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("brandingUpdater")
@Transactional
public class BrandingUpdater extends AbstractProviderEntityUpdater<Branding> {

  private final FixedLineRepository fixedLineRepository;
  private final FlexibleLineRepository flexibleLineRepository;

  public BrandingUpdater(
    AbstractProviderEntityMapper<Branding> mapper,
    ProviderEntityRepository<Branding> repository,
    FixedLineRepository fixedLineRepository,
    FlexibleLineRepository flexibleLineRepository
  ) {
    super(mapper, repository);
    this.fixedLineRepository = fixedLineRepository;
    this.flexibleLineRepository = flexibleLineRepository;
  }

  @Override
  protected Branding deleteEntity(DataFetchingEnvironment env) {
    return super.deleteEntity(env);
  }

  @Override
  protected void verifyDeleteAllowed(String id) {
    var branding = repository.getOne(id);
    if (branding != null) {
      long noOfLines =
        fixedLineRepository.countByBranding(branding) +
        flexibleLineRepository.countByBranding(branding);
      Preconditions.checkArgument(
        noOfLines == 0,
        EntityHasReferencesCodedError.fromNumberOfReferences((int) noOfLines),
        "%s cannot be deleted as it is referenced by %s line(s)",
        branding.identity(),
        noOfLines
      );
    }
    super.verifyDeleteAllowed(id);
  }
}
