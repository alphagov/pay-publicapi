package uk.gov.pay.api.model.links;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import static com.fasterxml.jackson.annotation.JsonInclude.Include;

@ApiModel(value = "paymentLink", description = "A link related to a payment")
@JsonInclude(Include.NON_NULL)
public class Link {

    private String href;
    private String method;

    Link(String href, String method) {
        this.href = href;
        this.method = method;
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
