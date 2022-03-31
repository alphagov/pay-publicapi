package uk.gov.pay.api.agreement.model.builder;
import uk.gov.pay.api.agreement.model.AgreementResponse;

public class AgreementResponseBuilder {

    private String agreementId;
  
    private String reference;
  
    public String getAgreementId() {
        return agreementId;
    }

    public String getReference() {
        return reference;
    }

    public AgreementResponseBuilder withAgreementId(String agreementId) {
        this.agreementId = agreementId;
        return this;
    }
    
    public AgreementResponseBuilder withReference(String reference) {
        this.reference = reference;
        return this;
    }
    
    public AgreementResponse build() {
        return new AgreementResponse(this);
    }
}
