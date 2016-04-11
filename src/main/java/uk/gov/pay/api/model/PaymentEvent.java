package uk.gov.pay.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value="Payment Event information", description = "A List of Payment Events information")
public class PaymentEvent {
    @JsonProperty("payment_id")
    private final String paymentId;

    @JsonProperty("status")
    private final String status;

    @JsonProperty("updated")
    private final String updated;

    @JsonProperty("_links")
    private PaymentEventLink paymentLink;

    public static PaymentEvent createPaymentEvent(JsonNode payload, String paymentLink) {
        return new PaymentEvent(
            payload.get("charge_id").asText(),
            payload.get("status").asText(),
            payload.get("updated").asText(),
            paymentLink
        );
    }

    private PaymentEvent(String chargeId, String status, String updated, String paymentLink) {
        this.paymentId = chargeId;
        this.status = status;
        this.updated = updated;
        this.paymentLink = new PaymentEventLink(paymentLink);
    }

    @ApiModelProperty(example = "hu20sqlact5260q2nanm0q8u93")
    public String getPaymentId() {
        return paymentId;
    }

    @ApiModelProperty(value = "status",example = "SUCCEEDED")
    public String getStatus() {
        return status;
    }

    @ApiModelProperty(value = "updated",example = "updated_date")
    public String getUpdated() {
        return updated;
    }

    @ApiModelProperty(dataType = "uk.gov.pay.api.model.PaymentEventLink")
    public PaymentEventLink getPaymentLink() {
        return paymentLink;
    }

    @Override
    public String toString() {
        return "PaymentEvent{" +
                "paymentId='" + paymentId + '\'' +
                ", status='" + status + '\'' +
                ", updated=" + updated +
                ", paymentLink=" + paymentLink +
                '}';
    }
}
