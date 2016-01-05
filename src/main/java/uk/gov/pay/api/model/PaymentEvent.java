package uk.gov.pay.api.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.dropwizard.jackson.JsonSnakeCase;
import io.swagger.annotations.ApiModel;
import uk.gov.pay.api.utils.JsonDateDeserializer;
import uk.gov.pay.api.utils.JsonDateSerializer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@ApiModel(value="Payment Events information", description = "A List of Payment Events information")
@JsonSnakeCase
public class PaymentEvent {

    private final String paymentId;
    private final String status;

    @JsonDeserialize(using = JsonDateDeserializer.class)
    @JsonSerialize(using = JsonDateSerializer.class)
    private final LocalDateTime updated;

    public static PaymentEvent createPaymentEvent(JsonNode payload) {
        LocalDateTime updated = null;
        if(payload.get("updated") != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            updated = LocalDateTime.parse(payload.get("updated").asText(), formatter);
        }
        return new PaymentEvent(
                payload.get("paymentId").asText(),
                payload.get("status").asText(),
                updated
        );
    }

    private PaymentEvent(String chargeId, String status, LocalDateTime updated) {
        this.paymentId = chargeId;
        this.status = status;
        this.updated = updated;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getUpdated() {
        return updated;
    }

    @Override
    public String toString() {
        return "PaymentEvent{" +
                "paymentId='" + paymentId + '\'' +
                ", status='" + status + '\'' +
                ", updated=" + updated +
                '}';
    }
}
