package uk.gov.pay.api.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.net.URI;
import java.util.Map;
import java.util.Objects;

import static com.fasterxml.jackson.annotation.JsonInclude.Include;

@ApiModel(value = "PostLink", description = "A POST link related to a payment")
@JsonInclude(Include.NON_NULL)
public class PostLink extends Link {

    private String type;
    private Map<String, Object> params;

    public PostLink(String href, String method, String type, Map<String, Object> params) {
        super(href, method);
        this.type = type;
        this.params = params;
    }

    public PostLink(String href, String method) {
        super(href, method);
    }
    
    public PostLink(URI href, String method) {
        super(href, method);
    }

    @ApiModelProperty(example = "POST")
    public String getMethod() {
        return super.getMethod();
    }

    @ApiModelProperty(example = "application/x-www-form-urlencoded")
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        PostLink postLink = (PostLink) o;
        return Objects.equals(type, postLink.type) &&
                Objects.equals(params, postLink.params);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), type, params);
    }
}
