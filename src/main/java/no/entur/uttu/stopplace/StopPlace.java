package no.entur.uttu.stopplace;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class StopPlace {

    private String id;
    private MultilingualString name;
    private List<Quay> quays;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public MultilingualString getName() {
        return name;
    }

    public void setName(MultilingualString name) {
        this.name = name;
    }

    public List<Quay> getQuays() {
        return quays;
    }

    public void setQuays(List<Quay> quays) {
        this.quays = quays;
    }
}
