package uk.gov.pay.api.model;

import io.dropwizard.jackson.JsonSnakeCase;
import io.swagger.annotations.ApiModel;

@ApiModel(value = "paymentLinks", description = "Resource links of a Payment")
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

    public String getHref() {
        return href;
    }

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
