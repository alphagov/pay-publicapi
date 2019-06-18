package uk.gov.pay.api.model.directdebit;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.pay.api.model.PaymentState;

import java.util.Objects;

public class DirectDebitConnectorCreatePaymentResponse {
    @JsonProperty("charge_id")
    private String paymentExternalId;

    @JsonProperty
    private Long amount;

    @JsonProperty("payment_provider")
    private String paymentProvider;

    @JsonProperty
    private String description;

    @JsonProperty
    private String reference;

    @JsonProperty("created_date")
    private String createdDate;

    @JsonProperty("mandate_id")
    private String mandateId;
    
    @JsonProperty
    private PaymentState state;

    public DirectDebitConnectorCreatePaymentResponse() {
        // for Jackson
    }

    public String getPaymentProvider() {
        return paymentProvider;
    }

    public String getPaymentExternalId() {
        return paymentExternalId;
    }

    public Long getAmount() {
        return amount;
    }

    public String getDescription() {
        return description;
    }

    public String getReference() {
        return reference;
    }

    public String getCreatedDate() {
        return createdDate;
    }
    
    public String getMandateId() {
        return mandateId;
    }

    public PaymentState getState() {
        return state;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DirectDebitConnectorCreatePaymentResponse that = (DirectDebitConnectorCreatePaymentResponse) o;
        return Objects.equals(paymentExternalId, that.paymentExternalId) &&
                Objects.equals(amount, that.amount) &&
                Objects.equals(paymentProvider, that.paymentProvider) &&
                Objects.equals(description, that.description) &&
                Objects.equals(reference, that.reference) &&
                Objects.equals(createdDate, that.createdDate) &&
                Objects.equals(state, that.state);
    }

    @Override
    public int hashCode() {
        return Objects.hash(paymentExternalId, amount, paymentProvider, description, reference, createdDate, state);
    }

    @Override
    public String toString() {
        return "DirectDebitConnectorCreatePaymentResponse{" +
                "paymentExternalId='" + paymentExternalId + '\'' +
                ", amount=" + amount +
                ", paymentProvider='" + paymentProvider + '\'' +
                ", reference='" + reference + '\'' +
                ", createdDate='" + createdDate + '\'' +
                ", state=" + state +
                '}';
    }

    public static final class DirectDebitConnectorCreatePaymentResponseBuilder {
        private String paymentExternalId;
        private Long amount;
        private String paymentProvider;
        private String description;
        private String reference;
        private String createdDate;
        private String mandateId;
        private PaymentState state;

        private DirectDebitConnectorCreatePaymentResponseBuilder() {
        }

        public static DirectDebitConnectorCreatePaymentResponseBuilder aDirectDebitConnectorCreatePaymentResponse() {
            return new DirectDebitConnectorCreatePaymentResponseBuilder();
        }

        public DirectDebitConnectorCreatePaymentResponseBuilder withPaymentExternalId(String paymentExternalId) {
            this.paymentExternalId = paymentExternalId;
            return this;
        }

        public DirectDebitConnectorCreatePaymentResponseBuilder withAmount(Long amount) {
            this.amount = amount;
            return this;
        }

        public DirectDebitConnectorCreatePaymentResponseBuilder withPaymentProvider(String paymentProvider) {
            this.paymentProvider = paymentProvider;
            return this;
        }

        public DirectDebitConnectorCreatePaymentResponseBuilder withDescription(String description) {
            this.description = description;
            return this;
        }

        public DirectDebitConnectorCreatePaymentResponseBuilder withReference(String reference) {
            this.reference = reference;
            return this;
        }

        public DirectDebitConnectorCreatePaymentResponseBuilder withCreatedDate(String createdDate) {
            this.createdDate = createdDate;
            return this;
        }

        public DirectDebitConnectorCreatePaymentResponseBuilder withState(PaymentState state) {
            this.state = state;
            return this;
        }
        
        public DirectDebitConnectorCreatePaymentResponseBuilder withMandateId(String mandateId) {
            this.mandateId = mandateId;
            return this;
        }
 
        public DirectDebitConnectorCreatePaymentResponse build() {
            DirectDebitConnectorCreatePaymentResponse directDebitConnectorCreatePaymentResponse = new DirectDebitConnectorCreatePaymentResponse();
            directDebitConnectorCreatePaymentResponse.createdDate = this.createdDate;
            directDebitConnectorCreatePaymentResponse.state = this.state;
            directDebitConnectorCreatePaymentResponse.amount = this.amount;
            directDebitConnectorCreatePaymentResponse.paymentExternalId = this.paymentExternalId;
            directDebitConnectorCreatePaymentResponse.description = this.description;
            directDebitConnectorCreatePaymentResponse.reference = this.reference;
            directDebitConnectorCreatePaymentResponse.paymentProvider = this.paymentProvider;
            directDebitConnectorCreatePaymentResponse.mandateId = this.mandateId;
            return directDebitConnectorCreatePaymentResponse;
        }
    }
}
