package no.entur.uttu.graphql.mappers;

import no.entur.uttu.graphql.ArgumentWrapper;
import no.entur.uttu.model.Branding;
import no.entur.uttu.repository.ProviderRepository;
import no.entur.uttu.repository.generic.ProviderEntityRepository;
import org.springframework.stereotype.Component;

@Component
public class BrandingMapper extends AbstractProviderEntityMapper<Branding> {

  public BrandingMapper(
    ProviderRepository providerRepository,
    ProviderEntityRepository<Branding> entityRepository
  ) {
    super(providerRepository, entityRepository);
  }

  @Override
  protected Branding createNewEntity(ArgumentWrapper input) {
    return new Branding();
  }

  @Override
  protected void populateEntityFromInput(Branding entity, ArgumentWrapper input) {
    input.apply("name", entity::setName);
    input.apply("shortName", entity::setShortName);
    input.apply("description", entity::setDescription);
    input.apply("url", entity::setUrl);
    input.apply("imageUrl", entity::setImageUrl);
  }
}
