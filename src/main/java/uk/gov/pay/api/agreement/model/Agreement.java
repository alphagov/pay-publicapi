package uk.gov.pay.api.agreement.model;

public class Agreement {
    private String reference;
    
    private String agreementId;

    private Agreement(String agreementId, String reference) {
        this.agreementId = agreementId;
        this.reference = reference;
    }

    public static Agreement from(ConnectorAgreementResponse agreementResponse) {
        return new Agreement(agreementResponse.getAgreement_id(),
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
