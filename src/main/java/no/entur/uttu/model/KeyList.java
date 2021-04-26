package no.entur.uttu.model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.List;

@Entity
public class KeyList extends IdentifiedEntity{

    @OneToMany(mappedBy = "keyList", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<KeyValue> keyValue;

    public List<KeyValue> getKeyValue() {
        return keyValue;
    }

    public void setKeyValue(List<KeyValue> keyValue) {
        this.keyValue = keyValue;
    }
}
