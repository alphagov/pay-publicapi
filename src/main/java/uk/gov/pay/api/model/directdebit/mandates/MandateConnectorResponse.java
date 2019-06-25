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
    private List<PaymentConnectorResponseLink> links = new ArrayList<>();
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
}
