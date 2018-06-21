package uk.gov.pay.api.model.directdebit;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel(value="DDPaymentState", description = "A structure representing the current state of the direct debit transaction in its lifecycle.")
public class DDPaymentState {
    @JsonProperty("status")
    private String status;

    @JsonProperty("finished")
    private boolean finished;

    public static DDPaymentState createPaymentState(JsonNode node) {
        return new DDPaymentState(
            node.get("status").asText(),
            node.get("finished").asBoolean()
        );
    }

    public DDPaymentState() {
    }
    
    public DDPaymentState(String status, boolean finished) {
        this.status = status;
        this.finished = finished;
    }

    @ApiModelProperty(value = "Current progress of the payment in its lifecycle", required=true, example = "created")
    public String getStatus() {
        return status;
    }

    @ApiModelProperty(value = "Whether the payment has finished", required = true)
    public boolean isFinished() {
        return finished;
    }
    
    @Override
    public String toString() {
        return "PaymentState{" +
                "status='" + status + '\'' +
                ", finished='" + finished + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DDPaymentState that = (DDPaymentState) o;
        return finished == that.finished &&
                Objects.equals(status, that.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, finished);
    }
}
