package uk.gov.pay.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "paymentLinks", description = "Resource links of a Payment")
public class Links {

    private Link self;

    @JsonProperty("next_url")
    private Link nextUrl;

    @JsonProperty("next_url_post")
    private Link nextUrlPost;

    public void setSelf(String url) {
        this.self = Link.get(url);
    }

    public void setNextUrl(String url) {
        this.nextUrl = Link.get(url);
    }

    public void setNextUrlPost(String url, String type, JsonNode params) {
        this.nextUrlPost = Link.post(url, type, params);
    }

    @ApiModelProperty(value = "self", dataType = "uk.gov.pay.api.model.Link")
    public Link getSelf() {
        return self;
    }

    @ApiModelProperty(value = "next_url", dataType = "uk.gov.pay.api.model.Link")
    public Link getNextUrl() {
        return nextUrl;
    }

    @ApiModelProperty(value = "next_url_post", dataType = "uk.gov.pay.api.model.Link")
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
}
