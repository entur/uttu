package no.entur.uttu.model.job;

import no.entur.uttu.model.Line;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;

@Entity
public class ExportLineAssociation {
    @Id
    @GeneratedValue(generator = "sequence_per_table_generator")
    protected Long id;

    @ManyToOne
    @NotNull
    private Export export;

    @OneToOne
    @NotNull
    private Line line;

    public Long getId() {
        return id;
    }

    public Export getExport() {
        return export;
    }

    public void setExport(Export export) {
        this.export = export;
    }

    public Line getLine() {
        return line;
    }

    public void setLine(Line line) {
        this.line = line;
    }
}

