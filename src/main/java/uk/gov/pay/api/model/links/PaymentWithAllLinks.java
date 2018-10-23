package uk.gov.pay.api.model.links;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import io.swagger.annotations.ApiModelProperty;
import uk.gov.pay.api.model.CardDetails;
import uk.gov.pay.api.model.CardPayment;
import uk.gov.pay.api.model.ChargeFromResponse;
import uk.gov.pay.api.model.DirectDebitPayment;
import uk.gov.pay.api.model.Payment;
import uk.gov.pay.api.model.PaymentConnectorResponseLink;
import uk.gov.pay.api.model.PaymentState;
import uk.gov.pay.api.model.RefundSummary;
import uk.gov.pay.api.model.SettlementSummary;
import uk.gov.pay.commons.model.SupportedLanguage;
import uk.gov.pay.commons.model.TokenPaymentType;

import java.net.URI;
import java.util.List;

import static uk.gov.pay.api.model.Payment.LINKS_JSON_ATTRIBUTE;

public class PaymentWithAllLinks {

    @JsonUnwrapped
    private Payment payment;

    @JsonProperty(LINKS_JSON_ATTRIBUTE)
    private PaymentLinks links = new PaymentLinks();

    @ApiModelProperty(dataType = "uk.gov.pay.api.model.links.PaymentLinks")
    public PaymentLinks getLinks() {
        return links;
    }

    @ApiModelProperty(dataType = "uk.gov.pay.api.model.Payment")
    public Payment getPayment() {
        return payment;
    }

    public PaymentWithAllLinks(String chargeId, long amount, PaymentState state, String returnUrl, String description,
                               String reference, String email, String paymentProvider, String createdDate, SupportedLanguage language,
                               boolean delayedCapture, RefundSummary refundSummary, SettlementSummary settlementSummary, CardDetails cardDetails,
                               List<PaymentConnectorResponseLink> paymentConnectorResponseLinks, URI selfLink, URI paymentEventsUri, URI paymentCancelUri,
                               URI paymentRefundsUri, Long corporateCardSurcharge, Long totalAmount) {
        this.payment = new CardPayment(chargeId, amount, state, returnUrl, description, reference, email, paymentProvider, createdDate,
                refundSummary, settlementSummary, cardDetails, language, delayedCapture, corporateCardSurcharge, totalAmount);
        this.links.addSelf(selfLink.toString());
        this.links.addKnownLinksValueOf(paymentConnectorResponseLinks);
        this.links.addEvents(paymentEventsUri.toString());
        this.links.addRefunds(paymentRefundsUri.toString());

        if (!state.isFinished()) {
            this.links.addCancel(paymentCancelUri.toString());
        }
    }

    private PaymentWithAllLinks(String chargeId, long amount, PaymentState state, String returnUrl, String description,
                                String reference, String email, String paymentProvider, String createdDate, List<PaymentConnectorResponseLink> paymentConnectorResponseLinks,
                                URI selfLink) {
        this.payment = new DirectDebitPayment(chargeId, amount, state, returnUrl, description, reference, email, paymentProvider, createdDate);
        this.links.addSelf(selfLink.toString());
        this.links.addKnownLinksValueOf(paymentConnectorResponseLinks);
    }

    public static PaymentWithAllLinks valueOf(ChargeFromResponse paymentConnector,
                                              URI selfLink) {
        return new PaymentWithAllLinks(
                paymentConnector.getChargeId(),
                paymentConnector.getAmount(),
                paymentConnector.getState(),
                paymentConnector.getReturnUrl(),
                paymentConnector.getDescription(),
                paymentConnector.getReference(),
                paymentConnector.getEmail(),
                paymentConnector.getPaymentProvider(),
                paymentConnector.getCreatedDate(),
                paymentConnector.getLinks(),
                selfLink
        );
    }

    public static PaymentWithAllLinks valueOf(ChargeFromResponse paymentConnector,
                                              URI selfLink,
                                              URI paymentEventsUri,
                                              URI paymentCancelUri,
                                              URI paymentRefundsUri) {
        return new PaymentWithAllLinks(
                paymentConnector.getChargeId(),
                paymentConnector.getAmount(),
                paymentConnector.getState(),
                paymentConnector.getReturnUrl(),
                paymentConnector.getDescription(),
                paymentConnector.getReference(),
                paymentConnector.getEmail(),
                paymentConnector.getPaymentProvider(),
                paymentConnector.getCreatedDate(),
                paymentConnector.getLanguage(),
                paymentConnector.getDelayedCapture(),
                paymentConnector.getRefundSummary(),
                paymentConnector.getSettlementSummary(),
                paymentConnector.getCardDetails(),
                paymentConnector.getLinks(),
                selfLink,
                paymentEventsUri,
                paymentCancelUri,
                paymentRefundsUri,
                paymentConnector.getCorporateCardSurcharge(), 
                paymentConnector.getTotalAmount()
        );
    }

    public static PaymentWithAllLinks getPaymentWithLinks(
            TokenPaymentType paymentType,
            ChargeFromResponse paymentConnector,
            URI selfLink,
            URI paymentEventsUri,
            URI paymentCancelUri,
            URI paymentRefundsUri) {
        switch (paymentType) {
            case DIRECT_DEBIT:
                return PaymentWithAllLinks.valueOf(paymentConnector, selfLink);
            default:
                return PaymentWithAllLinks.valueOf(paymentConnector, selfLink, paymentEventsUri, paymentCancelUri, paymentRefundsUri);
        }
    }

    @Override
    public String toString() {
        return getPayment().toString();
    }
}
