package uk.gov.pay.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import static javax.ws.rs.HttpMethod.GET;

@ApiModel(value = "selfLinks", description = "Resource self link of a Payment")
public class SelfLinks {

    public static final String SELF = "self";

    @JsonProperty(value = SELF)
    private Link self;

    @ApiModelProperty(value = SELF, dataType = "uk.gov.pay.api.model.Link")
    public Link getSelf() {
        return self;
    }


    @Override
    public String toString() {
        return "Links{" +
                "self=" + self +
                '}';
    }

    void addSelf(String href) {
        this.self = new Link(href, GET);
    }
}
