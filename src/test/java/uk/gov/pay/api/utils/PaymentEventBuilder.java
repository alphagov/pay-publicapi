package uk.gov.pay.api.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import uk.gov.pay.api.model.PaymentEvent;

import java.time.LocalDateTime;

/**
 * Created by pmonteiro on 04/01/16.
 */
public class PaymentEventBuilder {

    @JsonDeserialize
    @JsonSerialize
    private String paymentId;

    @JsonDeserialize
    @JsonSerialize
    private String status;

    @JsonDeserialize(using = JsonDateDeserializer.class)
    @JsonSerialize(using = JsonDateSerializer.class)
    private LocalDateTime updated;

    public PaymentEventBuilder(String paymentId, String status, LocalDateTime updated) {
        this.paymentId = paymentId;
        this.status = status;
        this.updated = updated;
    }

    public PaymentEvent build() {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.convertValue(this, JsonNode.class);
        return PaymentEvent.createPaymentEvent(node);
    }

}
