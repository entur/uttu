package no.entur.uttu.graphql.mappers;

import static no.entur.uttu.graphql.GraphQLNames.*;
import static no.entur.uttu.graphql.GraphQLNames.FIELD_NOTICES;
import static no.entur.uttu.graphql.LinesGraphQLSchema.FIELD_BRANDING_REF;

import no.entur.uttu.graphql.ArgumentWrapper;
import no.entur.uttu.model.Line;
import no.entur.uttu.organisation.spi.OrganisationRegistry;
import no.entur.uttu.repository.BrandingRepository;
import no.entur.uttu.repository.NetworkRepository;
import no.entur.uttu.repository.ProviderRepository;
import no.entur.uttu.repository.generic.ProviderEntityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public abstract class LineMapper<T extends Line>
  extends AbstractGroupOfEntitiesMapper<T> {

  @Autowired
  private NetworkRepository networkRepository;

  @Autowired
  private JourneyPatternMapper journeyPatternMapper;

  @Autowired
  private NoticeMapper noticeMapper;

  @Autowired
  private OrganisationRegistry organisationRegistry;

  @Autowired
  private BrandingRepository brandingRepository;

  public LineMapper(
    ProviderRepository providerRepository,
    ProviderEntityRepository<T> repository
  ) {
    super(providerRepository, repository);
  }

  @Override
  protected void populateEntityFromInput(T entity, ArgumentWrapper input) {
    input.apply(FIELD_NAME, entity::setName);
    input.apply(FIELD_PUBLIC_CODE, entity::setPublicCode);
    input.apply(FIELD_TRANSPORT_MODE, entity::setTransportMode);
    input.apply(FIELD_TRANSPORT_SUBMODE, entity::setTransportSubmode);
    input.applyReference(FIELD_NETWORK_REF, networkRepository, entity::setNetwork);
    input.applyReference(FIELD_BRANDING_REF, brandingRepository, entity::setBranding);

    input.apply(
      FIELD_OPERATOR_REF,
      (String operatorRef) -> {
        organisationRegistry.validateOperatorRef(operatorRef);
        return operatorRef;
      },
      entity::setOperatorRef
    );

    input.applyList(
      FIELD_JOURNEY_PATTERNS,
      journeyPatternMapper::map,
      entity::setJourneyPatterns
    );
    input.applyList(FIELD_NOTICES, noticeMapper::map, entity::setNotices);
  }
}
