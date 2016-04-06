package uk.gov.pay.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value="Payment Error response", description = "A Payment Error response")
public class PaymentErrorResponse {
    private String code;
    private String message;

    public PaymentErrorResponse(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @ApiModelProperty(example = "P0198")
    public String getCode() {
        return code;
    }

    @ApiModelProperty(example = "Downstream system error")
    public String getMessage() {
        return message;
    }
}
