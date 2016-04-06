package uk.gov.pay.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static uk.gov.pay.api.model.PaymentEvent.createPaymentEvent;

@ApiModel(value="Payment Events information", description = "A List of Payment Events information")
public class PaymentEvents {
    @JsonProperty("payment_id")
    private final String paymentId;

    private final List<PaymentEvent> events;

    @JsonProperty("_links")
    private SelfLinks links = new SelfLinks();

    public static PaymentEvents createPaymentEventsResponse(JsonNode payload) {
        List<PaymentEvent> events = newArrayList();
        if(payload.get("events").isArray()) {
            for (JsonNode event : payload.get("events")) {
                events.add(createPaymentEvent(event));
            }
        }
        return new PaymentEvents(
                payload.get("charge_id").asText(),
                events
        );
    }

    private PaymentEvents(String chargeId, List<PaymentEvent> events) {
        this.paymentId = chargeId;
        this.events = events;
    }

    @ApiModelProperty(example = "hu20sqlact5260q2nanm0q8u93")
    public String getPaymentId() {
        return paymentId;
    }

    public List<PaymentEvent> getEvents() {
        return events;
    }

    @Override
    public String toString() {
        return "PaymentEvents{" +
                "paymentId='" + paymentId + '\'' +
                ", events=" + events +
                ", links=" + links +
                '}';
    }

    public PaymentEvents withSelfLink(String url) {
        links.addSelf(url);
        return this;
    }
}
