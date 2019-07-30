package uk.gov.pay.api.model.telephone;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class TelephonePaymentResponse {
    
    private int amount;
    
    private String reference;
    
    private String description;
    
    private String createdDate;
    
    private String authorisedDate;
    
    private String processorId;
    
    private String providerId;
    
    private String authCode;
    
    private PaymentOutcome paymentOutcome;
    
    private String cardType;
    
    private String nameOnCard;
    
    private String emailAddress;
    
    private String cardExpiry;
    
    private String lastFourDigits;
    
    private String firstSixDigits;
    
    private String telephoneNumber;
    
    private String paymentId;
    
    private State state;

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
                                    State state) {
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

    public String getPaymentId() { return paymentId; }

    public State getState() {
        return state;
    }
}
