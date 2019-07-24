package uk.gov.pay.api.model.telephone;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TelephonePaymentResponse {

    @JsonProperty("amount")
    private int amount;

    @JsonProperty("reference")
    private String reference;

    @JsonProperty("description")
    private String description;

    @JsonProperty("created_date")
    private String createdDate;

    @JsonProperty("authorised_date")
    private String authorisedDate;

    @JsonProperty("processor_id")
    private String processorId;

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

    @JsonProperty("state")
    private State state;

    public TelephonePaymentResponse() {
        // For Jackson
    }

    public TelephonePaymentResponse(int amount, String reference, String description, String createdDate, String authorisedDate, String processorId, String providerId, String authCode, PaymentOutcome paymentOutcome, String cardType, String nameOnCard, String emailAddress, String cardExpiry, String lastFourDigits, String firstSixDigits, String telephoneNumber, State state) {
        // For testing serialization
        this.amount = amount;
        this.reference = reference;
        this.description = description;
        this.createdDate = createdDate;
        this.authorisedDate = authorisedDate;
        this.processorId = processorId;
        this.providerId = providerId;
        this.authCode = authCode;
        this.paymentOutcome = paymentOutcome;
        this.cardType = cardType;
        this.nameOnCard = nameOnCard;
        this.emailAddress = emailAddress;
        this.cardExpiry = cardExpiry;
        this.lastFourDigits = lastFourDigits;
        this.firstSixDigits = firstSixDigits;
        this.telephoneNumber = telephoneNumber;
        this.state = state;
    }

    public int getAmount() {
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

    public String getProcessorId() {
        return processorId;
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

    public State getState() {
        return state;
    }
}
