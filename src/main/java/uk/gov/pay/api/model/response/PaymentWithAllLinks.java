package uk.gov.pay.api.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import uk.gov.pay.api.model.card.ChargeFromResponse;
import uk.gov.pay.api.model.DirectDebitPayment;
import uk.gov.pay.api.model.RefundSummary;
import uk.gov.pay.api.model.SettlementSummary;
import uk.gov.pay.api.model.TokenPaymentType;
import uk.gov.pay.api.model.card.ConnectorPaymentState;
import uk.gov.pay.commons.model.SupportedLanguage;
import uk.gov.pay.commons.model.charge.ExternalMetadata;

import java.net.URI;
import java.util.List;

import static uk.gov.pay.api.model.response.Payment.LINKS_JSON_ATTRIBUTE;

@ApiModel
public class PaymentWithAllLinks {

    @JsonUnwrapped
    private Payment payment;

    @JsonProperty(LINKS_JSON_ATTRIBUTE)
    private PaymentLinks links = new PaymentLinks();

    @ApiModelProperty(name = LINKS_JSON_ATTRIBUTE, dataType = "uk.gov.pay.api.model.response.PaymentLinks")
    public PaymentLinks getLinks() {
        return links;
    }

    @ApiModelProperty // don't name this property (so it is generated in docs unwrapped)
    public Payment getPayment() {
        return payment;
    }

    public PaymentWithAllLinks(String chargeId, long amount, PaymentState state, String returnUrl, String description,
                               String reference, String email, String paymentProvider, String createdDate, SupportedLanguage language,
                               boolean delayedCapture, RefundSummary refundSummary, SettlementSummary settlementSummary, CardDetails cardDetails,
                               List<HalLink> halLinks, URI selfLink, URI paymentEventsUri, URI paymentCancelUri,
                               URI paymentRefundsUri, URI paymentCaptureUri, Long corporateCardSurcharge, Long totalAmount, String providerId, ExternalMetadata metadata,
                               Long fee, Long netAmount) {
        this.payment = new CardPayment(chargeId, amount, state, returnUrl, description, reference, email, paymentProvider, createdDate,
                refundSummary, settlementSummary, cardDetails, language, delayedCapture, corporateCardSurcharge, totalAmount, providerId, metadata, fee, netAmount);
        this.links.addSelf(selfLink);
        this.links.addKnownLinksValueOf(halLinks);
        this.links.addEvents(paymentEventsUri);
        this.links.addRefunds(paymentRefundsUri);

        if (!state.isFinished()) {
            this.links.addCancel(paymentCancelUri);
        }

        if (halLinks.stream().anyMatch(link -> "capture".equals(link.getRel()))) {
            this.links.addCapture(paymentCaptureUri);
        }
    }

    private PaymentWithAllLinks(String chargeId, long amount, ConnectorPaymentState state, String returnUrl, String description,
                                String reference, String email, String paymentProvider, String createdDate, List<HalLink> halLinks,
                                URI selfLink) {
        this.payment = new DirectDebitPayment(chargeId, amount, PaymentState.from(state), returnUrl, description, reference, email, paymentProvider, createdDate);
        this.links.addSelf(selfLink);
        this.links.addKnownLinksValueOf(halLinks);
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
                                              URI paymentRefundsUri,
                                              URI paymentsCaptureUri) {
        return new PaymentWithAllLinks(
                paymentConnector.getChargeId(),
                paymentConnector.getAmount(),
                PaymentState.from(paymentConnector.getState()),
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
                paymentsCaptureUri,
                paymentConnector.getCorporateCardSurcharge(),
                paymentConnector.getTotalAmount(),
                paymentConnector.getGatewayTransactionId(),
                paymentConnector.getMetadata().orElse(null),
                paymentConnector.getFee(),
                paymentConnector.getNetAmount());
    }

    public static PaymentWithAllLinks getPaymentWithLinks(
            TokenPaymentType paymentType,
            ChargeFromResponse paymentConnector,
            URI selfLink,
            URI paymentEventsUri,
            URI paymentCancelUri,
            URI paymentRefundsUri,
            URI paymentsCaptureUri) {
        switch (paymentType) {
            case DIRECT_DEBIT:
                return PaymentWithAllLinks.valueOf(paymentConnector, selfLink);
            default:
                return PaymentWithAllLinks.valueOf(paymentConnector, selfLink, paymentEventsUri, paymentCancelUri, paymentRefundsUri, paymentsCaptureUri);
        }
    }

    @Override
    public String toString() {
        return getPayment().toString();
    }
}
