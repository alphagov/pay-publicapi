package uk.gov.pay.api.model;

import com.fasterxml.jackson.databind.JsonNode;
import uk.gov.pay.api.model.generated.Link;
import uk.gov.pay.api.model.generated.PaymentEvent;
import uk.gov.pay.api.model.generated.PaymentEventLink;
import uk.gov.pay.api.model.generated.PaymentEvents;
import uk.gov.pay.api.model.generated.PaymentState;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public class PaymentEventsCreator {

    public static final String EVENTS = "events";
    
    public static PaymentEvents createPaymentEventsResponse(JsonNode payload, String paymentLink) {
        List<PaymentEvent> events = newArrayList();
        String paymentId = payload.get("charge_id").asText();

        if(payload.get(EVENTS).isArray()) {
            for (JsonNode event : payload.get(EVENTS)) {
                JsonNode node = event.get("state");
                PaymentState state = new PaymentState()
                        .status(node.get("status").asText())
                        .finished(node.get("finished").asBoolean())
                        .message(node.has("message") ? node.get("message").asText() : null)
                        .code(node.has("code") ? node.get("code").asText() : null);
                PaymentEvent paymentEvent = new PaymentEvent()
                        .paymentId(paymentId)
                        .state(state)
                        .updated(event.get("updated").asText())
                        .links(new PaymentEventLink().paymentUrl(new Link().href(paymentLink)));
                events.add(paymentEvent);
            }
        }
        return new PaymentEvents().paymentId(payload.get("charge_id").asText()).events(events);
    }
}
