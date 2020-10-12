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
@ApiModel(value="RefundSettlementSummary", description = "A structure representing information about a settlement for refunds")
@Schema(name = "RefundSettlementSummary", description = "A structure representing information about a settlement for refunds")
public class RefundSettlementSummary {

    @JsonProperty("settled_date")
    private String settledDate;

    public RefundSettlementSummary() {}

    public RefundSettlementSummary(String settledDate) {
        this.settledDate = settledDate;
    }

    @ApiModelProperty(value = "The date that the transaction was refunded from the service's account.", example = "2016-01-21")
    @Schema(description = "The date that the transaction was refunded from the service's account.", example = "2016-01-21",
            accessMode = READ_ONLY)
    public String getSettledDate() {
        return settledDate;
    }
}
