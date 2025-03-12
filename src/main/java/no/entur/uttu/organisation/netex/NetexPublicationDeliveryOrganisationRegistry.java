package no.entur.uttu.organisation.netex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.annotation.PostConstruct;
import no.entur.uttu.error.codederror.CodedError;
import no.entur.uttu.error.codes.ErrorCodeEnumeration;
import no.entur.uttu.netex.NetexUnmarshaller;
import no.entur.uttu.organisation.spi.OrganisationRegistry;
import no.entur.uttu.util.Preconditions;
import org.rutebanken.netex.model.Authority;
import org.rutebanken.netex.model.Operator;
import org.rutebanken.netex.model.PublicationDeliveryStructure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class NetexPublicationDeliveryOrganisationRegistry
  implements OrganisationRegistry {

  protected final Logger logger = LoggerFactory.getLogger(
    NetexPublicationDeliveryOrganisationRegistry.class
  );

  protected final NetexUnmarshaller netexUnmarshaller = new NetexUnmarshaller(
    PublicationDeliveryStructure.class
  );

  protected final List<Authority> authorities = Collections.synchronizedList(
    new ArrayList<>()
  );

  protected final List<Operator> operators = Collections.synchronizedList(
    new ArrayList<>()
  );

  @PostConstruct
  public abstract void init();

  @Override
  public List<Authority> getAuthorities() {
    return Collections.unmodifiableList(authorities);
  }

  @Override
  public Optional<Authority> getAuthority(String id) {
    return authorities
      .stream()
      .filter(authority -> authority.getId().equals(id))
      .findFirst();
  }

  @Override
  public List<Operator> getOperators() {
    return Collections.unmodifiableList(operators);
  }

  @Override
  public Optional<Operator> getOperator(String id) {
    return operators.stream().filter(operator -> operator.getId().equals(id)).findFirst();
  }

  /**
   * By default, all organisations in the registry are valid operators
   */
  @Override
  public void validateOperatorRef(String operatorRef) {
    Preconditions.checkArgument(
      operators.stream().anyMatch(org -> org.getId().equals(operatorRef)),
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
      authorities.stream().anyMatch(org -> org.getId().equals(authorityRef)),
      CodedError.fromErrorCode(
        ErrorCodeEnumeration.ORGANISATION_NOT_IN_ORGANISATION_REGISTRY
      ),
      "Organisation with ref %s not found in organisation registry",
      authorityRef
    );
  }
}
