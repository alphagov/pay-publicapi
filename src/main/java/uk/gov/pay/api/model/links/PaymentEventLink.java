package uk.gov.pay.api.model.links;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import uk.gov.pay.api.model.response.Link;

import static javax.ws.rs.HttpMethod.GET;

@ApiModel(value = "PaymentEventLink", description = "Resource link for a payment of a payment event")
public class PaymentEventLink {

    public static final String PAYMENT_LINK = "payment_url";

    @JsonProperty(value = PAYMENT_LINK)
    private Link paymentLink;


    public PaymentEventLink(String href) {
        this.paymentLink = new Link(href, GET);
    }

    @ApiModelProperty(value = PAYMENT_LINK, dataType = "uk.gov.pay.api.model.response.Link")
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
