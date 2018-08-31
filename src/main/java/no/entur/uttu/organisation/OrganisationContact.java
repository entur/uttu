package no.entur.uttu.organisation;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrganisationContact {

    public String url;
    public String email;
    public String phone;



}
