package no.entur.uttu.model;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

@Entity
@Table(
        uniqueConstraints = {
                                    @UniqueConstraint(name = "codespace_xmlns", columnNames = {"xmlns"})}
)
public class Codespace extends IdentifiedEntity {

    @NotNull
    private String xmlns;

    @NotNull
    private String xmlnsUrl;

    public String getXmlnsUrl() {
        return xmlnsUrl;
    }

    public void setXmlnsUrl(String xmlnsUrl) {
        this.xmlnsUrl = xmlnsUrl;
    }

    public String getXmlns() {
        return xmlns;
    }

    public void setXmlns(String xmlns) {
        this.xmlns = xmlns;
    }
}
