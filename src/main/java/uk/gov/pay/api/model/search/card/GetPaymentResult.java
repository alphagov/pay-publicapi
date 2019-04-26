package uk.gov.pay.api.model.search.card;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import uk.gov.pay.api.model.CardDetails;
import uk.gov.pay.api.model.CardPayment;
import uk.gov.pay.api.model.PaymentState;
import uk.gov.pay.api.model.RefundSummary;
import uk.gov.pay.api.model.SettlementSummary;
import uk.gov.pay.api.model.links.PaymentLinks;
import uk.gov.pay.commons.model.SupportedLanguage;

/**
 * Defines swagger specs for Get payment
 */
@ApiModel
public class GetPaymentResult extends CardPayment {

    @JsonProperty(LINKS_JSON_ATTRIBUTE)
    @ApiModelProperty(name = LINKS_JSON_ATTRIBUTE, dataType = "uk.gov.pay.api.model.links.PaymentLinks")
    private PaymentLinks links;

    public GetPaymentResult(String chargeId, long amount, PaymentState state, String returnUrl, String description, 
                            String reference, String email, String paymentProvider, String createdDate, 
                            RefundSummary refundSummary, SettlementSummary settlementSummary, CardDetails cardDetails, 
                            SupportedLanguage language, boolean delayedCapture, Long corporateCardSurcharge, 
                            Long totalAmount, String providerId) {
        super(chargeId, amount, state, returnUrl, description, reference, email, paymentProvider, createdDate, 
                refundSummary, settlementSummary, cardDetails, language, delayedCapture, corporateCardSurcharge, 
                totalAmount, providerId, null);
    }
}
