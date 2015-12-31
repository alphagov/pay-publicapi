package uk.gov.pay.api.model;

import com.fasterxml.jackson.databind.JsonNode;
import io.dropwizard.jackson.JsonSnakeCase;
import io.swagger.annotations.ApiModel;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@ApiModel(value="Payment Events information", description = "A List of Payment Events information")
@JsonSnakeCase
public class PaymentEvent {
    private final String paymentId;
    private final String status;
    private final LocalDateTime updated;

    public static PaymentEvent createPaymentEvent(JsonNode payload) {
        LocalDateTime updated = null;
        if(payload.get("updated") != null) {
            String updatedStr =
                    payload.get("updated").get("year") + "-" +
                    format(payload.get("updated").get("monthValue").asInt()) + "-" +
                    format(payload.get("updated").get("dayOfMonth").asInt()) + " " +
                    format(payload.get("updated").get("hour").asInt()) + ":" +
                    format(payload.get("updated").get("minute").asInt()) + ":" +
                    format(payload.get("updated").get("second").asInt());
            System.out.println("TEST: UpdatedStr: "  + updatedStr);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            updated = LocalDateTime.parse(updatedStr, formatter);
        }
        return new PaymentEvent(
                payload.get("chargeId").asText(),
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

    private static String format(Integer value) {
        System.out.println("TEST-format: value: "  + value);
        if (value < 10) {
            return "0" + value;
        } else {
            return value.toString();
        }
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
