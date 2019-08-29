package uk.gov.pay.api.model.telephone;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import uk.gov.pay.api.validation.ValidCardExpiryDate;
import uk.gov.pay.api.validation.ValidCardFirstSixDigits;
import uk.gov.pay.api.validation.ValidCardLastFourDigits;
import uk.gov.pay.api.validation.ValidCardType;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class CreateTelephonePaymentRequest {

    public static final int REFERENCE_MAX_LENGTH = 255;
    public static final int AMOUNT_MAX_VALUE = 10000000;
    public static final int AMOUNT_MIN_VALUE = 0;
    public static final int DESCRIPTION_MAX_LENGTH = 255;
    
    @Min(value = AMOUNT_MIN_VALUE, message = "Must be greater than or equal to 1")
    @Max(value = AMOUNT_MAX_VALUE, message = "Must be less than or equal to {value}")
    private int amount;
    
    @Size(max = REFERENCE_MAX_LENGTH, message = "Must be less than or equal to {max} characters length")
    private String reference;
    
    @Size(max = DESCRIPTION_MAX_LENGTH, message = "Must be less than or equal to {max} characters length")
    private String description;
    
    private String createdDate;
    
    private String authorisedDate;
    
    @NotNull(message = "Field [processor_id] cannot be null")
    private String processorId;
    
    @NotNull(message = "Field [provider_id] cannot be null")
    private String providerId;
    
    private String authCode;
    
    @Valid
    @NotNull(message = "Field [payment_outcome] cannot be null")
    private PaymentOutcome paymentOutcome;
    
    @NotNull(message = "Field [card_type] cannot be null")
    @ValidCardType(message = "Field [card_type] must be either master-card, visa, maestro, diners-club or american-express")
    private String cardType;
    
    private String nameOnCard;
    
    private String emailAddress;

    @NotNull(message = "Field [card_expiry] cannot be null")
    @ValidCardExpiryDate(message = "Field [card_expiry] must have valid MM/YY")
    private String cardExpiry;
    
    @NotNull(message = "Field [last_four_digits] cannot be null")
    @ValidCardLastFourDigits(message = "Field [last_four_digits] must be exactly 4 digits")
    private String lastFourDigits;
    
    @NotNull(message = "Field [first_six_digits_ cannot be null")
    @ValidCardFirstSixDigits(message = "Field [first_six_digits] must be exactly 6 digits")
    private String firstSixDigits;
    
    private String telephoneNumber;

    public CreateTelephonePaymentRequest() {
        // To enable Jackson serialisation we need a default constructor
    }

    public CreateTelephonePaymentRequest(int amount, String reference, String description, String createdDate, String authorisedDate, String processorId, String providerId, String authCode, PaymentOutcome paymentOutcome, String cardType, String nameOnCard, String emailAddress, String cardExpiry, String lastFourDigits, String firstSixDigits, String telephoneNumber) {
        // For testing deserialization
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
    }
    
    public CreateTelephonePaymentRequest(TelephoneRequestBuilder builder) {
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

    public static class TelephoneRequestBuilder {
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

        public TelephoneRequestBuilder amount(int amount) {
            this.amount = amount;
            return this;
        }

        public TelephoneRequestBuilder reference(String reference) {
            this.reference = reference;
            return this;
        }

        public TelephoneRequestBuilder description(String description) {
            this.description = description;
            return this;
        }

        public TelephoneRequestBuilder createdDate(String createdDate) {
            this.createdDate = createdDate;
            return this;
        }

        public TelephoneRequestBuilder authorisedDate(String authorisedDate) {
            this.authorisedDate = authorisedDate;
            return this;
        }

        public TelephoneRequestBuilder processorId(String processorId) {
            this.processorId = processorId;
            return this;
        }

        public TelephoneRequestBuilder providerId(String providerId) {
            this.providerId = providerId;
            return this;
        }

        public TelephoneRequestBuilder authCode(String authCode) {
            this.authCode = authCode;
            return this;
        }

        public TelephoneRequestBuilder paymentOutcome(PaymentOutcome paymentOutcome) {
            this.paymentOutcome = paymentOutcome;
            return this;
        }

        public TelephoneRequestBuilder cardType(String cardType) {
            this.cardType = cardType;
            return this;
        }

        public TelephoneRequestBuilder nameOnCard(String nameOnCard) {
            this.nameOnCard = nameOnCard;
            return this;
        }

        public TelephoneRequestBuilder emailAddress(String emailAddress) {
            this.emailAddress = emailAddress;
            return this;
        }

        public TelephoneRequestBuilder cardExpiry(String cardExpiry) {
            this.cardExpiry = cardExpiry;
            return this;
        }

        public TelephoneRequestBuilder lastFourDigits(String lastFourDigits) {
            this.lastFourDigits = lastFourDigits;
            return this;
        }

        public TelephoneRequestBuilder firstSixDigits(String firstSixDigits) {
            this.firstSixDigits = firstSixDigits;
            return this;
        }

        public TelephoneRequestBuilder telephoneNumber(String telephoneNumber) {
            this.telephoneNumber = telephoneNumber;
            return this;
        }

        public CreateTelephonePaymentRequest build() {
            return new CreateTelephonePaymentRequest(this);
        }
    }
}
