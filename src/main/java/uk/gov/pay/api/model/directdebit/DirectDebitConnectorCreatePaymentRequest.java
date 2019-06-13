package uk.gov.pay.api.model.directdebit;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DirectDebitConnectorCreatePaymentRequest {
    public final int amount;
    public final String reference;
    public final String description;
    public final String agreementId;

    private DirectDebitConnectorCreatePaymentRequest(DirectDebitConnectorCreatePaymentRequestBuilder builder) {
        this.amount = builder.amount;
        this.reference = builder.reference;
        this.description = builder.description;
        this.agreementId = builder.agreementId;
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

    @JsonProperty("agreement_id")
    public String getAgreementId() {
        return agreementId;
    }

    public static final class DirectDebitConnectorCreatePaymentRequestBuilder {
        public int amount;
        public String reference;
        public String description;
        public String agreementId;

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

        public DirectDebitConnectorCreatePaymentRequestBuilder withAgreementId(String agreementId) {
            this.agreementId = agreementId;
            return this;
        }

        public DirectDebitConnectorCreatePaymentRequest build() {
            return new DirectDebitConnectorCreatePaymentRequest(this);
        }
    }
}
