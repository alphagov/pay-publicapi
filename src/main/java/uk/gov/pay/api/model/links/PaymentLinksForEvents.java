package uk.gov.pay.api.model.links;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import uk.gov.pay.api.model.response.Link;

import static javax.ws.rs.HttpMethod.GET;

@ApiModel(value = "PaymentLinksForEvents", description = "links for events resource")
public class PaymentLinksForEvents {

    public static final String SELF = "self";

    @JsonProperty(value = SELF)
    private Link self;

    @ApiModelProperty(value = SELF, dataType = "uk.gov.pay.api.model.response.Link")
    public Link getSelf() {
        return self;
    }

    public void addSelf(String href) {
        this.self = new Link(href, GET);
    }

}
