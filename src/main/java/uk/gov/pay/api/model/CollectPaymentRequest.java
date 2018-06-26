package uk.gov.pay.api.model;

public class CollectPaymentRequest extends CreatePaymentRequest {
    private final String agreementId;

    public CollectPaymentRequest(int amount, String reference, String description, String agreementId) {
        super(amount, "", reference, description);

        this.agreementId = agreementId;
    }

    public String getAgreementId() {
        return agreementId;
    }
}
