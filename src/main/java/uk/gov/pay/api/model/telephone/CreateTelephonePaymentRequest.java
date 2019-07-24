package uk.gov.pay.api.model.telephone;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.pay.api.validation.ValidCardExpiryDate;
import uk.gov.pay.api.validation.ValidCardFirstSixDigits;
import uk.gov.pay.api.validation.ValidCardLastFourDigits;
import uk.gov.pay.api.validation.ValidCardType;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class CreateTelephonePaymentRequest {

    public static final int REFERENCE_MAX_LENGTH = 255;
    public static final int AMOUNT_MAX_VALUE = 10000000;
    public static final int AMOUNT_MIN_VALUE = 0;
    public static final int DESCRIPTION_MAX_LENGTH = 255;
    
    @JsonProperty("amount")
    @Min(value = AMOUNT_MIN_VALUE, message = "Must be greater than or equal to 1")
    @Max(value = AMOUNT_MAX_VALUE, message = "Must be less than or equal to {value}")
    private int amount;
    
    @JsonProperty("reference")
    @Size(max = REFERENCE_MAX_LENGTH, message = "Must be less than or equal to {max} characters length")
    private String reference;

    @JsonProperty("description")
    @Size(max = DESCRIPTION_MAX_LENGTH, message = "Must be less than or equal to {max} characters length")
    private String description;

    @JsonProperty("created_date")
    private String createdDate;

    @JsonProperty("authorised_date")
    private String authorisedDate;
    
    @JsonProperty("processor_id")
    @NotNull
    private String processorId;

    @JsonProperty("provider_id")
    @NotNull
    private String providerId;

    @JsonProperty("auth_code")
    private String authCode;

    @JsonProperty("payment_outcome")
    private PaymentOutcome paymentOutcome;

    @JsonProperty("card_type")
    @ValidCardType
    private String cardType;

    @JsonProperty("name_on_card")
    private String nameOnCard;

    @JsonProperty("email_address")
    private String emailAddress;

    @JsonProperty("card_expiry")
    @ValidCardExpiryDate
    private String cardExpiry;

    @JsonProperty("last_four_digits")
    @ValidCardLastFourDigits
    private String lastFourDigits;

    @JsonProperty("first_six_digits")
    @ValidCardFirstSixDigits
    private String firstSixDigits;

    @JsonProperty("telephone_number")
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
}
