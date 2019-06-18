package uk.gov.pay.api.model.links.directdebit;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import uk.gov.pay.api.model.PaymentConnectorResponseLink;
import uk.gov.pay.api.model.links.Link;
import uk.gov.pay.api.model.links.PostLink;

import java.util.List;

import static javax.ws.rs.HttpMethod.GET;

@ApiModel(value = "MandateLinks", description = "self and next links of an Agreement")
public class MandateLinks {

    private static final String SELF = "self";
    private static final String NEXT_URL = "next_url";
    private static final String NEXT_URL_POST = "next_url_post";
    private static final String PAYMENTS = "payments";

    @JsonProperty(SELF)
    private Link self;

    @JsonProperty(NEXT_URL)
    private Link nextUrl;

    @JsonProperty(NEXT_URL_POST)
    private PostLink nextUrlPost;

    @JsonProperty(PAYMENTS)
    private Link payments;

    @ApiModelProperty(value = SELF, dataType = "uk.gov.pay.api.model.links.Link")
    public Link getSelf() {
        return self;
    }

    @ApiModelProperty(value = NEXT_URL, dataType = "uk.gov.pay.api.model.links.Link")
    public Link getNextUrl() {
        return nextUrl;
    }

    @ApiModelProperty(value = NEXT_URL_POST, dataType = "uk.gov.pay.api.model.links.PostLink")
    public PostLink getNextUrlPost() {
        return nextUrlPost;
    }

    @ApiModelProperty(value = PAYMENTS, dataType = "uk.gov.pay.api.model.links.Link")
    public Link getPayments() {
        return payments;
    }

    private MandateLinks() {
    }

    public static final class MandateLinksBuilder {
        private Link self;
        private Link nextUrl;
        private PostLink nextUrlPost;
        private Link payments;

        private MandateLinksBuilder() {
        }

        public static MandateLinksBuilder aMandateLinks() {
            return new MandateLinksBuilder();
        }

        public MandateLinksBuilder withSelf(String selfLink) {
            this.self = new Link(selfLink, GET);
            return this;
        }

        public MandateLinksBuilder withNextUrl(List<PaymentConnectorResponseLink> chargeLinks) {
            chargeLinks.stream()
                    .filter(link -> NEXT_URL.equals(link.getRel()))
                    .findFirst()
                    .ifPresent(chargeLink -> this.nextUrl = new Link(chargeLink.getHref(), chargeLink.getMethod()));
            return this;
        }

        public MandateLinksBuilder withNextUrlPost(List<PaymentConnectorResponseLink> chargeLinks) {
            chargeLinks.stream()
                    .filter(link -> NEXT_URL_POST.equals(link.getRel()))
                    .findFirst()
                    .ifPresent(chargeLink -> this.nextUrlPost = new PostLink(chargeLink.getHref(), chargeLink.getMethod(), chargeLink.getType(), chargeLink.getParams()));
            return this;
        }

        public MandateLinksBuilder withPayments(String paymentsLink) {
            this.payments = new Link(paymentsLink, GET);
            return this;
        }

        public MandateLinks build() {
            MandateLinks mandateLinks = new MandateLinks();
            mandateLinks.nextUrl = this.nextUrl;
            mandateLinks.self = this.self;
            mandateLinks.nextUrlPost = this.nextUrlPost;
            mandateLinks.payments = this.payments;
            return mandateLinks;
        }
    }
}
