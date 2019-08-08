package uk.gov.pay.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import uk.gov.pay.api.model.links.PaymentEventLink;

@ApiModel(value="PaymentEvent", description = "A List of Payment Events information")
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentEventResponse {
    @JsonProperty("payment_id")
    private String paymentId;

    @JsonProperty("state")
    private PaymentState state;

    @JsonProperty("updated")
    private String updated;

    @JsonProperty("_links")
    private PaymentEventLink paymentLink;

    public static PaymentEventResponse from(PaymentEvent paymentEvent, String paymentId, String paymentLink) {
        return new PaymentEventResponse(paymentId, paymentEvent.getState(), paymentEvent.getUpdated(), paymentLink);
    }

    public static PaymentEventResponse from(TransactionEvent event, String paymentId, String paymentLink) {
        return new PaymentEventResponse(paymentId, event.getState(), event.getTimestamp(), paymentLink);
    }

    private PaymentEventResponse(String paymentId, PaymentState state, String updated, String paymentLink) {
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

    @ApiModelProperty(value = "updated",example = "2017-01-10T16:44:48.646Z")
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
