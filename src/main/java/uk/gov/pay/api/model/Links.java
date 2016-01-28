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

    @ApiModelProperty(example = "https://publicapi-integration-1.pymnt.uk/v1/payments/12345")
    public Link getSelf() {
        return self;
    }

    @ApiModelProperty(example = "https://www-integration-1.pymnt.uk/charge/12345?chargeTokenId=3671c717-2a0c-4655-92d3-348c7c7b04fb")
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
