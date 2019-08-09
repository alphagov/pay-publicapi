package uk.gov.pay.api.model.telephone;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.pay.api.validation.ValidPaymentOutcome;

import javax.validation.constraints.NotNull;
import java.util.Optional;

@ValidPaymentOutcome
public class PaymentOutcome {
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("code")
    private String code;
    
    @JsonProperty("supplemental")
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private Supplemental supplemental;

    public PaymentOutcome() {
    }

    public PaymentOutcome(String status, String code, Supplemental supplemental) {
        // For testing deserialization
        this.status = status;
        this.code = code;
        this.supplemental = supplemental;
    }

    public String getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }
    
    public Optional<Supplemental> getSupplemental() {
        return Optional.ofNullable(supplemental);
    }
}
