package uk.gov.pay.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChargeFromResponse {

    @JsonProperty("charge_id")
    private String chargeId;

    @JsonProperty("return_url")
    private String returnUrl;

    @JsonProperty("payment_provider")
    private String paymentProvider;

    @JsonProperty("card_brand")
    private String cardLabel;

    @JsonProperty("links")
    private List<PaymentConnectorResponseLink> links = new ArrayList<>();

    @JsonProperty(value = "refund_summary")
    private RefundSummary refundSummary;

    private Long amount;
    private PaymentState state;
    private String description;
    private String reference;
    private String email;
    private String created_date;

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

    public String getEmail() {
        return email;
    }

    public String getPaymentProvider() {
        return paymentProvider;
    }

    public String getCardLabel() { return cardLabel; }

    public String getCreated_date() {
        return created_date;
    }

    public List<PaymentConnectorResponseLink> getLinks() {
        return links;
    }

    public RefundSummary getRefundSummary() {
        return refundSummary;
    }
}
