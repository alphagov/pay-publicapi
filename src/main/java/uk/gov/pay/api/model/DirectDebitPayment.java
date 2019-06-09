package uk.gov.pay.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import uk.gov.pay.api.model.response.Payment;
import uk.gov.pay.api.model.response.PaymentState;

import static uk.gov.pay.api.model.TokenPaymentType.DIRECT_DEBIT;


@JsonInclude(value = JsonInclude.Include.NON_NULL)
@ApiModel(value = "DirectDebitPayment")
public class DirectDebitPayment extends Payment {

    public DirectDebitPayment(String chargeId, long amount, PaymentState state, String returnUrl, String description,
                              String reference, String email, String paymentProvider, String createdDate) {
        super(chargeId, amount, state, returnUrl, description, reference, email, paymentProvider, createdDate);
        this.paymentType = DIRECT_DEBIT.getFriendlyName();
    }

    @Override
    public String toString() {
        // Some services put PII in the description, so don’t include it in the stringification
        return "Direct Debit Payment{" +
                "paymentId='" + super.paymentId + '\'' +
                ", paymentProvider='" + paymentProvider + '\'' +
                ", amount=" + amount +
                ", state='" + state + '\'' +
                ", returnUrl='" + returnUrl + '\'' +
                ", reference='" + reference + '\'' +
                ", createdDate='" + createdDate + '\'' +
                '}';
    }
}
