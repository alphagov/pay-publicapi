package uk.gov.pay.api.model;

import uk.gov.pay.api.agreement.model.CreateAgreementRequest;

public class CreateAgreementRequestBuilder {
    private String reference;
    public static CreateAgreementRequestBuilder builder() {
        return new CreateAgreementRequestBuilder();
    }

    public CreateAgreementRequest build() {
        return new CreateAgreementRequest(this);
    }

    public CreateAgreementRequestBuilder reference(String reference) {
        this.reference = reference;
        return this;
    }

    public String getReference() {
        return reference;
    }
}
