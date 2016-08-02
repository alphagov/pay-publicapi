package uk.gov.pay.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "CreatePaymentRefundRequest", description = "The Payment Refund Request Payload")
public class CreatePaymentRefundRequest {

    private int amount;

    public CreatePaymentRefundRequest() {
    }

    public CreatePaymentRefundRequest(int amount) {
        this.amount = amount;
    }

    @ApiModelProperty(value = "Amount in pence. Can't be more than the available amount for refunds", required = true, allowableValues = "range[1, 10000000]", example = "150000")
    public int getAmount() {
        return amount;
    }


    @Override
    public String toString() {
        return "CreatePaymentRefundRequest{" +
                "amount=" + amount +
                '}';
    }
}
