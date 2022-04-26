package uk.gov.pay.api.model;

public class AuthorisationRequestBuilder {
    private String oneTimeToken;
    private String cardNumber;
    private String cvc;
    private String expiryDate;
    private String cardholderName;

    public static AuthorisationRequestBuilder builder() {
        return new AuthorisationRequestBuilder();
    }

    public AuthorisationRequest build() {
        return new AuthorisationRequest(this);
    }

    public String getOneTimeToken() {
        return oneTimeToken;
    }

    public AuthorisationRequestBuilder oneTimeToken(String oneTimeToken) {
        this.oneTimeToken = oneTimeToken;
        return this;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public AuthorisationRequestBuilder cardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
        return this;
    }

    public String getCvc() {
        return cvc;
    }

    public AuthorisationRequestBuilder cvc(String cvc) {
        this.cvc = cvc;
        return this;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public AuthorisationRequestBuilder expiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
        return this;
    }

    public String getCardholderName() {
        return cardholderName;
    }

    public AuthorisationRequestBuilder cardholderName(String cardholderName) {
        this.cardholderName = cardholderName;
        return this;
    }
}
