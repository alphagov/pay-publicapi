package uk.gov.pay.api.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

@ApiModel(value = "PaymentLinks", description = "links for payment")
public class PaymentLinks extends PaymentLinksForSearch {

    private static final String NEXT_URL = "next_url";
    private static final String NEXT_URL_POST = "next_url_post";

    @JsonProperty(NEXT_URL)
    private Link nextUrl;

    @JsonProperty(NEXT_URL_POST)
    private PostLink nextUrlPost;

    @ApiModelProperty(value = NEXT_URL, dataType = "uk.gov.pay.api.model.response.Link")
    public Link getNextUrl() {
        return nextUrl;
    }

    @ApiModelProperty(value = NEXT_URL_POST, dataType = "uk.gov.pay.api.model.response.PostLink")
    public PostLink getNextUrlPost() {
        return nextUrlPost;
    }
    
    public void addKnownLinksValueOf(List<HalLink> chargeLinks) {
        addNextUrlIfPresent(chargeLinks);
        addNextUrlPostIfPresent(chargeLinks);
        addCaptureUrlIfPresent(chargeLinks);
    }

    private void addNextUrlPostIfPresent(List<HalLink> chargeLinks) {
        chargeLinks.stream()
                .filter(chargeLink -> NEXT_URL_POST.equals(chargeLink.getRel()))
                .findFirst()
                .ifPresent(chargeLink -> this.nextUrlPost = new PostLink(chargeLink.getHref(), chargeLink.getMethod(), chargeLink.getType(), chargeLink.getParams()));
    }

    private void addNextUrlIfPresent(List<HalLink> chargeLinks) {
        chargeLinks.stream()
                .filter(chargeLink -> NEXT_URL.equals(chargeLink.getRel()))
                .findFirst()
                .ifPresent(chargeLink -> this.nextUrl = new Link(chargeLink.getHref(), chargeLink.getMethod()));
    }

    private void addCaptureUrlIfPresent(List<HalLink> chargeLinks) {
        chargeLinks.stream()
                .filter(chargeLink -> "capture".equals(chargeLink.getRel()))
                .findFirst()
                .ifPresent(chargeLink -> addCapture(chargeLink.getHref()));
    }
}
