package uk.gov.pay.api.model.links;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import io.swagger.annotations.ApiModelProperty;
import uk.gov.pay.api.model.*;

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

    private PaymentWithAllLinks(String chargeId, long amount, PaymentState state, String returnUrl, String description,
                                String reference, String email, String paymentProvider, String createdDate,
                                RefundSummary refundSummary, SettlementSummary settlementSummary, CardDetails cardDetails, List<PaymentConnectorResponseLink> paymentConnectorResponseLinks,
                                URI selfLink, URI paymentEventsUri, URI paymentCancelUri, URI paymentRefundsUri) {
        this.payment = new CardPayment(chargeId, amount, state, returnUrl, description, reference, email, paymentProvider, createdDate, refundSummary, settlementSummary, cardDetails);
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
                paymentConnector.getRefundSummary(),
                paymentConnector.getSettlementSummary(),
                paymentConnector.getCardDetails(),
                paymentConnector.getLinks(),
                selfLink,
                paymentEventsUri,
                paymentCancelUri,
                paymentRefundsUri
        );
    }

    public static PaymentWithAllLinks getPaymentWithLinks(
            TokenPaymentType paymentType,
            ChargeFromResponse paymentConnector,
            URI selfLink,
            URI paymentEventsUri,
            URI paymentCancelUri,
            URI paymentRefundsUri){
        switch (paymentType) {
            case DIRECT_DEBIT:
                return PaymentWithAllLinks.valueOf(paymentConnector, selfLink);
            default:
                return PaymentWithAllLinks.valueOf(paymentConnector, selfLink, paymentEventsUri, paymentCancelUri, paymentRefundsUri);
        }
    }
}
