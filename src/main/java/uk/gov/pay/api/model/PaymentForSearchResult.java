package uk.gov.pay.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import uk.gov.pay.api.model.links.PaymentLinksForSearch;

import java.net.URI;

public class PaymentForSearchResult extends Payment {

    @JsonProperty(LINKS_JSON_ATTRIBUTE)
    private PaymentLinksForSearch links = new PaymentLinksForSearch();

    public PaymentForSearchResult(String chargeId, long amount, String status, String returnUrl, String description,
                                  String reference, String paymentProvider, String createdDate,
                                  URI selfLink, URI paymentEventsLink) {
        super(chargeId, amount, status, returnUrl, description, reference, paymentProvider, createdDate);
        this.links.addSelf(selfLink.toString());
        this.links.addEvents(paymentEventsLink.toString());
    }

    public static PaymentForSearchResult valueOf(
            PaymentConnectorResponse paymentConnectorResponse,
            URI selfLink,
            URI paymentEventsLink) {
        return new PaymentForSearchResult(
                paymentConnectorResponse.getChargeId(),
                paymentConnectorResponse.getAmount(),
                paymentConnectorResponse.getStatus(),
                paymentConnectorResponse.getReturnUrl(),
                paymentConnectorResponse.getDescription(),
                paymentConnectorResponse.getReference(),
                paymentConnectorResponse.getPaymentProvider(),
                paymentConnectorResponse.getCreated_date(),
                selfLink,
                paymentEventsLink
        );
    }

    @ApiModelProperty(dataType = "uk.gov.pay.api.model.links.PaymentLinksForSearch")
    public PaymentLinksForSearch getLinks() {
        return links;
    }
}
