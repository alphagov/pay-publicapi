package uk.gov.pay.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Optional;

@ApiModel(value = "PaymentRefundRequest", description = "The structure of your request to the API when you create a refund.")
public class CreatePaymentRefundRequest {

    public static final String REFUND_AMOUNT_AVAILABLE = "refund_amount_available";
    public static final int REFUND_MIN_VALUE = 1;

    private int amount;
    @JsonProperty("refund_amount_available")
    private Integer refundAmountAvailable;

    public CreatePaymentRefundRequest() {
    }

    public CreatePaymentRefundRequest(int amount, Integer refundAmountAvailable) {
        this.amount = amount;
        this.refundAmountAvailable = refundAmountAvailable;
    }

    @ApiModelProperty(value = "How much to refund in pence.", required = true, allowableValues = "range[1, 10000000]", example = "150000")
    public int getAmount() {
        return amount;
    }

    /**
     * This field should be made compulsory at a later stage
     *
     * @return
     */
    @ApiModelProperty(value = "The `amount_available` you received when you got information about the payment.", required = false, allowableValues = "range[1, 10000000]", example = "200000")
    public Optional<Integer> getRefundAmountAvailable() {
        return Optional.ofNullable(refundAmountAvailable);
    }

    @Override
    public String toString() {
        return "CreatePaymentRefundRequest{" +
                "amount=" + amount +
                ", refundAmountAvailable=" + refundAmountAvailable +
                '}';
    }
}
