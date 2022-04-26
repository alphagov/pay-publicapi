package uk.gov.pay.api.model;

public class AuthorisationAPIRequestBuilder {
    private String oneTimeToken;
    private String cardNumber;
    private String cvc;
    private String expiryDate;
    private String cardholderName;

    public static AuthorisationAPIRequestBuilder builder() {
        return new AuthorisationAPIRequestBuilder();
    }

    public AuthorisationAPIRequest build() {
        return new AuthorisationAPIRequest(this);
    }

    public String getOneTimeToken() {
        return oneTimeToken;
    }

    public AuthorisationAPIRequestBuilder oneTimeToken(String oneTimeToken) {
        this.oneTimeToken = oneTimeToken;
        return this;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public AuthorisationAPIRequestBuilder cardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
        return this;
    }

    public String getCvc() {
        return cvc;
    }

    public AuthorisationAPIRequestBuilder cvc(String cvc) {
        this.cvc = cvc;
        return this;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public AuthorisationAPIRequestBuilder expiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
        return this;
    }

    public String getCardholderName() {
        return cardholderName;
    }

    public AuthorisationAPIRequestBuilder cardholderName(String cardholderName) {
        this.cardholderName = cardholderName;
        return this;
    }
}
