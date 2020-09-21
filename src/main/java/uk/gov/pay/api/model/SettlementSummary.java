package uk.gov.pay.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;

@JsonInclude(value = JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel(value="SettlementSummary", description = "A structure representing information about a settlement")
@Schema(name = "SettlementSummary", description = "A structure representing information about a settlement")
public class SettlementSummary {

    @JsonProperty("capture_submit_time")
    private String captureSubmitTime;

    @JsonProperty("captured_date")
    private String capturedDate;

    private String settledDate;

    public SettlementSummary() {}

    public SettlementSummary(String captureSubmitTime, String capturedDate, String settledDate) {
        this.captureSubmitTime = captureSubmitTime;
        this.capturedDate = capturedDate;
        this.settledDate = settledDate;
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

    @JsonProperty("settled_date")
    @ApiModelProperty(value = "The date that the transaction was settled, for example paid into or refunded from the service's account", example = "2016-01-21", hidden = true)
    @Schema(description = "The date that the transaction was settled, for example paid into or refunded from the service's account", example = "2016-01-21",
            accessMode = READ_ONLY, hidden = true)
    public String getSettledDate() {
        return settledDate;
    }
}
