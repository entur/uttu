package no.entur.uttu.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

@Entity
public class KeyValue extends IdentifiedEntity {

    @ManyToOne @NotNull
    private KeyList keylist;

    private String typeOfKey;

    @Column(name = "key_value_key")
    private String key;

    @Column(name = "key_value_value")
    private String value;

    public String getTypeOfKey() {
        return typeOfKey;
    }

    public void setTypeOfKey(String typeOfKey) {
        this.typeOfKey = typeOfKey;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
