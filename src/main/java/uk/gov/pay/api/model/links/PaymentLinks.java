package uk.gov.pay.api.model.links;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import uk.gov.pay.api.model.PaymentConnectorResponseLink;

import java.util.List;

import static javax.ws.rs.HttpMethod.GET;
import static javax.ws.rs.HttpMethod.POST;

@ApiModel(value = "allLinksForAPayment", description = "self,events and next links of a Payment")
public class PaymentLinks {

    private static final String SELF = "self";
    private static final String NEXT_URL = "next_url";
    private static final String NEXT_URL_POST = "next_url_post";
    private static final String EVENTS = "events";
    private static final String CANCEL = "cancel";
    private static final String REFUNDS = "refunds";

    @JsonProperty(value = SELF)
    private Link self;

    @JsonProperty(NEXT_URL)
    private Link nextUrl;

    @JsonProperty(NEXT_URL_POST)
    private PostLink nextUrlPost;

    @JsonProperty(value = EVENTS)
    private Link events;

    @JsonProperty(value = REFUNDS)
    private Link refunds;

    @JsonProperty(value = CANCEL)
    private PostLink cancel;

    @ApiModelProperty(value = SELF, dataType = "uk.gov.pay.api.model.links.Link")
    public Link getSelf() {
        return self;
    }

    @ApiModelProperty(value = NEXT_URL, dataType = "uk.gov.pay.api.model.links.Link")
    public Link getNextUrl() {
        return nextUrl;
    }

    @ApiModelProperty(value = NEXT_URL_POST, dataType = "uk.gov.pay.api.model.links.PostLink")
    public PostLink getNextUrlPost() {
        return nextUrlPost;
    }

    @ApiModelProperty(value = EVENTS, dataType = "uk.gov.pay.api.model.links.Link")
    public Link getEvents() {
        return events;
    }

    @ApiModelProperty(value = REFUNDS, dataType = "uk.gov.pay.api.model.links.Link")
    public Link getRefunds() {
        return refunds;
    }

    @ApiModelProperty(value = CANCEL, dataType = "uk.gov.pay.api.model.links.PostLink")
    public PostLink getCancel() {
        return cancel;
    }

    public void addKnownLinksValueOf(List<PaymentConnectorResponseLink> chargeLinks) {
        addNextUrlIfPresent(chargeLinks);
        addNextUrlPostIfPresent(chargeLinks);
    }

    public void addSelf(String href) {
        this.self = new Link(href, GET);
    }

    public void addEvents(String href) {
        this.events = new Link(href, GET);
    }

    public void addRefunds(String href) {
        this.refunds = new Link(href, GET);
    }

    public void addCancel(String href) {
        this.cancel = new PostLink(href, POST);
    }

    private void addNextUrlPostIfPresent(List<PaymentConnectorResponseLink> chargeLinks) {
        chargeLinks.stream()
                .filter(chargeLink -> NEXT_URL_POST.equals(chargeLink.getRel()))
                .findFirst()
                .ifPresent(chargeLink -> this.nextUrlPost = new PostLink(chargeLink.getHref(), chargeLink.getMethod(), chargeLink.getType(), chargeLink.getParams()));
    }

    private void addNextUrlIfPresent(List<PaymentConnectorResponseLink> chargeLinks) {
        chargeLinks.stream()
                .filter(chargeLink -> NEXT_URL.equals(chargeLink.getRel()))
                .findFirst()
                .ifPresent(chargeLink -> this.nextUrl = new Link(chargeLink.getHref(), chargeLink.getMethod()));
    }
}
