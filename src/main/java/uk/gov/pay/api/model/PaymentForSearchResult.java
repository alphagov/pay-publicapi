package uk.gov.pay.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import uk.gov.pay.api.model.links.PaymentLinksForSearch;

import java.net.URI;

public class PaymentForSearchResult extends Payment {

    @JsonProperty(LINKS_JSON_ATTRIBUTE)
    private PaymentLinksForSearch links = new PaymentLinksForSearch();

    public PaymentForSearchResult(String chargeId, long amount, PaymentState state, String returnUrl, String description,
                                  String reference, String paymentProvider, String createdDate,
                                  URI selfLink, URI paymentEventsLink, URI paymentCancelLink) {
        super(chargeId, amount, state, returnUrl, description, reference, paymentProvider, createdDate);
        this.links.addSelf(selfLink.toString());
        this.links.addEvents(paymentEventsLink.toString());

        if (!state.isFinished()) {
            this.links.addCancel(paymentCancelLink.toString());
        }
    }

    public static PaymentForSearchResult valueOf(
            PaymentConnectorResponse paymentConnectorResponse,
            URI selfLink,
            URI paymentEventsLink,
            URI paymentCancelLink) {
        return new PaymentForSearchResult(
                paymentConnectorResponse.getChargeId(),
                paymentConnectorResponse.getAmount(),
                paymentConnectorResponse.getState(),
                paymentConnectorResponse.getReturnUrl(),
                paymentConnectorResponse.getDescription(),
                paymentConnectorResponse.getReference(),
                paymentConnectorResponse.getPaymentProvider(),
                paymentConnectorResponse.getCreated_date(),
                selfLink,
                paymentEventsLink,
                paymentCancelLink
        );
    }

    @ApiModelProperty(dataType = "uk.gov.pay.api.model.links.PaymentLinksForSearch")
    public PaymentLinksForSearch getLinks() {
        return links;
    }
}
