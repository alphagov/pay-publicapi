package uk.gov.pay.api.agreement.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import uk.gov.pay.api.agreement.model.builder.AgreementResponseBuilder;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class AgreementResponse {

    @JsonProperty("agreement_id")
    private String agreement_id;

      @JsonProperty("reference")
    private String reference;
      
      public AgreementResponse(){}
  
    public AgreementResponse(AgreementResponseBuilder agreementResponseBuilder) {
        this.agreement_id = agreementResponseBuilder.getAgreementId();
        this.reference = agreementResponseBuilder.getReference();
    }
    
    public String getAgreement_id() {
        return agreement_id;
    }
    
    public String getReference() {
        return reference;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AgreementResponse that = (AgreementResponse) o;
        return agreement_id.equals(that.agreement_id)  && reference.equals(that.reference);
    }

    @Override
    public int hashCode() {
        return Objects.hash(agreement_id,  reference);
    }

    @Override
    public String toString() {
        return "AgreementResponse{" +
                "agreementId='" + agreement_id + '\'' +
                ", reference='" + reference + '\'' +
                '}';
    }
}
