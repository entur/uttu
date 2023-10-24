/*
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *   https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */

package no.entur.uttu.export.netex.producer.common;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import no.entur.uttu.export.netex.NetexExportContext;
import no.entur.uttu.model.Network;
import no.entur.uttu.model.job.SeverityEnumeration;
import no.entur.uttu.organisation.OrganisationRegistry;
import org.rutebanken.netex.model.Authority;
import org.rutebanken.netex.model.AuthorityRef;
import org.rutebanken.netex.model.GeneralOrganisation;
import org.rutebanken.netex.model.KeyValueStructure;
import org.rutebanken.netex.model.Operator;
import org.rutebanken.netex.model.OperatorRefStructure;
import org.rutebanken.netex.model.Organisation_VersionStructure;
import org.springframework.stereotype.Component;

@Component
public class OrganisationProducer {

  private final OrganisationRegistry organisationRegistry;

  public OrganisationProducer(OrganisationRegistry organisationRegistry) {
    this.organisationRegistry = organisationRegistry;
  }

  public List<Authority> produceAuthorities(NetexExportContext context) {
    return context.networks
      .stream()
      .map(Network::getAuthorityRef)
      .distinct()
      .map(ref -> mapAuthority(ref, context))
      .collect(Collectors.toList());
  }

  public List<Operator> produceOperators(NetexExportContext context) {
    return context.operatorRefs
      .stream()
      .map(ref -> mapOperator(ref, context))
      .collect(Collectors.toList());
  }

  public AuthorityRef produceAuthorityRef(
    String authorityRef,
    boolean withVersion,
    NetexExportContext context
  ) {
    Authority authority = mapAuthority(authorityRef, context);
    AuthorityRef authorityRefElement = new AuthorityRef().withRef(authority.getId());
    if (withVersion) {
      authorityRefElement.withVersion(authority.getVersion());
    }
    return authorityRefElement;
  }

  public OperatorRefStructure produceOperatorRef(
    String operatorRef,
    boolean withVersion,
    NetexExportContext context
  ) {
    Operator operator = mapOperator(operatorRef, context);
    OperatorRefStructure operatorRefStructure = new OperatorRefStructure()
      .withRef(operator.getId());
    if (withVersion) {
      operatorRefStructure.withVersion(operator.getVersion());
    }
    return operatorRefStructure;
  }

  private Authority mapAuthority(String authorityRef, NetexExportContext context) {
    Optional<GeneralOrganisation> orgRegAuthority = organisationRegistry.getOrganisation(
      authorityRef
    );
    if (
      orgRegAuthority.isEmpty() ||
      organisationRegistry.getVerifiedAuthorityRef(authorityRef) == null
    ) {
      context.addExportMessage(
        SeverityEnumeration.ERROR,
        "Authority [id:{0}] not found",
        authorityRef
      );
      return new Authority();
    }

    GeneralOrganisation organisation = orgRegAuthority.get();

    if (
      organisation.getContactDetails() == null ||
      !validateContactUrl(organisation.getContactDetails().getUrl())
    ) {
      context.addExportMessage(
        SeverityEnumeration.ERROR,
        "Invalid authority contact: {0}",
        organisation.getContactDetails()
      );
    }

    return populateNetexOrganisation(new Authority(), organisation)
      .withId(getAuthorityNetexId(organisation));
  }

  private boolean validateContactUrl(String url) {
    return url != null && (url.startsWith("http://") || url.startsWith("https://"));
  }

  private Operator mapOperator(String operatorRef, NetexExportContext context) {
    Optional<GeneralOrganisation> orgRegOperator = organisationRegistry.getOrganisation(
      operatorRef
    );
    if (
      orgRegOperator.isEmpty() ||
      organisationRegistry.getVerifiedOperatorRef(operatorRef) == null
    ) {
      context.addExportMessage(
        SeverityEnumeration.ERROR,
        "Operator [id:{0}] not found",
        operatorRef
      );
      return new Operator();
    }

    GeneralOrganisation organisation = orgRegOperator.get();

    return populateNetexOrganisation(new Operator(), organisation)
      .withId(getOperatorNetexId(organisation))
      .withCustomerServiceContactDetails(organisation.getContactDetails());
  }

  private <N extends Organisation_VersionStructure> N populateNetexOrganisation(
    N netexOrg,
    GeneralOrganisation orgRegOrg
  ) {
    netexOrg
      .withVersion(orgRegOrg.getVersion())
      .withName(orgRegOrg.getName())
      .withCompanyNumber(orgRegOrg.getCompanyNumber())
      .withContactDetails(orgRegOrg.getContactDetails())
      .withLegalName(orgRegOrg.getLegalName());
    return netexOrg;
  }

  private String getOperatorNetexId(GeneralOrganisation organisation) {
    return getNetexId(organisation, "Operator");
  }

  private String getAuthorityNetexId(GeneralOrganisation organisation) {
    return getNetexId(organisation, "Authority");
  }

  protected static String getNetexId(GeneralOrganisation organisation, String type) {
    return Optional
      .ofNullable(organisation.getKeyList())
      .flatMap(kl ->
        kl
          .getKeyValue()
          .stream()
          .filter(keyValueStructure -> keyValueStructure.getKey().equals("LegacyId"))
          .findFirst()
          .map(KeyValueStructure::getValue)
          .map(value -> value.split(","))
          .flatMap(value ->
            Arrays.stream(value).filter(id -> id.contains(type)).findFirst()
          )
      )
      .orElse(organisation.getId());
  }
}
