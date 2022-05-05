package uk.gov.pay.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class AuthorisationRequest {
    @JsonProperty("one_time_token")
    @NotBlank
    private String oneTimeToken;
    @JsonProperty("card_number")
    @NotNull
    @Size(min = 12, max = 19, message = "Must be between 12 and 19 characters long")
    private String cardNumber;
    @JsonProperty("cvc")
    @NotNull
    @Size(min = 3, max = 4, message = "Must be between 3 and 4 characters long")
    private String cvc;
    @JsonProperty("expiry_date")
    @NotNull
    @Size(min = 5, max = 5, message = "Must be a valid date with the format MM/YY")
    private String expiryDate;
    @JsonProperty("cardholder_name")
    @NotBlank
    @Size(max = 255, message = "Must be less than or equal to {max} characters long")
    private String cardholderName;

    public AuthorisationRequest() {
    }

    public AuthorisationRequest(String oneTimeToken, String cardNumber, String cvc, String expiryDate, String cardholderName) {
        this.oneTimeToken = oneTimeToken;
        this.cardNumber = cardNumber;
        this.cvc = cvc;
        this.expiryDate = expiryDate;
        this.cardholderName = cardholderName;
    }

    public String getOneTimeToken() {
        return oneTimeToken;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public String getCvc() {
        return cvc;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public String getCardholderName() {
        return cardholderName;
    }
}
