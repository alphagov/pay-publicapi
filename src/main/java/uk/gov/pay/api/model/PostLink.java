package uk.gov.pay.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Map;

import static com.fasterxml.jackson.annotation.JsonInclude.Include;

@ApiModel(value = "paymentPOSTLink", description = "A POST link related to a payment")
@JsonInclude(Include.NON_NULL)
public class PostLink extends Link {

    private String type;
    private Map<String, Object> params;

    PostLink(String href, String method, String type, Map<String, Object> params) {
        super(href, method);
        this.type = type;
        this.params = params;
    }

    @ApiModelProperty(example = "POST")
    public String getMethod() {
        return super.getMethod();
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
                "href='" + getHref() + '\'' +
                ", method='" + getMethod() + '\'' +
                ", type='" + type + '\'' +
                ", params=" + params +
                '}';
    }
}
