package uk.gov.pay.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;

@JsonInclude(value = JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(name = "PaymentSettlementSummary", description = "A structure representing information about a settlement")
public class PaymentSettlementSummary {

    @JsonProperty("capture_submit_time")
    private String captureSubmitTime;

    @JsonProperty("captured_date")
    private String capturedDate;

    @JsonProperty("settled_date")
    private String settledDate;

    public PaymentSettlementSummary() {}

    public PaymentSettlementSummary(String captureSubmitTime, String capturedDate, String settledDate) {
        this.captureSubmitTime = captureSubmitTime;
        this.capturedDate = capturedDate;
        this.settledDate = settledDate;
    }

    @Schema(description = "Date and time capture request has been submitted. May be null if capture request was not immediately acknowledged by payment gateway.",
            example = "2016-01-21T17:15:000Z", accessMode = READ_ONLY)
    public String getCaptureSubmitTime() {
        return captureSubmitTime;
    }

    @Schema(description = "Date of the capture event.", example = "2016-01-21", accessMode = READ_ONLY)
    public String getCapturedDate() {
        return capturedDate;
    }

    @Schema(description = "The date that the transaction was paid into the service's account.", example = "2016-01-21",
            accessMode = READ_ONLY)
    public String getSettledDate() {
        return settledDate;
    }
}
