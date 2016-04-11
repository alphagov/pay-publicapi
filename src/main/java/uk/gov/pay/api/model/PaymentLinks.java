package uk.gov.pay.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import static javax.ws.rs.HttpMethod.GET;

@ApiModel(value = "selfLinks", description = "Resource self link of a Payment")
public class PaymentLinks {

    public static final String SELF = "self";
    public static final String EVENTS = "events";

    @JsonProperty(value = SELF)
    private Link self;

    @JsonProperty(value = EVENTS)
    private Link events;

    @ApiModelProperty(value = SELF, dataType = "uk.gov.pay.api.model.Link")
    public Link getSelf() {
        return self;
    }


    @ApiModelProperty(value = EVENTS, dataType = "uk.gov.pay.api.model.Link")
    public Link getEvents() {
        return events;
    }

    @Override
    public String toString() {
        return "PaymentLinks{" +
                "self=" + self +
                ", events=" + events +
                '}';
    }

    void addSelf(String href) {
        this.self = new Link(href, GET);
    }

    void addEvents(String href) {
        this.events = new Link(href, GET);
    }
}
