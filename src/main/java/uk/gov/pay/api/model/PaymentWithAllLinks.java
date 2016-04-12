package uk.gov.pay.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import uk.gov.pay.api.model.links.PaymentLinks;

import java.net.URI;
import java.util.List;

public class PaymentWithAllLinks extends Payment {

    @JsonProperty(LINKS_JSON_ATTRIBUTE)
    private PaymentLinks links = new PaymentLinks();

    private PaymentWithAllLinks(String chargeId, long amount, String status, String returnUrl, String description,
                                String reference, String paymentProvider, String createdDate,
                                List<PaymentConnectorResponseLink> paymentConnectorResponseLinks,
                                URI selfLink, URI paymentEventsLink) {
        super(chargeId, amount, status, returnUrl, description, reference, paymentProvider, createdDate);

        this.links.addSelf(selfLink.toString());
        this.links.addKnownLinksValueOf(paymentConnectorResponseLinks);
        this.links.addEvents(paymentEventsLink.toString());
    }

    public static PaymentWithAllLinks valueOf(PaymentConnectorResponse paymentConnector, URI selfLink, URI paymentEventsUri) {
        return new PaymentWithAllLinks(
                paymentConnector.getChargeId(),
                paymentConnector.getAmount(),
                paymentConnector.getStatus(),
                paymentConnector.getReturnUrl(),
                paymentConnector.getDescription(),
                paymentConnector.getReference(),
                paymentConnector.getPaymentProvider(),
                paymentConnector.getCreated_date(),
                paymentConnector.getLinks(),
                selfLink,
                paymentEventsUri
        );
    }

    @ApiModelProperty(dataType = "uk.gov.pay.api.model.links.PaymentLinks")
    public PaymentLinks getLinks() {
        return links;
    }
}
