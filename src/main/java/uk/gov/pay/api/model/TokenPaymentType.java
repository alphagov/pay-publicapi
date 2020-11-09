package uk.gov.pay.api.model;

//to be shared between Public Auth and Public Api
public enum TokenPaymentType {
    CARD("Card Payment"), DIRECT_DEBIT("Direct Debit Payment");

    private String friendlyName;

    TokenPaymentType(String friendlyName) {
        this.friendlyName = friendlyName;
    }

    public String getFriendlyName() {
        return this.friendlyName;
    }
}
