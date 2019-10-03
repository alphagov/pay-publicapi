package uk.gov.pay.api.model.search.card;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import uk.gov.pay.api.model.links.RefundLinksForSearch;

@ApiModel(value = "Refund")
@Schema(name = "Refund")
public class RefundResult {

    @ApiModelProperty
    @JsonUnwrapped
    private RefundForSearchRefundsResult refunds;
    @ApiModelProperty(name = "_links")
    @Schema(name = "_links")
    private RefundLinksForSearch links;

    public RefundForSearchRefundsResult getRefunds() {
        return refunds;
    }

    public RefundLinksForSearch getLinks() {
        return links;
    }
}
