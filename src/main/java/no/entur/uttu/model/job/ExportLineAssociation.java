package no.entur.uttu.model.job;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import no.entur.uttu.model.Line;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
public class ExportLineAssociation {

  @Id
  @GeneratedValue(generator = "sequence_per_table_generator")
  protected Long id;

  @ManyToOne
  @NotNull
  private Export export;

  @ManyToOne
  @OnDelete(action = OnDeleteAction.CASCADE)
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
