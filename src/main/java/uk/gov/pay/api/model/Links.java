package uk.gov.pay.api.model;

import io.dropwizard.jackson.JsonSnakeCase;
import io.swagger.annotations.ApiModel;

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

    public Link getSelf() {
        return self;
    }

    public Link getNextUrl() {
        return nextUrl;
    }
}
