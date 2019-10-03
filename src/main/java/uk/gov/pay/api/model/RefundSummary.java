package uk.gov.pay.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value="RefundSummary", description = "Information about a refund.")
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

    @ApiModelProperty(value = "The status of the refund.", example = "available")
    public String getStatus() {
        return status;
    }

    @ApiModelProperty(value = "How much you can refund in pence.", example = "100")
    public long getAmountAvailable() {
        return amountAvailable;
    }

    @ApiModelProperty(value = "How much you've already refunded.")
    public long getAmountSubmitted() {
        return amountSubmitted;
    }
}
