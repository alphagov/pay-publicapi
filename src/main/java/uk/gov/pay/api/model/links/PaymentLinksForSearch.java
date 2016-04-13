package uk.gov.pay.api.model.links;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import static javax.ws.rs.HttpMethod.GET;

@ApiModel(value = "linksForSearchResults", description = "links for search payment resource")
public class PaymentLinksForSearch {

    public static final String SELF = "self";
    public static final String EVENTS = "events";

    @JsonProperty(value = SELF)
    private Link self;

    @JsonProperty(value = EVENTS)
    private Link events;

    @ApiModelProperty(value = SELF, dataType = "uk.gov.pay.api.model.links.Link")
    public Link getSelf() {
        return self;
    }

    @ApiModelProperty(value = EVENTS, dataType = "uk.gov.pay.api.model.links.Link")
    public Link getEvents() {
        return events;
    }

    public void addSelf(String href) {
        this.self = new Link(href, GET);
    }

    public void addEvents(String href) {
        this.events = new Link(href, GET);
    }
}
