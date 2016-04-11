package uk.gov.pay.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.net.URI;

public class PaymentWithLinks extends Payment {

    @JsonProperty(LINKS_JSON_ATTRIBUTE)
    private PaymentLinks links = new PaymentLinks();

    public PaymentWithLinks(String chargeId, long amount, String status, String returnUrl, String description,
                            String reference, String paymentProvider, String createdDate, URI selfLink) {
        super(chargeId, amount, status, returnUrl, description, reference, paymentProvider, createdDate);
        this.links.addSelf(selfLink.toString());
    }

    public static PaymentWithLinks valueOf(PaymentConnectorResponse paymentConnectorResponse, URI selfLink) {
        return new PaymentWithLinks(
                paymentConnectorResponse.getChargeId(),
                paymentConnectorResponse.getAmount(),
                paymentConnectorResponse.getStatus(),
                paymentConnectorResponse.getReturnUrl(),
                paymentConnectorResponse.getDescription(),
                paymentConnectorResponse.getReference(),
                paymentConnectorResponse.getPaymentProvider(),
                paymentConnectorResponse.getCreated_date(),
                selfLink
        );
    }

    @ApiModelProperty(dataType = "uk.gov.pay.api.model.PaymentLinks")
    public PaymentLinks getLinks() {
        return links;
    }
}
