package uk.gov.pay.api.model.directdebit.mandates;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.pay.api.model.PaymentConnectorResponseLink;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MandateConnectorResponse {

    private String mandateId;
    private String mandateReference;
    private String serviceReference;
    private String returnUrl;
    private String createdDate;
    private String paymentProvider;
    private MandateState state;
    private List<PaymentConnectorResponseLink> links;
    private String description;
    private String providerId;
    private Payer payer;

    @JsonProperty("payer")
    public Payer getPayer() {
        return payer;
    }

    @JsonProperty("payment_provider")
    public String getPaymentProvider() {
        return paymentProvider;
    }

    @JsonProperty("mandate_id")
    public String getMandateId() {
        return mandateId;
    }

    @JsonProperty("mandate_reference")
    public String getMandateReference() { return mandateReference; }

    @JsonProperty("service_reference")
    public String getServiceReference() {
        return serviceReference;
    }

    @JsonProperty("return_url")
    public String getReturnUrl() {
        return returnUrl;
    }

    @JsonProperty("created_date")
    public String getCreatedDate() {
        return createdDate;
    }

    @JsonProperty("state")
    public MandateState getState() {
        return state;
    }

    @JsonProperty("links")
    public List<PaymentConnectorResponseLink> getLinks() {
        return links;
    }

    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    @JsonProperty("provider_id")
    public String getProviderId() {
        return providerId;
    }


    public static final class MandateConnectorResponseBuilder {
        private String mandateId;
        private String mandateReference;
        private String serviceReference;
        private String returnUrl;
        private String createdDate;
        private String paymentProvider;
        private MandateState state;
        private List<PaymentConnectorResponseLink> links;
        private String description;
        private String providerId;
        private Payer payer;

        private MandateConnectorResponseBuilder() {
        }

        public static MandateConnectorResponseBuilder aMandateConnectorResponse() {
            return new MandateConnectorResponseBuilder();
        }

        public MandateConnectorResponseBuilder withMandateId(String mandateId) {
            this.mandateId = mandateId;
            return this;
        }

        public MandateConnectorResponseBuilder withMandateReference(String mandateReference) {
            this.mandateReference = mandateReference;
            return this;
        }

        public MandateConnectorResponseBuilder withServiceReference(String serviceReference) {
            this.serviceReference = serviceReference;
            return this;
        }

        public MandateConnectorResponseBuilder withReturnUrl(String returnUrl) {
            this.returnUrl = returnUrl;
            return this;
        }

        public MandateConnectorResponseBuilder withCreatedDate(String createdDate) {
            this.createdDate = createdDate;
            return this;
        }

        public MandateConnectorResponseBuilder withPaymentProvider(String paymentProvider) {
            this.paymentProvider = paymentProvider;
            return this;
        }

        public MandateConnectorResponseBuilder withState(MandateState state) {
            this.state = state;
            return this;
        }

        public MandateConnectorResponseBuilder withLinks(List<PaymentConnectorResponseLink> links) {
            this.links = List.copyOf(links);
            return this;
        }

        public MandateConnectorResponseBuilder withDescription(String description) {
            this.description = description;
            return this;
        }

        public MandateConnectorResponseBuilder withProviderId(String providerId) {
            this.providerId = providerId;
            return this;
        }

        public MandateConnectorResponseBuilder withPayer(Payer payer) {
            this.payer = payer;
            return this;
        }

        public MandateConnectorResponse build() {
            MandateConnectorResponse mandateConnectorResponse = new MandateConnectorResponse();
            mandateConnectorResponse.returnUrl = this.returnUrl;
            mandateConnectorResponse.links = this.links;
            mandateConnectorResponse.providerId = this.providerId;
            mandateConnectorResponse.mandateReference = this.mandateReference;
            mandateConnectorResponse.paymentProvider = this.paymentProvider;
            mandateConnectorResponse.description = this.description;
            mandateConnectorResponse.state = this.state;
            mandateConnectorResponse.mandateId = this.mandateId;
            mandateConnectorResponse.serviceReference = this.serviceReference;
            mandateConnectorResponse.payer = this.payer;
            mandateConnectorResponse.createdDate = this.createdDate;
            return mandateConnectorResponse;
        }
    }
}
