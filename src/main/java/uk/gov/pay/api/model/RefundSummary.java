package uk.gov.pay.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;

@ApiModel(value = "RefundSummary", description = "A structure representing the refunds availability")
@Schema(name = "RefundSummary", description = "A structure representing the refunds availability")
@JsonIgnoreProperties(ignoreUnknown = true)
public class RefundSummary {

    private String status;

    @JsonProperty("amount_available")
    private long amountAvailable;

    @JsonProperty("amount_submitted")
    private long amountSubmitted;

    public RefundSummary() {}

    public RefundSummary(String status, long amountAvailable, long amountSubmitted) {
        this.status = status;
        this.amountAvailable = amountAvailable;
        this.amountSubmitted = amountSubmitted;
    }

    @ApiModelProperty(value = "Availability status of the refund", example = "available")
    @Schema(description = "Availability status of the refund", example = "available")
    public String getStatus() {
        return status;
    }

    @ApiModelProperty(value = "Amount available for refund in pence", example = "100")
    @Schema(description = "Amount available for refund in pence", example = "100", accessMode = READ_ONLY)
    public long getAmountAvailable() {
        return amountAvailable;
    }

    @ApiModelProperty(value = "Amount submitted for refunds on this Payment in pence")
    @Schema(description = "Amount submitted for refunds on this Payment in pence", accessMode = READ_ONLY)
    public long getAmountSubmitted() {
        return amountSubmitted;
    }
}
