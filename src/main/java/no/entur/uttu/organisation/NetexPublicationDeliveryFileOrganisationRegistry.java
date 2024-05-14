package no.entur.uttu.organisation;

import java.io.File;
import java.util.List;
import java.util.Optional;
import javax.annotation.PostConstruct;
import javax.xml.transform.stream.StreamSource;
import no.entur.uttu.error.codederror.CodedError;
import no.entur.uttu.error.codes.ErrorCodeEnumeration;
import no.entur.uttu.netex.NetexUnmarshaller;
import no.entur.uttu.netex.NetexUnmarshallerReadFromSourceException;
import no.entur.uttu.organisation.spi.OrganisationRegistry;
import no.entur.uttu.util.Preconditions;
import org.rutebanken.netex.model.GeneralOrganisation;
import org.rutebanken.netex.model.PublicationDeliveryStructure;
import org.rutebanken.netex.model.ResourceFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnMissingBean(
  value = OrganisationRegistry.class,
  ignored = NetexPublicationDeliveryFileOrganisationRegistry.class
)
public class NetexPublicationDeliveryFileOrganisationRegistry
  implements OrganisationRegistry {

  private static final Logger logger = LoggerFactory.getLogger(
    NetexPublicationDeliveryFileOrganisationRegistry.class
  );

  private static final NetexUnmarshaller publicationDeliveryUnmarshaller =
    new NetexUnmarshaller(PublicationDeliveryStructure.class);

  private List<GeneralOrganisation> organisations = List.of();

  @Value("${uttu.organisations.netex-file-uri}")
  String netexFileUri;

  @PostConstruct
  public void init() {
    try {
      PublicationDeliveryStructure publicationDeliveryStructure =
        publicationDeliveryUnmarshaller.unmarshalFromSource(
          new StreamSource(new File(netexFileUri))
        );
      publicationDeliveryStructure
        .getDataObjects()
        .getCompositeFrameOrCommonFrame()
        .forEach(frame -> {
          var frameValue = frame.getValue();
          if (frameValue instanceof ResourceFrame resourceFrame) {
            organisations =
              resourceFrame
                .getOrganisations()
                .getOrganisation_()
                .stream()
                .map(org -> (GeneralOrganisation) org.getValue())
                .toList();
          }
        });
    } catch (NetexUnmarshallerReadFromSourceException e) {
      logger.warn(
        "Unable to unmarshal organisations xml, organisation registry will be an empty list"
      );
    }
  }

  @Override
  public List<GeneralOrganisation> getOrganisations() {
    return organisations;
  }

  @Override
  public Optional<GeneralOrganisation> getOrganisation(String id) {
    return organisations.stream().filter(org -> org.getId().equals(id)).findFirst();
  }

  /**
   * By default, all organisations in the registry are valid operators
   */
  @Override
  public void validateOperatorRef(String operatorRef) {
    Preconditions.checkArgument(
      organisations.stream().anyMatch(org -> org.getId().equals(operatorRef)),
      CodedError.fromErrorCode(
        ErrorCodeEnumeration.ORGANISATION_NOT_IN_ORGANISATION_REGISTRY
      ),
      "Organisation with ref %s not found in organisation registry",
      operatorRef
    );
  }

  /**
   * By default, all organisations in the registry are valid authorities
   */
  @Override
  public void validateAuthorityRef(String authorityRef) {
    Preconditions.checkArgument(
      organisations.stream().anyMatch(org -> org.getId().equals(authorityRef)),
      CodedError.fromErrorCode(
        ErrorCodeEnumeration.ORGANISATION_NOT_IN_ORGANISATION_REGISTRY
      ),
      "Organisation with ref %s not found in organisation registry",
      authorityRef
    );
  }
}
