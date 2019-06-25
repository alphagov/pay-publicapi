package uk.gov.pay.api.model.directdebit.mandates;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Payer {
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("email")
    private String email;

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }
}
