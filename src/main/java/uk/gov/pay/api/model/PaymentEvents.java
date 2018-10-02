package uk.gov.pay.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import uk.gov.pay.api.model.links.PaymentLinksForEvents;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static uk.gov.pay.api.model.PaymentEvent.createPaymentEvent;

public class PaymentEvents {
    public static final String EVENTS = "events";
    @JsonProperty("payment_id")
    private final String paymentId;

    private final List<PaymentEvent> events;

    @JsonProperty("_links")
    private PaymentLinksForEvents links = new PaymentLinksForEvents();

    public static PaymentEvents createPaymentEventsResponse(JsonNode payload, String paymentLink) {
        List<PaymentEvent> events = newArrayList();
        String paymentId = payload.get("charge_id").asText();

        if(payload.get(EVENTS).isArray()) {
            for (JsonNode event : payload.get(EVENTS)) {
                events.add(createPaymentEvent(event, paymentLink, paymentId));
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
