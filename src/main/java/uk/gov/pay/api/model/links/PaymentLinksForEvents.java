package uk.gov.pay.api.model.links;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import static javax.ws.rs.HttpMethod.GET;

@ApiModel(value = "PaymentLinksForEvents", description = "links for events resource")
@Schema(name = "PaymentLinksForEvents", description = "links for events resource")
public class PaymentLinksForEvents {

    public static final String SELF = "self";

    @JsonProperty(value = SELF)
    private Link self;

    @ApiModelProperty(value = SELF, dataType = "uk.gov.pay.api.model.links.Link")
    @Schema(description = SELF)
    public Link getSelf() {
        return self;
    }

    public void addSelf(String href) {
        this.self = new Link(href, GET);
    }

}
