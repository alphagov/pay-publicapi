package uk.gov.pay.api.model.telephone;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import uk.gov.pay.api.model.ChargeFromResponse;
import uk.gov.pay.api.model.PaymentState;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class TelephonePaymentResponse {
    
    private Long amount;
    
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
    
    private PaymentState state;

    public TelephonePaymentResponse() {
        // For Jackson
    }
    
    public TelephonePaymentResponse(Builder builder) {
        this.amount = builder.amount;
        this.reference = builder.reference;
        this.description = builder.description;
        this.createdDate = builder.createdDate;
        this.authorisedDate = builder.authorisedDate;
        this.processorId = builder.processorId;
        this.providerId = builder.providerId;
        this.authCode = builder.authCode;
        this.paymentOutcome = builder.paymentOutcome;
        this.cardType = builder.cardType;
        this.nameOnCard = builder.nameOnCard;
        this.emailAddress = builder.emailAddress;
        this.cardExpiry = builder.cardExpiry;
        this.lastFourDigits = builder.lastFourDigits;
        this.firstSixDigits = builder.firstSixDigits;
        this.telephoneNumber = builder.telephoneNumber;
        this.paymentId = builder.paymentId;
        this.state = builder.state;
    }
    
    public TelephonePaymentResponse(Long amount, 
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

    public Long getAmount() {
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

    public PaymentState getState() {
        return state;
    }
    
    public static TelephonePaymentResponse from(ChargeFromResponse chargeFromResponse) {
        return new TelephonePaymentResponse.Builder()
                .withAmount(chargeFromResponse.getAmount())
                .withReference(chargeFromResponse.getReference())
                .withCreatedDate(chargeFromResponse.getCreatedDate())
                .withAuthorisedDate(chargeFromResponse.getAuthorisedDate())
                .withProcessorId(chargeFromResponse.getProcessorId())
                .withProviderId(chargeFromResponse.getProviderId())
                .withAuthCode(chargeFromResponse.getAuthCode())
                .withPaymentOutcome(chargeFromResponse.getPaymentOutcome())
                .withCardType(chargeFromResponse.getCardBrand())
                .withNameOnCard(chargeFromResponse.getCardDetails().getCardHolderName())
                .withEmailAddress(chargeFromResponse.getEmail())
                .withCardExpiry(chargeFromResponse.getCardDetails().getExpiryDate())
                .withLastFourDigits(chargeFromResponse.getCardDetails().getLastDigitsCardNumber())
                .withFirstSixDigits(chargeFromResponse.getCardDetails().getFirstDigitsCardNumber())
                .withTelephoneNumber(chargeFromResponse.getTelephoneNumber())
                .withPaymentId(chargeFromResponse.getChargeId())
                .withState(chargeFromResponse.getState())
                .build();
    }
    
    public static class Builder {
        private Long amount;
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
        private PaymentState state;

        public Builder withAmount(Long amount) {
            this.amount = amount;
            return this;
        }

        public Builder withReference(String reference) {
            this.reference = reference;
            return this;
        }

        public Builder withDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder withCreatedDate(String createdDate) {
            this.createdDate = createdDate;
            return this;
        }

        public Builder withAuthorisedDate(String authorisedDate) {
            this.authorisedDate = authorisedDate;
            return this;
        }

        public Builder withProcessorId(String processorId) {
            this.processorId = processorId;
            return this;
        }

        public Builder withProviderId(String providerId) {
            this.providerId = providerId;
            return this;
        }

        public Builder withAuthCode(String authCode) {
            this.authCode = authCode;
            return this;
        }

        public Builder withPaymentOutcome(PaymentOutcome paymentOutcome) {
            this.paymentOutcome = paymentOutcome;
            return this;
        }

        public Builder withCardType(String cardType) {
            this.cardType = cardType;
            return this;
        }

        public Builder withNameOnCard(String nameOnCard) {
            this.nameOnCard = nameOnCard;
            return this;
        }

        public Builder withEmailAddress(String emailAddress) {
            this.emailAddress = emailAddress;
            return this;
        }

        public Builder withCardExpiry(String cardExpiry) {
            this.cardExpiry = cardExpiry;
            return this;
        }

        public Builder withLastFourDigits(String lastFourDigits) {
            this.lastFourDigits = lastFourDigits;
            return this;
        }

        public Builder withFirstSixDigits(String firstSixDigits) {
            this.firstSixDigits = firstSixDigits;
            return this;
        }

        public Builder withTelephoneNumber(String telephoneNumber) {
            this.telephoneNumber = telephoneNumber;
            return this;
        }

        public Builder withPaymentId(String paymentId) {
            this.paymentId = paymentId;
            return this;
        }

        public Builder withState(PaymentState state) {
            this.state = state;
            return this;
        }
        
        public TelephonePaymentResponse build () {
            return new TelephonePaymentResponse(this);
        }
    }
}
