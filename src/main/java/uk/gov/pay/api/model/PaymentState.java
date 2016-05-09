package uk.gov.pay.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.annotations.ApiModelProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
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

    @ApiModelProperty(example = "created")
    public String getStatus() {
        return status;
    }

    @ApiModelProperty(example = "boolean")
    public boolean isFinished() {
        return finished;
    }

    @ApiModelProperty(example = "Payment was cancelled by the user", required = false)
    public String getMessage() {
        return message;
    }

    @ApiModelProperty(example = "P0030")
    public String getCode() {
        return code;
    }
}
