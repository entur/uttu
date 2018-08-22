package no.entur.uttu.model;

import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

@Entity
public class CodeSpace extends IdentifiedEntity {

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
