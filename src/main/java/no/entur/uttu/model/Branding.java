package no.entur.uttu.model;

import jakarta.persistence.Entity;
import no.entur.uttu.util.Preconditions;

@Entity
public class Branding extends ProviderEntity {
    private String name;
    private String shortName;
    private String description;
    private String url;
    private String imageUrl;

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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public void checkPersistable() {
        super.checkPersistable();

        Preconditions.checkArgument(
                getName() != null && !getName().isEmpty(),
                "%s branding must have a non-empty name",
                this
        );
    }
}
