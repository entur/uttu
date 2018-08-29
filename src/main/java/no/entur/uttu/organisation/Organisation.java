package no.entur.uttu.organisation;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Organisation {

    public long id;
    public String name;
    public Map<String, String> references;
    public List<String> types; // TODO enum? no...


}
