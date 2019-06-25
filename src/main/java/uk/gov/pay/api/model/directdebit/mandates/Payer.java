package uk.gov.pay.api.model.directdebit.mandates;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Payer {
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("email")
    private String email;

    public Payer() {
    }

    public Payer(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }
}
