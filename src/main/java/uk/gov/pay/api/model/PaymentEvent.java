package uk.gov.pay.api.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.joda.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@ApiModel(value="Payment Event information", description = "A List of Payment Events information")
public class PaymentEvent {
    @JsonProperty("payment_id")
    private final String paymentId;

    @JsonProperty("status")
    private final String status;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime updated;

    public static PaymentEvent createPaymentEvent(JsonNode payload) {
        LocalDateTime updated = null;
        if(payload.get("updated") != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            updated = LocalDateTime.parse(payload.get("updated").asText(), formatter);
        }
        return new PaymentEvent(
                payload.get("charge_id").asText(),
                payload.get("status").asText(),
                updated
        );
    }

    private PaymentEvent(String chargeId, String status, LocalDateTime updated) {
        this.paymentId = chargeId;
        this.status = status;
        this.updated = updated;
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
