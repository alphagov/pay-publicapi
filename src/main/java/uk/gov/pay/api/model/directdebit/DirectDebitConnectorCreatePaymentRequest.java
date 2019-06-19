package uk.gov.pay.api.model.directdebit;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DirectDebitConnectorCreatePaymentRequest {
    public final int amount;
    public final String reference;
    public final String description;
    public final String mandateId;

    private DirectDebitConnectorCreatePaymentRequest(DirectDebitConnectorCreatePaymentRequestBuilder builder) {
        this.amount = builder.amount;
        this.reference = builder.reference;
        this.description = builder.description;
        this.mandateId = builder.mandateId;
    }

    @JsonProperty("amount")
    public int getAmount() {
        return amount;
    }

    @JsonProperty("reference")
    public String getReference() {
        return reference;
    }

    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    @JsonProperty("mandate_id")
    public String getMandateId() {
        return mandateId;
    }

    public static final class DirectDebitConnectorCreatePaymentRequestBuilder {
        public int amount;
        public String reference;
        public String description;
        public String mandateId;

        private DirectDebitConnectorCreatePaymentRequestBuilder() {
        }

        public static DirectDebitConnectorCreatePaymentRequestBuilder aDirectDebitConnectorCreatePaymentRequest() {
            return new DirectDebitConnectorCreatePaymentRequestBuilder();
        }

        public DirectDebitConnectorCreatePaymentRequestBuilder withAmount(int amount) {
            this.amount = amount;
            return this;
        }

        public DirectDebitConnectorCreatePaymentRequestBuilder withReference(String reference) {
            this.reference = reference;
            return this;
        }

        public DirectDebitConnectorCreatePaymentRequestBuilder withDescription(String description) {
            this.description = description;
            return this;
        }

        public DirectDebitConnectorCreatePaymentRequestBuilder withMandateId(String mandateId) {
            this.mandateId = mandateId;
            return this;
        }

        public DirectDebitConnectorCreatePaymentRequest build() {
            return new DirectDebitConnectorCreatePaymentRequest(this);
        }
    }
}
