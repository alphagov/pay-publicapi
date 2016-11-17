package uk.gov.pay.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import uk.gov.pay.api.model.links.PaymentLinks;

import java.net.URI;
import java.util.List;

public class PaymentWithAllLinks extends Payment {

    @JsonProperty(LINKS_JSON_ATTRIBUTE)
    private PaymentLinks links = new PaymentLinks();

    private PaymentWithAllLinks(String chargeId, long amount, PaymentState state, String returnUrl, String description,
                                String reference, String email, String paymentProvider, String createdDate,
                                RefundSummary refundSummary, CardDetails cardDetails, List<PaymentConnectorResponseLink> paymentConnectorResponseLinks,
                                URI selfLink, URI paymentEventsUri, URI paymentCancelUri, URI paymentRefundsUri) {
        super(chargeId, amount, state, returnUrl, description, reference, email, paymentProvider, createdDate, refundSummary, cardDetails);
        this.links.addSelf(selfLink.toString());
        this.links.addKnownLinksValueOf(paymentConnectorResponseLinks);
        this.links.addEvents(paymentEventsUri.toString());
        this.links.addRefunds(paymentRefundsUri.toString());

        if (!state.isFinished()) {
            this.links.addCancel(paymentCancelUri.toString());
        }
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
                paymentConnector.getCardDetails(),
                paymentConnector.getLinks(),
                selfLink,
                paymentEventsUri,
                paymentCancelUri,
                paymentRefundsUri
        );
    }

    @ApiModelProperty(dataType = "uk.gov.pay.api.model.links.PaymentLinks")
    public PaymentLinks getLinks() {
        return links;
    }
}
