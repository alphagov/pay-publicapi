package uk.gov.pay.api.model.links;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;

@ApiModel(value = "SearchRefundsNavigationLinks", description = "Links to navigate through refund related pages")
public class SearchRefundsNavigationLinks {

    @JsonProperty(value = "self")
    private Link self;

    public Link getSelfPage() {
        return self;
    }

    public SearchRefundsNavigationLinks withSelfLink(String href) {
        this.self = new Link(href);
        return this;
    }
}
