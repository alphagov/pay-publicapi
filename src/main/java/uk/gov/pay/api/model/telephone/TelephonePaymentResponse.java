package uk.gov.pay.api.model.telephone;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import uk.gov.pay.api.model.PaymentState;

import java.util.Optional;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class TelephonePaymentResponse {
    
    private int amount;
    
    private String reference;
    
    private String description;
    
    @JsonProperty("created_date")
    private String createdDate;

    @JsonProperty("authorised_date")
    private String authorisedDate;
    
    private String processorId;
    
    private String providerId;

    @JsonProperty("auth_code")
    private String authCode;
    
    private PaymentOutcome paymentOutcome;
    
    private String cardType;

    @JsonProperty("name_on_card")
    private String nameOnCard;

    @JsonProperty("email_address")
    private String emailAddress;
    
    private String cardExpiry;
    
    private String lastFourDigits;
    
    private String firstSixDigits;

    @JsonProperty("telephone_number")
    private String telephoneNumber;
    
    private String paymentId;
    
    private PaymentState state;

    public TelephonePaymentResponse() {
        // For Jackson
    }

    public TelephonePaymentResponse(int amount, 
                                    String reference, 
                                    String description, 
                                    String createdDate, 
                                    String authorisedDate, 
                                    String processorId, 
                                    String providerId, 
                                    String authCode, 
                                    PaymentOutcome paymentOutcome, 
                                    String cardType, 
                                    String nameOnCard, 
                                    String emailAddress, 
                                    String cardExpiry, 
                                    String lastFourDigits, 
                                    String firstSixDigits, 
                                    String telephoneNumber, 
                                    String paymentId, 
                                    PaymentState state) {
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
        this.paymentId = paymentId;
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

    @JsonIgnore
    public Optional<String> getCreatedDate() {
        return Optional.ofNullable(createdDate);
    }

    @JsonIgnore
    public Optional<String> getAuthorisedDate() {
        return Optional.ofNullable(authorisedDate);
    }

    public String getProcessorId() {
        return processorId;
    }

    public String getProviderId() {
        return providerId;
    }

    @JsonIgnore
    public Optional<String> getAuthCode() {
        return Optional.ofNullable(authCode);
    }

    public PaymentOutcome getPaymentOutcome() {
        return paymentOutcome;
    }

    public String getCardType() {
        return cardType;
    }

    @JsonIgnore
    public Optional<String> getNameOnCard() {
        return Optional.ofNullable(nameOnCard);
    }

    @JsonIgnore
    public Optional<String> getEmailAddress() {
        return Optional.ofNullable(emailAddress);
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

    @JsonIgnore
    public Optional<String> getTelephoneNumber() {
        return Optional.ofNullable(telephoneNumber);
    }

    public String getPaymentId() { return paymentId; }

    public PaymentState getState() {
        return state;
    }
}
