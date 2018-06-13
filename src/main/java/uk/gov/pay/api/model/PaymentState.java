package uk.gov.pay.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel(value="PaymentState", description = "A structure representing the current state of the payment in its lifecycle.")
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

    public PaymentState(String status, boolean finished, String message, String code) {
        this.status = status;
        this.finished = finished;
        this.message = message;
        this.code = code;
    }

    @ApiModelProperty(value = "Current progress of the payment in its lifecycle", required=true, example = "created")
    public String getStatus() {
        return status;
    }

    @ApiModelProperty(value = "Whether the payment has finished", required = true)
    public boolean isFinished() {
        return finished;
    }

    @ApiModelProperty(value = "What went wrong with the Payment if it finished with an error - English message", required = true, example = "User cancelled the payment")
    public String getMessage() {
        return message;
    }

    @ApiModelProperty(value = "What went wrong with the Payment if it finished with an error - error code", required = true, example = "P010")
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
}
