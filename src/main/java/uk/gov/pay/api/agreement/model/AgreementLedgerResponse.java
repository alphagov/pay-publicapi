package uk.gov.pay.api.agreement.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import uk.gov.pay.api.model.CardDetails;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class AgreementLedgerResponse {
    private String externalId;
    private String serviceId;
    private String reference;
    private String description;
    private String status;
    private String createdDate;
    private PaymentInstrumentLedgerResponse paymentInstrument;

    @JsonProperty("external_id")
    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    @JsonProperty("id")
    public String getExternalId() {
        return externalId;
    }

    public String getServiceId() {
        return serviceId;
    }

    public String getReference() {
        return reference;
    }

    public String getDescription() {
        return description;
    }

    public String getStatus() {
        return status;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public PaymentInstrumentLedgerResponse getPaymentInstrument() {
        return paymentInstrument;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public class PaymentInstrumentLedgerResponse {
        private String externalId;
        private String agreementExternalId;
        private CardDetails cardDetails;
        private String createdDate;
        private String type;

        public String getExternalId() {
            return externalId;
        }

        public String getAgreementExternalId() {
            return agreementExternalId;
        }

        public CardDetails getCardDetails() {
            return cardDetails;
        }

        public String getCreatedDate() {
            return createdDate;
        }

        public String getType() {
            return type;
        }
    }
}
