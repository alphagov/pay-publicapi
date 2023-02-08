package uk.gov.pay.api.agreement.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import uk.gov.pay.api.model.CardDetails;

import java.util.Optional;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class Agreement {
    private String externalId;
    private String reference;
    private String description;
    private String status;
    private String createdDate;
    private PaymentInstrument paymentInstrument;
    private String userIdentifier;

    @JsonProperty("agreement_id")
    @Schema(description = "The unique ID GOV.UK Pay automatically associated with this agreement when you created it.", 
            example = "cgc1ocvh0pt9fqs0ma67r42l58")
    public String getExternalId() {
        return externalId;
    }

    @Schema(description = "The reference you sent when creating this agreement.",
            example = "CT-22-23-0001")
    public String getReference() {
        return reference;
    }

    @Schema(description = "The description you sent when creating this agreement.",
            example = "Dorset Council 2022/23 council tax subscription.")
    public String getDescription() {
        return description;
    }

    @Schema(description = "The status of this agreement. " +
            "`status value` will automatically be set to created because you’ve just created this agreement. " +
            "You can [read more about the meanings of each agreement status.](https://docs.payments.service.gov.uk/recurring_payments/#understanding-agreement-status)",
            allowableValues = {"created", "active", "cancelled", "expired"})
    public String getStatus() {
        return Optional.ofNullable(status).map(String::toLowerCase).orElse(null);
    }
    
    @Schema(description = "The date and time you created this agreement. " +
            "This value uses Coordinated Universal Time (UTC) and ISO 8601 format – `YYYY-MM-DDThh:mm:ss.sssZ`.",
            example = "2022-07-08T14:33:00.000Z")
    public String getCreatedDate() {
        return createdDate;
    }

    @Schema(description = "The identifier you sent when creating this agreement. " +
            "`user_identifier` helps you identify users in your records.",
            example = "user-3fb81107-76b7-4910")
    public String getUserIdentifier() {
        return userIdentifier;
    }

    public PaymentInstrument getPaymentInstrument() {
        return paymentInstrument;
    }

    public Agreement(String externalId, String reference, String description, String status, String createdDate, PaymentInstrument paymentInstrument, String userIdentifier) {
        this.externalId = externalId;
        this.reference = reference;
        this.description = description;
        this.status = status;
        this.createdDate = createdDate;
        this.paymentInstrument = paymentInstrument;
        this.userIdentifier = userIdentifier;
    }

    public static Agreement from(AgreementLedgerResponse agreementLedgerResponse) {
        return new Agreement(
                agreementLedgerResponse.getExternalId(),
                agreementLedgerResponse.getReference(),
                agreementLedgerResponse.getDescription(),
                agreementLedgerResponse.getStatus(),
                agreementLedgerResponse.getCreatedDate(),
                Optional.ofNullable(agreementLedgerResponse.getPaymentInstrument()).map(PaymentInstrument::from).orElse(null),
                agreementLedgerResponse.getUserIdentifier());
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class PaymentInstrument {
        private final CardDetails cardDetails;
        private final String createdDate;
        private final String type;

        public PaymentInstrument(CardDetails cardDetails, String createdDate, String type) {
            this.cardDetails = cardDetails;
            this.createdDate = createdDate;
            this.type = type;
        }

        public static PaymentInstrument from(AgreementLedgerResponse.PaymentInstrumentLedgerResponse paymentInstrumentLedgerResponse) {
            return new PaymentInstrument(paymentInstrumentLedgerResponse.getCardDetails(), paymentInstrumentLedgerResponse.getCreatedDate(), paymentInstrumentLedgerResponse.getType());
        }

        
        @Schema(name = "CardDetails")
        public CardDetails getCardDetails() {
            return cardDetails;
        }

        @Schema(description = "The date and time you created this payment instrument. " +
                "This value uses Coordinated Universal Time (UTC) and ISO 8601 format – `YYYY-MM-DDThh:mm:ss.sssZ`.",
                example = "2022-07-08T14:33:00.000Z")
        public String getCreatedDate() {
            return createdDate;
        }

        @Schema(description = "The type of payment instrument.",
                example = "card")
        public String getType() {
            return Optional.ofNullable(type).map(String::toLowerCase).orElse(null);
        }
    }
}
