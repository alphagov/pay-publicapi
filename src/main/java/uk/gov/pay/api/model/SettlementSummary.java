package uk.gov.pay.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;

@JsonInclude(value = JsonInclude.Include.NON_NULL)
@ApiModel(value="SettlementSummary", description = "A structure representing information about a settlement")
@Schema(name = "SettlementSummary", description = "A structure representing information about a settlement")
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

    @ApiModelProperty(value = "Date and time capture request has been submitted (may be null if capture request was not immediately acknowledged by payment gateway)", example = "2016-01-21T17:15:000Z")
    @Schema(description = "Date and time capture request has been submitted (may be null if capture request was not immediately acknowledged by payment gateway)", 
            example = "2016-01-21T17:15:000Z", accessMode = READ_ONLY)
    public String getCaptureSubmitTime() {
        return captureSubmitTime;
    }

    @ApiModelProperty(value = "Date of the capture event", example = "2016-01-21")
    @Schema(description = "Date of the capture event", example = "2016-01-21", accessMode = READ_ONLY)
    public String getCapturedDate() {
        return capturedDate;
    }
}
