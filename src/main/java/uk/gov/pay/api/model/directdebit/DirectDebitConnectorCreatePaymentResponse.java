package uk.gov.pay.api.model.directdebit;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.pay.api.model.PaymentState;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class DirectDebitConnectorCreatePaymentResponse {
    @JsonProperty("links")
    private List<Map<String, Object>> dataLinks;

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

    @JsonProperty
    private PaymentState state;

    public DirectDebitConnectorCreatePaymentResponse() {
        // for Jackson
    }

    public String getPaymentProvider() {
        return paymentProvider;
    }

    public List<Map<String, Object>> getDataLinks() {
        return dataLinks;
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

    public URI getLink(String rel) {
        return dataLinks.stream()
                .filter(map -> rel.equals(map.get("rel")))
                .findFirst()
                .map(link -> (URI) link.get("href"))
                .get();
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public PaymentState getState() {
        return state;
    }

    @Override
    public int hashCode() {
        int result = dataLinks.hashCode();
        result = 31 * result + paymentExternalId.hashCode();
        result = 31 * result + amount.hashCode();
        result = 31 * result + paymentProvider.hashCode();
        result = 31 * result + description.hashCode();
        result = 31 * result + reference.hashCode();
        result = 31 * result + createdDate.hashCode();
        result = 31 * result + state.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DirectDebitConnectorCreatePaymentResponse that = (DirectDebitConnectorCreatePaymentResponse) o;
        return Objects.equals(dataLinks, that.dataLinks) &&
                Objects.equals(paymentExternalId, that.paymentExternalId) &&
                Objects.equals(amount, that.amount) &&
                Objects.equals(paymentProvider, that.paymentProvider) &&
                Objects.equals(description, that.description) &&
                Objects.equals(reference, that.reference) &&
                Objects.equals(createdDate, that.createdDate) &&
                Objects.equals(state, that.state);
    }

    @Override
    public String toString() {
        return "CollectPaymentResponse{" +
                "dataLinks=" + dataLinks +
                ", paymentExternalId='" + paymentExternalId + '\'' +
                ", state='" + state + '\'' +
                ", amount=" + amount +
                ", paymentProvider=" + paymentProvider +
                ", reference='" + reference + '\'' +
                ", createdDate=" + createdDate +
                '}';
    }

    public static final class DirectDebitConnectorCreatePaymentResponseBuilder {
        private List<Map<String, Object>> dataLinks;
        private String paymentExternalId;
        private Long amount;
        private String paymentProvider;
        private String description;
        private String reference;
        private String createdDate;
        private PaymentState state;

        private DirectDebitConnectorCreatePaymentResponseBuilder() {
        }

        public static DirectDebitConnectorCreatePaymentResponseBuilder aDirectDebitConnectorCreatePaymentResponse() {
            return new DirectDebitConnectorCreatePaymentResponseBuilder();
        }

        public DirectDebitConnectorCreatePaymentResponseBuilder withDataLinks(List<Map<String, Object>> dataLinks) {
            this.dataLinks = dataLinks;
            return this;
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

        public DirectDebitConnectorCreatePaymentResponse build() {
            DirectDebitConnectorCreatePaymentResponse directDebitConnectorCreatePaymentResponse = new DirectDebitConnectorCreatePaymentResponse();
            directDebitConnectorCreatePaymentResponse.createdDate = this.createdDate;
            directDebitConnectorCreatePaymentResponse.state = this.state;
            directDebitConnectorCreatePaymentResponse.dataLinks = this.dataLinks;
            directDebitConnectorCreatePaymentResponse.amount = this.amount;
            directDebitConnectorCreatePaymentResponse.paymentExternalId = this.paymentExternalId;
            directDebitConnectorCreatePaymentResponse.description = this.description;
            directDebitConnectorCreatePaymentResponse.reference = this.reference;
            directDebitConnectorCreatePaymentResponse.paymentProvider = this.paymentProvider;
            return directDebitConnectorCreatePaymentResponse;
        }
    }
}
