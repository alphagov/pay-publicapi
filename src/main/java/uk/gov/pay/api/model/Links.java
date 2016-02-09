package uk.gov.pay.api.model;

import io.dropwizard.jackson.JsonSnakeCase;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value="paymentLinks", description = "Resource links of a Payment")
@JsonSnakeCase
public class Links {

    private Link self;
    private Link nextUrl;

    public void setSelf(String url) {
        this.self = Link.get(url);
    }

    public void setNext(String url) {
        this.nextUrl = Link.get(url);
    }

    @ApiModelProperty(value = "selfUrl", dataType = "uk.gov.pay.api.model.Link")
    public Link getSelf() {
        return self;
    }

    @ApiModelProperty(value = "nextUrl", dataType = "uk.gov.pay.api.model.Link")
    public Link getNextUrl() {
        return nextUrl;
    }

    @Override
    public String toString() {
        return "Links{" +
                "self=" + self +
                ", nextUrl=" + nextUrl +
                '}';
    }
}
