package uk.gov.pay.api.model.telephone;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TelephonePaymentNotification {
    
    @JsonProperty("amount")
    private long amount;
    
    @JsonProperty("reference")
    private String reference;

    @JsonProperty("description")
    private String description;

    @JsonProperty("created_date")
    private String createdDate;

    @JsonProperty("authorised_date")
    private String authorisedDate;

    @JsonProperty("provider_id")
    private String providerId;

    @JsonProperty("auth_code")
    private String authCode;

    @JsonProperty("payment_outcome")
    private PaymentOutcome paymentOutcome;

    @JsonProperty("card_type")
    private String cardType;

    @JsonProperty("name_on_card")
    private String nameOnCard;

    @JsonProperty("email_address")
    private String emailAddress;

    @JsonProperty("card_expiry")
    private String cardExpiry;

    @JsonProperty("last_four_digits")
    private String lastFourDigits;

    @JsonProperty("first_six_digits")
    private String firstSixDigits;

    @JsonProperty("telephone_number")
    private String telephoneNumber;

    public TelephonePaymentNotification() {
        // To enable Jackson serialisation we need a default constructor
    }

    public long getAmount() {
        return amount;
    }

    public String getReference() {
        return reference;
    }

    public String getDescription() {
        return description;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public String getAuthorisedDate() {
        return authorisedDate;
    }

    public String getProviderId() {
        return providerId;
    }

    public String getAuthCode() {
        return authCode;
    }

    public PaymentOutcome getPaymentOutcome() {
        return paymentOutcome;
    }

    public String getCardType() {
        return cardType;
    }

    public String getNameOnCard() {
        return nameOnCard;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public String getCardExpiry() {
        return cardExpiry;
    }

    public String getLastFourDigits() {
        return lastFourDigits;
    }

    public String getFirstSixDigits() {
        return firstSixDigits;
    }

    public String getTelephoneNumber() {
        return telephoneNumber;
    }
}
