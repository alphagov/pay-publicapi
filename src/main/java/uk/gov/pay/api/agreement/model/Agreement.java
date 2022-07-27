package uk.gov.pay.api.agreement.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
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

    @JsonProperty("agreement_id")
    public String getExternalId() {
        return externalId;
    }

    public String getReference() {
        return reference;
    }

    public String getDescription() {
        return description;
    }

    public String getStatus() {
        return Optional.ofNullable(status).map(String::toLowerCase).orElse(null);
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public PaymentInstrument getPaymentInstrument() {
        return paymentInstrument;
    }

    public Agreement(String externalId, String reference, String description, String status, String createdDate, PaymentInstrument paymentInstrument) {
        this.externalId = externalId;
        this.reference = reference;
        this.description = description;
        this.status = status;
        this.createdDate = createdDate;
        this.paymentInstrument = paymentInstrument;
    }

    public static Agreement from(AgreementLedgerResponse agreementLedgerResponse) {
        return new Agreement(
                agreementLedgerResponse.getExternalId(),
                agreementLedgerResponse.getReference(),
                agreementLedgerResponse.getDescription(),
                agreementLedgerResponse.getStatus(),
                agreementLedgerResponse.getCreatedDate(),
                Optional.ofNullable(agreementLedgerResponse.getPaymentInstrument()).map(PaymentInstrument::from).orElse(null)
        );
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

        public CardDetails getCardDetails() {
            return cardDetails;
        }

        public String getCreatedDate() {
            return createdDate;
        }

        public String getType() {
            return Optional.ofNullable(type).map(String::toLowerCase).orElse(null);
        }
    }
}
