package uk.gov.pay.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

import static javax.ws.rs.HttpMethod.GET;

@ApiModel(value = "paymentLinks", description = "Resource links of a Payment")
public class Links {

    private static final String SELF = "self";
    private static final String NEXT_URL = "next_url";
    private static final String NEXT_URL_POST = "next_url_post";

    private Link self;

    @JsonProperty(NEXT_URL)
    private Link nextUrl;

    @JsonProperty(NEXT_URL_POST)
    private Link nextUrlPost;

    @ApiModelProperty(value = SELF, dataType = "uk.gov.pay.api.model.Link")
    public Link getSelf() {
        return self;
    }

    @ApiModelProperty(value = NEXT_URL, dataType = "uk.gov.pay.api.model.Link")
    public Link getNextUrl() {
        return nextUrl;
    }

    @ApiModelProperty(value = NEXT_URL_POST, dataType = "uk.gov.pay.api.model.Link")
    public Link getNextUrlPost() {
        return nextUrlPost;
    }

    @Override
    public String toString() {
        return "Links{" +
                "self=" + self +
                ", nextUrl=" + nextUrl +
                ", nextUrlPost=" + nextUrlPost +
                '}';
    }

    void addSelf(String href) {
        this.self = new Link(href, GET);
    }

    void addKnownLinksValueOf(List<PaymentConnectorResponseLink> paymentConnectorLinks) {
        addNextUrlIfPresent(paymentConnectorLinks);
        addNextUrlPostIfPresent(paymentConnectorLinks);
    }

    private void addNextUrlPostIfPresent(List<PaymentConnectorResponseLink> links) {
        links.stream()
                .filter(chargeLink -> NEXT_URL_POST.equals(chargeLink.getRel()))
                .findFirst()
                .ifPresent(chargeLink -> this.nextUrlPost = new Link(chargeLink.getHref(), chargeLink.getMethod(), chargeLink.getType(), chargeLink.getParams()));
    }

    private void addNextUrlIfPresent(List<PaymentConnectorResponseLink> links) {
        links.stream()
                .filter(chargeLink -> NEXT_URL.equals(chargeLink.getRel()))
                .findFirst()
                .ifPresent(chargeLink -> this.nextUrl = new Link(chargeLink.getHref(), chargeLink.getMethod()));
    }
}
