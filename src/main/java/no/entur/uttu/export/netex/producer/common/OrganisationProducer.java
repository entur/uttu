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

import no.entur.uttu.export.netex.NetexExportContext;
import no.entur.uttu.export.netex.producer.NetexObjectFactory;
import no.entur.uttu.model.Network;
import no.entur.uttu.model.job.SeverityEnumeration;
import no.entur.uttu.organisation.Organisation;
import no.entur.uttu.organisation.OrganisationContact;
import no.entur.uttu.organisation.OrganisationRegistry;
import org.rutebanken.netex.model.Authority;
import org.rutebanken.netex.model.AuthorityRefStructure;
import org.rutebanken.netex.model.ContactStructure;
import org.rutebanken.netex.model.Operator;
import org.rutebanken.netex.model.OperatorRefStructure;
import org.rutebanken.netex.model.Organisation_VersionStructure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.util.UrlUtils;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrganisationProducer {

    @Autowired
    private OrganisationRegistry organisationRegistry;

    @Autowired
    private NetexObjectFactory objectFactory;

    public List<Authority> produceAuthorities(NetexExportContext context) {
        return context.networks.stream().map(Network::getAuthorityRef).distinct().map(ref -> mapAuthority(ref, context)).collect(Collectors.toList());
    }

    public List<Operator> produceOperators(NetexExportContext context) {
        return context.operatorRefs.stream().map(ref -> mapOperator(ref, context)).collect(Collectors.toList());
    }

    public AuthorityRefStructure produceAuthorityRef(String authorityRef, boolean withVersion, NetexExportContext context) {
        Authority authority = mapAuthority(authorityRef, context);
        AuthorityRefStructure authorityRefStruct = new AuthorityRefStructure().withRef(authority.getId());
        if (withVersion) {
            authorityRefStruct.withVersion(authority.getVersion());
        }
        return authorityRefStruct;
    }


    public OperatorRefStructure produceOperatorRef(String operatorRef, boolean withVersion, NetexExportContext context) {
        Operator operator = mapOperator(operatorRef, context);
        OperatorRefStructure operatorRefStructure = new OperatorRefStructure().withRef(operator.getId());
        if (withVersion) {
            operatorRefStructure.withVersion(operator.getVersion());
        }
        return operatorRefStructure;
    }

    private Authority mapAuthority(String authorityRef, NetexExportContext context) {
        Organisation orgRegAuthority = organisationRegistry.getOrganisation(authorityRef);
        if (orgRegAuthority == null || orgRegAuthority.getAuthorityNetexId() == null) {
            context.addExportMessage(SeverityEnumeration.ERROR, "Authority [id:{0}] not found", authorityRef);
            return new Authority();
        }

        if (orgRegAuthority.contact == null || !validateContactUrl(orgRegAuthority.contact.url)) {
            context.addExportMessage(SeverityEnumeration.ERROR, "Invalid authority contact: {0}", orgRegAuthority.contact);
        }

        return populateNetexOrganisation(new Authority(), orgRegAuthority)
                       .withId(orgRegAuthority.getAuthorityNetexId());
    }

    private boolean validateContactUrl(String url) {
        return UrlUtils.isAbsoluteUrl(url);
    }

    private Operator mapOperator(String operatorRef, NetexExportContext context) {
        Organisation orgRegOperator = organisationRegistry.getOrganisation(operatorRef);
        if (orgRegOperator == null || orgRegOperator.getOperatorNetexId() == null) {
            context.addExportMessage(SeverityEnumeration.ERROR, "Operator [id:{0}] not found", operatorRef);
            return new Operator();
        }
        return populateNetexOrganisation(new Operator(), orgRegOperator)
                       .withId(orgRegOperator.getOperatorNetexId())
                       .withCustomerServiceContactDetails(mapContact(orgRegOperator.customerContact));
    }


    private <N extends Organisation_VersionStructure> N populateNetexOrganisation(N netexOrg, Organisation orgRegOrg) {
        netexOrg
                .withVersion(orgRegOrg.version)
                .withName(objectFactory.createMultilingualString(orgRegOrg.name))
                .withCompanyNumber(orgRegOrg.getCompanyNumber())
                .withContactDetails(mapContact(orgRegOrg.contact))
                .withLegalName(objectFactory.createMultilingualString(orgRegOrg.legalName));
        return netexOrg;
    }

    private ContactStructure mapContact(OrganisationContact local) {
        if (local == null) {
            return null;
        }
        return new ContactStructure()
                       .withEmail(local.email)
                       .withPhone(local.phone)
                       .withUrl(local.url);
    }

}
