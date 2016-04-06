package uk.gov.pay.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.net.URI;

public class PaymentWithSelfLinks extends Payment {

    @JsonProperty(LINKS_JSON_ATTRIBUTE)
    private SelfLinks links = new SelfLinks();

    public PaymentWithSelfLinks(String chargeId, long amount, String status, String returnUrl, String description,
                                String reference, String paymentProvider, String createdDate, URI selfLink) {
        super(chargeId, amount, status, returnUrl, description, reference, paymentProvider, createdDate);
        this.links.addSelf(selfLink.toString());
    }

    public static PaymentWithSelfLinks valueOf(PaymentConnectorResponse paymentConnectorResponse, URI selfLink) {
        return new PaymentWithSelfLinks(
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

    @ApiModelProperty(dataType = "uk.gov.pay.api.model.SelfLinks")
    public SelfLinks getLinks() {
        return links;
    }
}
