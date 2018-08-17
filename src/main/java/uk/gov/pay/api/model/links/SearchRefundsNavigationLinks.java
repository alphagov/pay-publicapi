package uk.gov.pay.api.model.links;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;

@ApiModel(value = "SearchRefundsNavigationLinks", description = "Links to navigate through search refunds pages")
public class SearchRefundsNavigationLinks {

    @JsonProperty(value = "payment")
    private Link payment;

    @JsonProperty(value = "self")
    private Link self;
    
    public Link getPaymentPage() {
        return payment;
    }

    public Link getSelf() {
        return self;
    }

    public SearchRefundsNavigationLinks withPaymentLink(String href) {
        this.payment = new Link(href);
        return this;
    }

    public SearchRefundsNavigationLinks withSelfLink(String href) {
        this.self = new Link(href);
        return this;
    }
}
