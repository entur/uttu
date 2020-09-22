package no.entur.uttu.graphql.mappers;

import no.entur.uttu.graphql.ArgumentWrapper;
import no.entur.uttu.model.job.Export;
import no.entur.uttu.model.job.ExportLineAssociation;
import no.entur.uttu.repository.FixedLineRepository;
import no.entur.uttu.repository.FlexibleLineRepository;

import java.util.Map;

import static no.entur.uttu.graphql.GraphQLNames.FIELD_LINE_REF;

public class ExportLineAssociationMapper {
    private Export export;
    private FixedLineRepository fixedLineRepository;
    private FlexibleLineRepository flexibleLineRepository;

    public ExportLineAssociationMapper(Export export, FixedLineRepository fixedLineRepository, FlexibleLineRepository flexibleLineRepository) {
        this.export = export;
        this.fixedLineRepository = fixedLineRepository;
        this.flexibleLineRepository = flexibleLineRepository;
    }

    public ExportLineAssociation map(Map<String, Object> inputMap) {
        ArgumentWrapper input = new ArgumentWrapper(inputMap);
        ExportLineAssociation exportLineAssociation = new ExportLineAssociation();
        exportLineAssociation.setExport(export);

        String ref = input.get(FIELD_LINE_REF);
        if (ref.contains("FlexibleLine")) {
            input.applyReference(FIELD_LINE_REF, flexibleLineRepository, exportLineAssociation::setLine);
        } else {
            input.applyReference(FIELD_LINE_REF, fixedLineRepository, exportLineAssociation::setLine);
        }

        return exportLineAssociation;
    }
}
