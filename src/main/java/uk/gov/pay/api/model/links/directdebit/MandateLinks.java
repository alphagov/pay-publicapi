package uk.gov.pay.api.model.links.directdebit;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import uk.gov.pay.api.model.PaymentConnectorResponseLink;
import uk.gov.pay.api.model.links.Link;
import uk.gov.pay.api.model.links.PostLink;

import java.net.URI;
import java.util.List;

import static javax.ws.rs.HttpMethod.GET;

@Schema(name = "MandateLinks", description = "payment, events, self and next links of a Mandate")
public class MandateLinks {

    private static final String SELF = "self";
    private static final String NEXT_URL = "next_url";
    private static final String NEXT_URL_POST = "next_url_post";
    private static final String PAYMENTS = "payments";
    private static final String EVENTS = "events";

    @JsonProperty(SELF)
    private final Link self;

    @JsonProperty(NEXT_URL)
    private final Link nextUrl;

    @JsonProperty(NEXT_URL_POST)
    private final PostLink nextUrlPost;

    @JsonProperty(PAYMENTS)
    private final Link payments;
    
    @Schema(hidden = true)
    @JsonProperty(EVENTS)
    private final Link events;

    @Schema(name = SELF, description = "A link related to mandate")
    public Link getSelf() {
        return self;
    }

    @Schema(name = NEXT_URL, description = "A link related to mandate")
    public Link getNextUrl() {
        return nextUrl;
    }

    public PostLink getNextUrlPost() {
        return nextUrlPost;
    }

    @Schema(name = PAYMENTS, description = "A link related to payments")
    public Link getPayments() {
        return payments;
    }

    @Schema(hidden = true)
    public Link getEvents() {
        return events;
    }

    private MandateLinks(Link self, Link nextUrl, PostLink nextUrlPost, Link payments, Link events) {
        this.self = self;
        this.nextUrl = nextUrl;
        this.nextUrlPost = nextUrlPost;
        this.payments = payments;
        this.events = events;
    }

    public static final class MandateLinksBuilder {
        private Link self;
        private Link nextUrl;
        private PostLink nextUrlPost;
        private Link payments;
        private Link events;

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

        public MandateLinksBuilder withEvents(URI eventsLink) {
            this.events = new Link(eventsLink.toString(), GET);
            return this;
        }

        public MandateLinks build() {
            return new MandateLinks(self, nextUrl, nextUrlPost, payments, events);
        }
    }
}
