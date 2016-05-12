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
                                String reference, String paymentProvider, String createdDate,
                                List<PaymentConnectorResponseLink> paymentConnectorResponseLinks,
                                URI selfLink, URI paymentEventsLink, URI paymentCancelUri) {
        super(chargeId, amount, state, returnUrl, description, reference, paymentProvider, createdDate);

        this.links.addSelf(selfLink.toString());
        this.links.addKnownLinksValueOf(paymentConnectorResponseLinks);
        this.links.addEvents(paymentEventsLink.toString());

        if (!state.isFinished()) {
            this.links.addCancel(paymentCancelUri.toString());
        }
    }

    public static PaymentWithAllLinks valueOf(PaymentConnectorResponse paymentConnector,
                                              URI selfLink,
                                              URI paymentEventsUri,
                                              URI paymentCancelUri) {
        return new PaymentWithAllLinks(
                paymentConnector.getChargeId(),
                paymentConnector.getAmount(),
                paymentConnector.getState(),
                paymentConnector.getReturnUrl(),
                paymentConnector.getDescription(),
                paymentConnector.getReference(),
                paymentConnector.getPaymentProvider(),
                paymentConnector.getCreated_date(),
                paymentConnector.getLinks(),
                selfLink,
                paymentEventsUri,
                paymentCancelUri
        );
    }

    @ApiModelProperty(dataType = "uk.gov.pay.api.model.links.PaymentLinks")
    public PaymentLinks getLinks() {
        return links;
    }
}
