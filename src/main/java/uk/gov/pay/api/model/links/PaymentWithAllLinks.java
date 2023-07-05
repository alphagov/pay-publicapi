package uk.gov.pay.api.model.links;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import uk.gov.pay.api.model.AuthorisationSummary;
import uk.gov.pay.api.model.CardDetails;
import uk.gov.pay.api.model.CardPayment;
import uk.gov.pay.api.model.Charge;
import uk.gov.pay.api.model.PaymentConnectorResponseLink;
import uk.gov.pay.api.model.PaymentSettlementSummary;
import uk.gov.pay.api.model.PaymentState;
import uk.gov.pay.api.model.RefundSummary;
import uk.gov.service.payments.commons.model.AuthorisationMode;
import uk.gov.service.payments.commons.model.SupportedLanguage;
import uk.gov.service.payments.commons.model.charge.ExternalMetadata;

import java.net.URI;
import java.util.List;

public class PaymentWithAllLinks {

    @JsonUnwrapped
    private CardPayment payment;

    @JsonProperty(CardPayment.LINKS_JSON_ATTRIBUTE)
    private PaymentLinks links = new PaymentLinks();

    public PaymentLinks getLinks() {
        return links;
    }

    public CardPayment getPayment() {
        return payment;
    }

    public PaymentWithAllLinks(String chargeId, long amount, PaymentState state, String returnUrl, String description,
                               String reference, String email, String paymentProvider, String createdDate, SupportedLanguage language,
                               boolean delayedCapture, boolean moto, RefundSummary refundSummary, PaymentSettlementSummary settlementSummary, CardDetails cardDetails,
                               List<PaymentConnectorResponseLink> paymentConnectorResponseLinks, URI selfLink, URI paymentEventsUri, URI paymentCancelUri,
                               URI paymentRefundsUri, URI paymentCaptureUri, URI paymentAuthorisationUri,  Long corporateCardSurcharge, Long totalAmount, String providerId, ExternalMetadata metadata,
                               Long fee, Long netAmount, AuthorisationSummary authorisationSummary, String agreementId, AuthorisationMode authorisationMode) {
        this.payment = new CardPayment(chargeId, amount, state, returnUrl, description, reference, email, paymentProvider, createdDate,
                refundSummary, settlementSummary, cardDetails, language, delayedCapture, moto, corporateCardSurcharge, totalAmount,
                providerId, metadata, fee, netAmount, authorisationSummary, agreementId, authorisationMode);
        this.links.addSelf(selfLink.toString());
        this.links.addKnownLinksValueOf(paymentConnectorResponseLinks, paymentAuthorisationUri);
        this.links.addEvents(paymentEventsUri.toString());
        this.links.addRefunds(paymentRefundsUri.toString());

        if (!state.isFinished() && authorisationMode != AuthorisationMode.AGREEMENT) {
            this.links.addCancel(paymentCancelUri.toString());
        }

        if (paymentConnectorResponseLinks.stream().anyMatch(link -> "capture".equals(link.getRel()))) {
            this.links.addCapture(paymentCaptureUri.toString());
        }
    }

    public static PaymentWithAllLinks valueOf(Charge paymentConnector,
                                              URI selfLink,
                                              URI paymentEventsUri,
                                              URI paymentCancelUri,
                                              URI paymentRefundsUri,
                                              URI paymentsCaptureUri,
                                              URI paymentAuthorisationUri) {
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
                paymentConnector.isMoto(),
                paymentConnector.getRefundSummary(),
                paymentConnector.getSettlementSummary(),
                paymentConnector.getCardDetails(),
                paymentConnector.getLinks(),
                selfLink,
                paymentEventsUri,
                paymentCancelUri,
                paymentRefundsUri,
                paymentsCaptureUri,
                paymentAuthorisationUri,
                paymentConnector.getCorporateCardSurcharge(),
                paymentConnector.getTotalAmount(),
                paymentConnector.getGatewayTransactionId(),
                paymentConnector.getMetadata().orElse(null),
                paymentConnector.getFee(),
                paymentConnector.getNetAmount(),
                paymentConnector.getAuthorisationSummary(),
                paymentConnector.getAgreementId(),
                paymentConnector.getAuthorisationMode());
    }

    public static PaymentWithAllLinks getPaymentWithLinks(
            Charge paymentConnector,
            URI selfLink,
            URI paymentEventsUri,
            URI paymentCancelUri,
            URI paymentRefundsUri,
            URI paymentsCaptureUri,
            URI paymentAuthorisationUri) {
        
        return PaymentWithAllLinks.valueOf(paymentConnector, selfLink, paymentEventsUri, paymentCancelUri, paymentRefundsUri, paymentsCaptureUri, paymentAuthorisationUri);
    }

    @Override
    public String toString() {
        return getPayment().toString();
    }
}
