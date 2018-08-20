package uk.gov.pay.api.model.search.card;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import uk.gov.pay.api.model.CardDetails;
import uk.gov.pay.api.model.CardPayment;
import uk.gov.pay.api.model.ChargeFromResponse;
import uk.gov.pay.api.model.PaymentState;
import uk.gov.pay.api.model.RefundSummary;
import uk.gov.pay.api.model.SettlementSummary;
import uk.gov.pay.api.model.links.PaymentLinksForSearch;
import uk.gov.pay.commons.model.SupportedLanguage;

import java.net.URI;

public class PaymentForSearchResult extends CardPayment {

    @JsonProperty(LINKS_JSON_ATTRIBUTE)
    private PaymentLinksForSearch links = new PaymentLinksForSearch();

    public PaymentForSearchResult(String chargeId, long amount, PaymentState state, String returnUrl, String description,
                                  String reference, String email, String paymentProvider, String createdDate, SupportedLanguage language,
                                  RefundSummary refundSummary, SettlementSummary settlementSummary, CardDetails cardDetails,
                                  URI selfLink, URI paymentEventsLink, URI paymentCancelLink, URI paymentRefundsLink) {
        super(chargeId, amount, state, returnUrl, description, reference, email, paymentProvider,
                createdDate, refundSummary, settlementSummary, cardDetails, language);
        this.links.addSelf(selfLink.toString());
        this.links.addEvents(paymentEventsLink.toString());
        this.links.addRefunds(paymentRefundsLink.toString());

        if (!state.isFinished()) {
            this.links.addCancel(paymentCancelLink.toString());
        }
    }

    public static PaymentForSearchResult valueOf(
            ChargeFromResponse paymentResult,
            URI selfLink,
            URI paymentEventsLink,
            URI paymentCancelLink,
            URI paymentRefundsLink) {

        return new PaymentForSearchResult(
                paymentResult.getChargeId(),
                paymentResult.getAmount(),
                paymentResult.getState(),
                paymentResult.getReturnUrl(),
                paymentResult.getDescription(),
                paymentResult.getReference(),
                paymentResult.getEmail(),
                paymentResult.getPaymentProvider(),
                paymentResult.getCreatedDate(),
                paymentResult.getLanguage(),
                paymentResult.getRefundSummary(),
                paymentResult.getSettlementSummary(),
                paymentResult.getCardDetails(),
                selfLink,
                paymentEventsLink,
                paymentCancelLink,
                paymentRefundsLink);
    }

    @ApiModelProperty(dataType = "uk.gov.pay.api.model.links.PaymentLinksForSearch")
    public PaymentLinksForSearch getLinks() {
        return links;
    }
}
