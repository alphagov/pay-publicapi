package uk.gov.pay.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import uk.gov.pay.api.model.links.PaymentEventLink;

@ApiModel(value="PaymentEvent", description = "A List of Payment Events information")
public class PaymentEvent {
    @JsonProperty("payment_id")
    private final String paymentId;

    @JsonProperty("state")
    private final PaymentState state;

    @JsonProperty("updated")
    private final String updated;

    @JsonProperty("_links")
    private PaymentEventLink paymentLink;

    public static PaymentEvent createPaymentEvent(JsonNode payload, String paymentLink, String paymentId) {
        PaymentState state = PaymentState.createPaymentState(payload.get("state"));
        return new PaymentEvent(paymentId, state, payload.get("updated").asText(), paymentLink);
    }

    private PaymentEvent(String paymentId, PaymentState state, String updated, String paymentLink) {
        this.paymentId = paymentId;
        this.state = state;
        this.updated = updated;
        this.paymentLink = new PaymentEventLink(paymentLink);
    }

    @ApiModelProperty(example = "hu20sqlact5260q2nanm0q8u93")
    public String getPaymentId() {
        return paymentId;
    }

    @ApiModelProperty(value = "state", dataType = "uk.gov.pay.api.model.PaymentState")
    public PaymentState getState() {
        return state;
    }

    @ApiModelProperty(value = "updated",example = "updated_date")
    public String getUpdated() {
        return updated;
    }

    @ApiModelProperty(dataType = "uk.gov.pay.api.model.links.PaymentEventLink")
    public PaymentEventLink getPaymentLink() {
        return paymentLink;
    }

    @Override
    public String toString() {
        return "PaymentEvent{" +
                "paymentId='" + paymentId + '\'' +
                ", state='" + state + '\'' +
                ", updated=" + updated +
                ", paymentLink=" + paymentLink +
                '}';
    }
}
