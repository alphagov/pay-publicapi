package uk.gov.pay.api.agreement.model;

public class Agreement {
    private final String reference;
    private final String agreementId;

    private Agreement(String agreementId, String reference) {
        this.agreementId = agreementId;
        this.reference = reference;
    }

    public static Agreement from(ConnectorAgreementResponse agreementResponse) {
        return new Agreement(agreementResponse.getAgreementId(),
                agreementResponse.getReference()
        );
    }

    public String getAgreementId() {
        return agreementId;
    }

    public String getReference() {
        return reference;
    }
}
