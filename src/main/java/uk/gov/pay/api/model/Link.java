package uk.gov.pay.api.model;

import io.dropwizard.jackson.JsonSnakeCase;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "paymentLink", description = "A link related to a payment")
@JsonSnakeCase
public class Link {
    private String href;
    private String method;

    public Link(String href, String method) {
        this.href = href;
        this.method = method;
    }

    public static Link get(String url) {
        return new Link(url, "GET");
    }

    @ApiModelProperty(example = "https://an.example.link/from/payment/platform")
    public String getHref() {
        return href;
    }

    @ApiModelProperty(example = "GET")
    public String getMethod() {
        return method;
    }

    @Override
    public String toString() {
        return "Link{" +
                "href='" + href + '\'' +
                ", method='" + method + '\'' +
                '}';
    }
}
