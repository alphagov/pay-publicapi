package uk.gov.pay.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentResult {

    @JsonProperty("charge_id")
    private String chargeId;
    private Long amount;
    private PaymentState state;
    @JsonProperty("return_url")
    private String returnUrl;
    private String description;
    private String reference;
    @JsonProperty("payment_provider")
    private String paymentProvider;
    private String created_date;
    @JsonProperty("links")
    private List<PaymentConnectorResponseLink> links = new ArrayList<>();

    public String getChargeId() {
        return chargeId;
    }

    public Long getAmount() {
        return amount;
    }

    public PaymentState getState() {
        return state;
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public String getDescription() {
        return description;
    }

    public String getReference() {
        return reference;
    }

    public String getPaymentProvider() {
        return paymentProvider;
    }

    public String getCreated_date() {
        return created_date;
    }

    public List<PaymentConnectorResponseLink> getLinks() {
        return links;
    }
}
