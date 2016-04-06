package uk.gov.pay.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import static javax.ws.rs.HttpMethod.GET;

@ApiModel(value = "payment-event-link", description = "Resource link for a payment of a payment event")
public class PaymentEventLink {

    public static final String PAYMENT_LINK = "payment_url";

    @JsonProperty(value = PAYMENT_LINK)
    private Link paymentLink;


    public PaymentEventLink(String href) {
        this.paymentLink = new Link(href, GET);
    }

    @ApiModelProperty(value = PAYMENT_LINK, dataType = "uk.gov.pay.api.model.Link")
    public Link getPaymentLink() {
        return paymentLink;
    }


    @Override
    public String toString() {
        return "PaymentEventLink{" +
                "paymentLink=" + paymentLink +
                '}';
    }


}
