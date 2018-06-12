package uk.gov.pay.api.model.links;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import static javax.ws.rs.HttpMethod.GET;
import static javax.ws.rs.HttpMethod.POST;

@ApiModel(value = "PaymentLinksForSearch", description = "links for search payment resource")
public class PaymentLinksForSearch {

    private static final String SELF = "self";
    private static final String EVENTS = "events";
    private static final String CANCEL = "cancel";
    private static final String REFUNDS = "refunds";

    @JsonProperty(value = SELF)
    private Link self;

    @JsonProperty(value = CANCEL)
    private PostLink cancel;

    @JsonProperty(value = EVENTS)
    private Link events;

    @JsonProperty(value = REFUNDS)
    private Link refunds;

    @ApiModelProperty(value = SELF, dataType = "uk.gov.pay.api.model.links.Link")
    public Link getSelf() {
        return self;
    }

    @ApiModelProperty(value = CANCEL, dataType = "uk.gov.pay.api.model.links.PostLink")
    public PostLink getCancel() {
        return cancel;
    }

    @ApiModelProperty(value = EVENTS, dataType = "uk.gov.pay.api.model.links.Link")
    public Link getEvents() {
        return events;
    }

    @ApiModelProperty(value = REFUNDS, dataType = "uk.gov.pay.api.model.links.Link")
    public Link getRefunds() {
        return refunds;
    }

    public void addSelf(String href) {
        this.self = new Link(href, GET);
    }

    public void addEvents(String href) {
        this.events = new Link(href, GET);
    }

    public void addCancel(String href) {
        this.cancel = new PostLink(href, POST);
    }

    public void addRefunds(String href) {
        this.refunds = new Link(href, GET);
    }
}
