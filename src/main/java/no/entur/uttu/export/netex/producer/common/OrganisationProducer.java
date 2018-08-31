package no.entur.uttu.export.netex.producer.common;

import no.entur.uttu.export.model.ExportError;
import no.entur.uttu.export.netex.NetexExportContext;
import no.entur.uttu.export.netex.producer.NetexObjectFactory;
import no.entur.uttu.model.Network;
import no.entur.uttu.organisation.Organisation;
import no.entur.uttu.organisation.OrganisationContact;
import no.entur.uttu.organisation.OrganisationRegistry;
import org.rutebanken.netex.model.Authority;
import org.rutebanken.netex.model.ContactStructure;
import org.rutebanken.netex.model.Operator;
import org.rutebanken.netex.model.Organisation_VersionStructure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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

    private Authority mapAuthority(String authorityRef, NetexExportContext context) {
        Organisation orgRegAuthority = organisationRegistry.getOrganisation(authorityRef);
        if (orgRegAuthority == null || !orgRegAuthority.isAuthority()) {
            context.errors.add(new ExportError("Authority [id:{}] not found", authorityRef));
            return new Authority();
        }
        return populateNetexOrganisation(new Authority(), orgRegAuthority);
    }

    private Operator mapOperator(String operatorRef, NetexExportContext context) {
        Organisation orgRegOperator = organisationRegistry.getOrganisation(operatorRef);
        if (orgRegOperator == null || !orgRegOperator.isOperator()) {
            context.errors.add(new ExportError("Operator [id:{}] not found", operatorRef));
            return new Operator();
        }
        return populateNetexOrganisation(new Operator(), orgRegOperator)
                       .withCustomerServiceContactDetails(mapContact(orgRegOperator.customerContact));
    }


    private <N extends Organisation_VersionStructure> N populateNetexOrganisation(N netexOrg, Organisation orgRegOrg) {
        netexOrg
                .withId(orgRegOrg.getNetexId())
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
