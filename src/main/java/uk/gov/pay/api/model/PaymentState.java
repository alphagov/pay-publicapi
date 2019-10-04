package uk.gov.pay.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel(value = "PaymentState", description = "Information about the status of a payment.")
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

    @ApiModelProperty(value = "The current stage of your user's payment journey.", example = "created")
    public String getStatus() {
        return status;
    }

    @ApiModelProperty(value = "Whether your user's payment journey is complete.")
    public boolean isFinished() {
        return finished;
    }

    @ApiModelProperty(value = "What went wrong if your user did not successfully complete their payment.", example = "User cancelled the payment")
    public String getMessage() {
        return message;
    }

    @ApiModelProperty(value = "Which error code was generated if your user did not successfully complete their payment.", example = "P010")
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
