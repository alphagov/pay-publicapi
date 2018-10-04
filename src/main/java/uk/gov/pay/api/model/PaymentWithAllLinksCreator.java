package uk.gov.pay.api.model;

import uk.gov.pay.api.model.generated.CardDetails;
import uk.gov.pay.api.model.CardPayment;
import uk.gov.pay.api.model.generated.DirectDebitPayment;
import uk.gov.pay.api.model.generated.Link;
import uk.gov.pay.api.model.generated.Payment;
import uk.gov.pay.api.model.generated.PaymentLinks;
import uk.gov.pay.api.model.generated.PaymentState;
import uk.gov.pay.api.model.generated.PaymentWithAllLinks;
import uk.gov.pay.api.model.generated.PostLink;
import uk.gov.pay.api.model.generated.RefundSummary;
import uk.gov.pay.api.model.generated.SettlementSummary;
import uk.gov.pay.commons.model.SupportedLanguage;

import javax.ws.rs.HttpMethod;
import java.net.URI;
import java.util.List;

import static javax.ws.rs.HttpMethod.GET;
import static javax.ws.rs.HttpMethod.POST;

public class PaymentWithAllLinksCreator {

    private static final String NEXT_URL = "next_url";
    private static final String NEXT_URL_POST = "next_url_post";

    public static PaymentWithAllLinks getPaymentWithLinks(
            TokenPaymentType paymentType,
            ChargeFromResponse paymentConnector,
            URI selfLink,
            URI paymentEventsUri,
            URI paymentCancelUri,
            URI paymentRefundsUri) {
        switch (paymentType) {
            case DIRECT_DEBIT:
                return valueOf(paymentConnector, selfLink);
            default:
                return valueOf(paymentConnector, selfLink, paymentEventsUri, paymentCancelUri, paymentRefundsUri);
        }
    }

    public static PaymentWithAllLinks valueOf(ChargeFromResponse paymentConnector, URI selfLink) {
        Payment payment = new DirectDebitPayment()
                .paymentId(paymentConnector.getChargeId())
                .amount(paymentConnector.getAmount())
                .state(paymentConnector.getState())
                .returnUrl(paymentConnector.getReturnUrl())
                .description(paymentConnector.getDescription())
                .reference(paymentConnector.getReference())
                .email(paymentConnector.getEmail())
                .paymentProvider(paymentConnector.getPaymentProvider())
                .createdDate(paymentConnector.getCreatedDate());
        PaymentLinks paymentLinks = new PaymentLinks().self(new Link().href(selfLink.toString()).method(GET));
        addKnownLinksValueOf(paymentConnector.getLinks(), paymentLinks);
        return new PaymentWithAllLinks().links(paymentLinks).payment(payment);
    }

    private static void addKnownLinksValueOf(List<PaymentConnectorResponseLink> chargeLinks, PaymentLinks paymentLinks) {
        addNextUrlIfPresent(chargeLinks, paymentLinks);
        addNextUrlPostIfPresent(chargeLinks, paymentLinks);
    }

    private static void addNextUrlPostIfPresent(List<PaymentConnectorResponseLink> chargeLinks, PaymentLinks paymentLinks) {
        chargeLinks.stream()
                .filter(chargeLink -> NEXT_URL_POST.equals(chargeLink.getRel()))
                .findFirst()
                .ifPresent(chargeLink -> 
                        paymentLinks.nextUrlPost(
                                new PostLink().href(chargeLink.getHref()).method(chargeLink.getMethod()).type(chargeLink.getType()).params(chargeLink.getParams())));
    }

    private static void addNextUrlIfPresent(List<PaymentConnectorResponseLink> chargeLinks, PaymentLinks paymentLinks) {
        chargeLinks.stream()
                .filter(chargeLink -> NEXT_URL.equals(chargeLink.getRel()))
                .findFirst()
                .ifPresent(chargeLink -> paymentLinks.nextUrl(new Link().href(chargeLink.getHref()).method(chargeLink.getMethod())));
    }

    public static PaymentWithAllLinks valueOf(ChargeFromResponse paymentConnector,
                                              URI selfLink,
                                              URI paymentEventsUri,
                                              URI paymentCancelUri,
                                              URI paymentRefundsUri) {
        Payment payment = new CardPayment(
                paymentConnector.getChargeId(),
                paymentConnector.getAmount(),
                paymentConnector.getState(),
                paymentConnector.getReturnUrl(),
                paymentConnector.getDescription(),
                paymentConnector.getReference(),
                paymentConnector.getEmail(),
                paymentConnector.getPaymentProvider(),
                paymentConnector.getCreatedDate(),
                paymentConnector.getRefundSummary(),
                paymentConnector.getSettlementSummary(),
                paymentConnector.getCardDetails(),
                paymentConnector.getLanguage(),
                paymentConnector.getDelayedCapture()
        );
        PaymentLinks paymentLinks = new PaymentLinks().self(new Link().href(selfLink.toString()).method(GET));
        addKnownLinksValueOf(paymentConnector.getLinks(), paymentLinks);
        paymentLinks.events(new Link().href(paymentEventsUri.toString()).method(GET));
        paymentLinks.refunds(new Link().href(paymentRefundsUri.toString()).method(GET));

        if (!paymentConnector.getState().isFinished()) {
            paymentLinks.cancel(new PostLink().href(paymentCancelUri.toString()).method(POST));
        }

        return new PaymentWithAllLinks().links(paymentLinks).payment(payment);
    }

    public static PaymentWithAllLinks valueOf(String chargeId, long amount, PaymentState state, String returnUrl, String description,
                                              String reference, String email, String paymentProvider, String createdDate, SupportedLanguage language,
                                              boolean delayedCapture, RefundSummary refundSummary, SettlementSummary settlementSummary, CardDetails cardDetails,
                                              List<PaymentConnectorResponseLink> paymentConnectorResponseLinks, URI selfLink, URI paymentEventsUri, URI paymentCancelUri,
                                              URI paymentRefundsUri) {

        Payment payment = new CardPayment(chargeId, amount, state, returnUrl, description, reference, email, 
                paymentProvider, createdDate, refundSummary, settlementSummary, cardDetails, language, delayedCapture);
        PaymentLinks paymentLinks = new PaymentLinks().self(new Link().href(selfLink.toString()));
        addKnownLinksValueOf(paymentConnectorResponseLinks, paymentLinks);
        paymentLinks.events(new Link().href(paymentEventsUri.toString()));
        paymentLinks.refunds(new Link().href(paymentRefundsUri.toString()));

        if (!state.isFinished()) {
            paymentLinks.cancel(new PostLink().href(paymentCancelUri.toString()));
        }

        return new PaymentWithAllLinks().links(paymentLinks).payment(payment);
    }
}
