package uk.gov.pay.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import io.dropwizard.jackson.JsonSnakeCase;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value="Payment information", description = "A Payment description")
@JsonSnakeCase
public class Payment {
    @JsonProperty("payment_id")
    private final String paymentId;
    @JsonProperty("payment_provider")
    private final String paymentProvider;
    private final long amount;
    private final String status;
    private final String description;
    private final String returnUrl;
    private final String reference;
    @JsonProperty("_links")
    private final Links links = new Links();

    @JsonProperty("created_date")
    private final String createdDate;

    public static Payment createPaymentResponse(JsonNode payload) {
        return new Payment(
                payload.get("charge_id").asText(),
                payload.get("amount").asLong(),
                payload.get("status").asText(),
                payload.get("return_url").asText(),
                payload.get("description").asText(),
                payload.get("reference").asText(),
                payload.get("payment_provider").asText(),
                payload.get("created_date").asText()
        );
    }

    private Payment(String chargeId, long amount, String status, String returnUrl, String description,
                    String reference, String paymentProvider, String createdDate) {
        this.paymentId = chargeId;
        this.amount = amount;
        this.status = status;
        this.returnUrl = returnUrl;
        this.description = description;
        this.reference = reference;
        this.paymentProvider = paymentProvider;
        this.createdDate = createdDate;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    @ApiModelProperty(example = "1122335")
    public String getPaymentId() {
        return paymentId;
    }

    @ApiModelProperty(example = "1200")
    public long getAmount() {
        return amount;
    }

    @ApiModelProperty(example = "CREATED")
    public String getStatus() {
        return status;
    }

    @ApiModelProperty(example = "http://government.service.domain/transaction/1")
    public String getReturnUrl() {
        return returnUrl;
    }

    @ApiModelProperty(example = "New Passport Application")
    public String getDescription() {
        return description;
    }

    @ApiModelProperty(example = "12345")
    public String getReference() {
        return reference;
    }

    public String getPaymentProvider() {
        return paymentProvider;
    }

    @Override
    public String toString() {
        return "Payment{" +
                "paymentId='" + paymentId + '\'' +
                ", paymentProvider='" + paymentProvider + '\'' +
                ", amount=" + amount +
                ", status='" + status + '\'' +
                ", returnUrl='" + returnUrl + '\'' +
                ", description='" + description + '\'' +
                ", reference='" + reference + '\'' +
                ", createdDate='" + createdDate + '\'' +
                ", links=" + links +
                '}';
    }

    public Payment withSelfLink(String url) {
        this.links.setSelf(url);
        return this;
    }

    public Payment withNextLink(String url) {
        this.links.setNext(url);
        return this;
    }
}
