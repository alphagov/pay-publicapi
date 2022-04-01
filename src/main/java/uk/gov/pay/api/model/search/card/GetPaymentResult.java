package uk.gov.pay.api.model.search.card;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.pay.api.model.AuthorisationSummary;
import uk.gov.pay.api.model.CardDetails;
import uk.gov.pay.api.model.CardPayment;
import uk.gov.pay.api.model.PaymentSettlementSummary;
import uk.gov.pay.api.model.PaymentState;
import uk.gov.pay.api.model.RefundSummary;
import uk.gov.pay.api.model.links.PaymentLinks;
import uk.gov.service.payments.commons.model.SupportedLanguage;

/**
 * Defines swagger specs for Get payment
 */
public class GetPaymentResult extends CardPayment {

    @JsonProperty(LINKS_JSON_ATTRIBUTE)
    private PaymentLinks links;

    public GetPaymentResult(String chargeId, long amount, PaymentState state, String returnUrl, String description,
                            String reference, String email, String paymentProvider, String createdDate,
                            RefundSummary refundSummary, PaymentSettlementSummary settlementSummary, CardDetails cardDetails,
                            SupportedLanguage language, boolean delayedCapture, boolean moto, Long corporateCardSurcharge,
                            Long totalAmount, String providerId, Long fee, Long netAmount, AuthorisationSummary authorisationSummary) {
        super(chargeId, amount, state, returnUrl, description, reference, email, paymentProvider, createdDate,
                refundSummary, settlementSummary, cardDetails, language, delayedCapture, moto, corporateCardSurcharge,
                totalAmount, providerId, null, fee, netAmount, authorisationSummary, null);
    }
}
