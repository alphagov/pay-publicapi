package uk.gov.pay.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

@ApiModel(value = "commonPaymentLinksAndNextLinks", description = "Resource self,events and next links of a Payment")
public class PaymentNextLinks extends PaymentLinks {

    private static final String NEXT_URL = "next_url";
    private static final String NEXT_URL_POST = "next_url_post";

    @JsonProperty(NEXT_URL)
    private Link nextUrl;

    @JsonProperty(NEXT_URL_POST)
    private Link nextUrlPost;

    @ApiModelProperty(value = NEXT_URL, dataType = "uk.gov.pay.api.model.Link")
    public Link getNextUrl() {
        return nextUrl;
    }

    @ApiModelProperty(value = NEXT_URL_POST, dataType = "uk.gov.pay.api.model.PostLink")
    public Link getNextUrlPost() {
        return nextUrlPost;
    }

    @Override
    public String toString() {
        return "Links{" +
                "self=" + super.getSelf() +
                ", nextUrl=" + nextUrl +
                ", nextUrlPost=" + nextUrlPost +
                '}';
    }

    void addKnownLinksValueOf(List<PaymentConnectorResponseLink> chargeLinks) {
        addNextUrlIfPresent(chargeLinks);
        addNextUrlPostIfPresent(chargeLinks);
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
