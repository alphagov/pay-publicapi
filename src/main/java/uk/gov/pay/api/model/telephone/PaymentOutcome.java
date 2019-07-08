package uk.gov.pay.api.model.telephone;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PaymentOutcome {
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("code")
    private String code;
    
    @JsonProperty("supplemental")
    private Supplemental supplemental;

    public PaymentOutcome() {
        //
    }

    public String getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    public Supplemental getSupplemental() {
        return supplemental;
    }
}
