package uk.gov.pay.api.model.links;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import static com.fasterxml.jackson.annotation.JsonInclude.Include;

@ApiModel(value = "paymentLink", description = "A link related to a payment")
@JsonInclude(Include.NON_NULL)
public class Link {

    @JsonProperty(value = "href")
    private String href;
    @JsonProperty(value = "method")
    private String method;

    Link(String href, String method) {
        this.href = href;
        this.method = method;
    }

    Link(String href) {
        this.href = href;
    }

    public Link() {}

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
