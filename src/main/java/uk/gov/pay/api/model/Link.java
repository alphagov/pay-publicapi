package uk.gov.pay.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Map;

import static com.fasterxml.jackson.annotation.JsonInclude.Include;

@ApiModel(value = "paymentLink", description = "A link related to a payment")
@JsonInclude(Include.NON_NULL)
public class Link {

    private String href;
    private String method;
    private String type;
    private Map<String, Object> params;

    Link(String href, String method) {
        this.href = href;
        this.method = method;
    }

    Link(String href, String method, String type, Map<String, Object> params) {
        this(href, method);
        this.type = type;
        this.params = params;
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

    @ApiModelProperty(example = "\"description\":\"This is a value for a parameter called description\"")
    public Map<String, Object> getParams() {
        return params;
    }

    @Override
    public String toString() {
        return "Link{" +
                "href='" + href + '\'' +
                ", method='" + method + '\'' +
                ", type='" + type + '\'' +
                ", params=" + params +
                '}';
    }
}
