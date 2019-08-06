package uk.gov.pay.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentEvent {
    @JsonProperty("charge_id")
    private String chargeId;

    @JsonProperty("state")
    private PaymentState state;

    @JsonProperty("updated")
    private String updated;

    public PaymentEvent() {}

    public String getChargeId() {
        return chargeId;
    }

    public PaymentState getState() {
        return state;
    }

    public String getUpdated() {
        return updated;
    }

    @Override
    public String toString() {
        return "PaymentEvent{" +
                "chargeId='" + chargeId + '\'' +
                ", state='" + state + '\'' +
                ", updated=" + updated +
                "}";
    }
}
