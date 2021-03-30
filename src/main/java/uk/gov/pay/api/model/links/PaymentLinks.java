package uk.gov.pay.api.model.links;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import uk.gov.pay.api.model.PaymentConnectorResponseLink;

import java.util.List;

import static javax.ws.rs.HttpMethod.GET;
import static javax.ws.rs.HttpMethod.POST;

@Schema(name = "PaymentLinks", description = "links for payment")
public class PaymentLinks {

    private static final String SELF = "self";
    private static final String NEXT_URL = "next_url";
    private static final String NEXT_URL_POST = "next_url_post";
    private static final String EVENTS = "events";
    private static final String CANCEL = "cancel";
    private static final String REFUNDS = "refunds";
    private static final String CAPTURE = "capture";

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
    
    @JsonProperty(value = CAPTURE)
    private PostLink capture;

    public Link getSelf() {
        return self;
    }

    public Link getNextUrl() {
        return nextUrl;
    }

    public PostLink getNextUrlPost() {
        return nextUrlPost;
    }

    public Link getEvents() {
        return events;
    }

    public Link getRefunds() {
        return refunds;
    }

    public PostLink getCancel() {
        return cancel;
    }

    public PostLink getCapture() {
        return capture;
    }

    public void addKnownLinksValueOf(List<PaymentConnectorResponseLink> chargeLinks) {
        addNextUrlIfPresent(chargeLinks);
        addNextUrlPostIfPresent(chargeLinks);
        addCaptureUrlIfPresent(chargeLinks);
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
    
    public void addCapture(String href) {
        this.capture = new PostLink(href, POST);
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

    private void addCaptureUrlIfPresent(List<PaymentConnectorResponseLink> chargeLinks) {
        chargeLinks.stream()
                .filter(chargeLink -> CAPTURE.equals(chargeLink.getRel()))
                .findFirst()
                .ifPresent(chargeLink -> this.capture = new PostLink(chargeLink.getHref(), chargeLink.getMethod()));
    }
}
