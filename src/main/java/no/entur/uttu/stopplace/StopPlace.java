package no.entur.uttu.stopplace;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class StopPlace {

    private String id;
    private String name;
    private List<Quay> quays;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Quay> getQuays() {
        return quays;
    }

    public void setQuays(List<Quay> quays) {
        this.quays = quays;
    }
}
