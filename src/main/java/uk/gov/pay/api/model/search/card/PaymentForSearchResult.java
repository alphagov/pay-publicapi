package uk.gov.pay.api.model.search.card;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import uk.gov.pay.api.model.ChargeFromResponse;
import uk.gov.pay.api.model.generated.CardDetails;
import uk.gov.pay.api.model.CardPayment;
import uk.gov.pay.api.model.generated.Link;
import uk.gov.pay.api.model.generated.PaymentLinksForSearch;
import uk.gov.pay.api.model.generated.PaymentState;
import uk.gov.pay.api.model.generated.PostLink;
import uk.gov.pay.api.model.generated.RefundSummary;
import uk.gov.pay.api.model.generated.SettlementSummary;
import uk.gov.pay.commons.model.SupportedLanguage;

import javax.ws.rs.HttpMethod;
import java.net.URI;

import static javax.ws.rs.HttpMethod.GET;
import static javax.ws.rs.HttpMethod.POST;

public class PaymentForSearchResult extends CardPayment {

    public static final String LINKS_JSON_ATTRIBUTE = "_links";

    @JsonProperty(LINKS_JSON_ATTRIBUTE)
    private PaymentLinksForSearch links = new PaymentLinksForSearch();

    public PaymentForSearchResult(String chargeId, long amount, PaymentState state, String returnUrl, String description,
                                  String reference, String email, String paymentProvider, String createdDate, SupportedLanguage language,
                                  boolean delayedCapture, RefundSummary refundSummary, SettlementSummary settlementSummary, CardDetails cardDetails,
                                  URI selfLink, URI paymentEventsLink, URI paymentCancelLink, URI paymentRefundsLink) {
        super(chargeId, amount, state, returnUrl, description, reference, email,
                paymentProvider, createdDate, refundSummary, settlementSummary, cardDetails, language, delayedCapture);
        
        this.links.self(new Link().href(selfLink.toString()).method(GET));
        this.links.events(new Link().href(paymentEventsLink.toString()).method(GET));
        this.links.refunds(new Link().href(paymentRefundsLink.toString()).method(GET));

        if (!state.isFinished()) {
            this.links.cancel(new PostLink().href(paymentCancelLink.toString()).method(POST));
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
                paymentResult.getDelayedCapture(),
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
