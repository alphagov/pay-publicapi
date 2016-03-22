package uk.gov.pay.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import static com.fasterxml.jackson.annotation.JsonInclude.Include;

@ApiModel(value = "paymentLink", description = "A link related to a payment")
@JsonInclude(Include.NON_NULL)
public class Link {

    private String href;
    private String method;
    private String type;
    private JsonNode params;

    private Link(String href, String method) {
        this.href = href;
        this.method = method;
    }

    private Link(String href, String method, String type, JsonNode params) {
        this.href = href;
        this.method = method;
        this.type = type;
        this.params = params;
    }

    public static Link get(String url) {
        return new Link(url, "GET");
    }

    public static Link post(String url, String type, JsonNode params) {
        return new Link(url, "POST", type, params);
    }

    @ApiModelProperty(example = "https://an.example.link/from/payment/platform")
    public String getHref() {
        return href;
    }

    @ApiModelProperty(example = "GET")
    public String getMethod() {
        return method;
    }

    @ApiModelProperty(example = "multipart/form-data")
    public String getType() {
        return type;
    }

    @ApiModelProperty(example = "\"description\":\"This is a value for description\"")
    public JsonNode getParams() {
        return params;
    }

    @Override
    public String toString() {
        return "Link{" +
                "href='" + href + '\'' +
                ", method='" + method + '\'' +
                '}';
    }
}
