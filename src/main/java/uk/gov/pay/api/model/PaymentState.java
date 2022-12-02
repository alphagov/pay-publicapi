package uk.gov.pay.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Objects;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(name = "PaymentState", description = "A structure representing the current state of the payment in its lifecycle.")
public class PaymentState {
    @JsonProperty("status")
    private String status;

    @JsonProperty("finished")
    private boolean finished;

    @JsonProperty("message")
    private String message;

    @JsonProperty("code")
    private String code;
    

    public static PaymentState createPaymentState(JsonNode node) {
        return new PaymentState(
                node.get("status").asText(),
                node.get("finished").asBoolean(),
                node.has("message") ? node.get("message").asText() : null,
                node.has("code") ? node.get("code").asText() : null
        );
    }

    public PaymentState() {
    }

    public PaymentState(String status, boolean finished) {
        this(status, finished, null, null);
    }

    public PaymentState(String status, boolean finished, String message, String code) {
        this.status = status;
        this.finished = finished;
        this.message = message;
        this.code = code;
    }

    @Schema(description = "Where the payment is in [the payment status lifecycle]" +
            "(https://docs.payments.service.gov.uk/api_reference/#payment-status-meanings).", 
            example = "created", accessMode = READ_ONLY)
    public String getStatus() {
        return status;
    }

    @Schema(description = "Indicates whether a payment journey is finished.", accessMode = READ_ONLY)
    public boolean isFinished() {
        return finished;
    }

    @Schema(description = "A description of what went wrong with this payment. `message` only appears if the payment failed.",
            example = "User cancelled the payment", accessMode = READ_ONLY)
    public String getMessage() {
        return message;
    }

    @Schema(description = "An [API error code](https://docs.payments.service.gov.uk/api_reference/#gov-uk-pay-api-error-codes)" +
            "that explains why the payment failed. `code` only appears if the payment failed.", example = "P010",
            accessMode = READ_ONLY)
    public String getCode() {
        return code;
    }
    
    @Override
    public String toString() {
        return "PaymentState{" +
                "status='" + status + '\'' +
                ", finished='" + finished + '\'' +
                ", message=" + message +
                ", code=" + code +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PaymentState that = (PaymentState) o;
        return finished == that.finished &&
                Objects.equals(status, that.status) &&
                Objects.equals(message, that.message) &&
                Objects.equals(code, that.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, finished, message, code);
    }
}
