package uk.gov.pay.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@JsonInclude(value = JsonInclude.Include.NON_NULL)
@ApiModel(value="SettlementSummary", description = "Information about when your PSP took a payment.")
public class SettlementSummary {

    @JsonProperty("capture_submit_time")
    private String captureSubmitTime;

    @JsonProperty("captured_date")
    private String capturedDate;

    public SettlementSummary() {}

    public SettlementSummary(String captureSubmitTime, String capturedDate) {
        this.captureSubmitTime = captureSubmitTime;
        this.capturedDate = capturedDate;
    }

    @ApiModelProperty(value = "When we sent a request to your PSP to take the payment from your user's bank account.", example = "2016-01-21T17:15:000Z")
    public String getCaptureSubmitTime() {
        return captureSubmitTime;
    }

    @ApiModelProperty(value = "When the PSP took the payment from your user's bank account.", example = "2016-01-21")
    public String getCapturedDate() {
        return capturedDate;
    }
}
