package uk.gov.pay.api.model.card;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import uk.gov.pay.api.model.links.RefundLinksForSearch;

@ApiModel(value = "Refund")
public class RefundResult {

    @ApiModelProperty
    @JsonUnwrapped
    private RefundForSearchRefundsResult refunds;
    @ApiModelProperty(name = "_links")
    private RefundLinksForSearch links;

    public RefundForSearchRefundsResult getRefunds() {
        return refunds;
    }

    public RefundLinksForSearch getLinks() {
        return links;
    }
}
