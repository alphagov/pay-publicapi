package uk.gov.pay.api.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.net.URI;

import static javax.ws.rs.HttpMethod.GET;
import static javax.ws.rs.HttpMethod.POST;

@ApiModel(value = "PaymentLinksForSearch", description = "links for search payment resource")
public class PaymentLinksForSearch {

    @JsonProperty(value = "self")
    private Link self;

    @JsonProperty(value = "cancel")
    private PostLink cancel;

    @JsonProperty(value = "events")
    private Link events;

    @JsonProperty(value = "refunds")
    private Link refunds;
    
    @JsonProperty(value = "capture")
    private PostLink capture;

    @ApiModelProperty(value = "self", dataType = "uk.gov.pay.api.model.response.Link")
    public Link getSelf() {
        return self;
    }

    @ApiModelProperty(value = "cancel", dataType = "uk.gov.pay.api.model.response.PostLink")
    public PostLink getCancel() {
        return cancel;
    }

    @ApiModelProperty(value = "events", dataType = "uk.gov.pay.api.model.response.Link")
    public Link getEvents() {
        return events;
    }

    @ApiModelProperty(value = "refunds", dataType = "uk.gov.pay.api.model.response.Link")
    public Link getRefunds() {
        return refunds;
    }

    @ApiModelProperty(value = "capture", dataType = "uk.gov.pay.api.model.response.PostLink")
    public Link getCapture() {
        return capture;
    }

    public void addSelf(URI href) {
        this.self = new Link(href, GET);
    }

    public void addEvents(URI href) {
        this.events = new Link(href, GET);
    }

    public void addCancel(URI href) {
        this.cancel = new PostLink(href, POST);
    }

    public void addRefunds(URI href) {
        this.refunds = new Link(href, GET);
    }

    public void addCapture(String href) {
        this.capture = new PostLink(href, POST);
    }
    
    public void addCapture(URI href) {
        this.capture = new PostLink(href, POST);
    }
}
