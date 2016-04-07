package uk.gov.pay.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "Payment Error response", description = "A Payment Error response")
public class PaymentErrorResponse {
    private String code;
    private String description;

    public PaymentErrorResponse(String code, String description) {
        this.code = code;
        this.description = description;
    }

    @ApiModelProperty(example = "P0198")
    public String getCode() {
        return code;
    }

    @ApiModelProperty(example = "Downstream system error")
    public String getDescription() {
        return description;
    }
}
