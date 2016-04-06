package uk.gov.pay.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.net.URI;
import java.util.List;

public class PaymentWithLinks extends Payment {

    @JsonProperty(LINKS_JSON_ATTRIBUTE)
    private SelfAndNextLinks links = new SelfAndNextLinks();

    private PaymentWithLinks(String chargeId, long amount, String status, String returnUrl, String description,
                             String reference, String paymentProvider, String createdDate,
                             List<PaymentConnectorResponseLink> paymentConnectorResponseLinks, URI selfLink) {
        super(chargeId, amount, status, returnUrl, description, reference, paymentProvider, createdDate);

        this.links.addSelf(selfLink.toString());
        this.links.addKnownLinksValueOf(paymentConnectorResponseLinks);

    }

    public static PaymentWithLinks valueOf(PaymentConnectorResponse paymentConnector, URI selfLink) {
        return new PaymentWithLinks(
                paymentConnector.getChargeId(),
                paymentConnector.getAmount(),
                paymentConnector.getStatus(),
                paymentConnector.getReturnUrl(),
                paymentConnector.getDescription(),
                paymentConnector.getReference(),
                paymentConnector.getPaymentProvider(),
                paymentConnector.getCreated_date(),
                paymentConnector.getLinks(),
                selfLink
        );
    }

    @ApiModelProperty(dataType = "uk.gov.pay.api.model.SelfAndNextLinks")
    public SelfAndNextLinks getLinks() {
        return links;
    }
}
