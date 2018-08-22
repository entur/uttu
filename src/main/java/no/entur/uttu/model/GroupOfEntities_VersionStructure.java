package no.entur.uttu.model;

import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class GroupOfEntities_VersionStructure
        extends ProviderEntity {

    protected String name;

    protected String shortName;

    protected String description;

    protected String privateCode;

    public GroupOfEntities_VersionStructure() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPrivateCode() {
        return privateCode;
    }

    public void setPrivateCode(String value) {
        this.privateCode = value;
    }


}