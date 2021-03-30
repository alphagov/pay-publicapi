package uk.gov.pay.api.model.directdebit;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import uk.gov.pay.api.model.links.Link;

import java.net.URI;

import static javax.ws.rs.HttpMethod.GET;

@JsonInclude(value = JsonInclude.Include.NON_NULL)
@Schema(name = "DirectDebitPaymentLinks", description = "links for payment")
public class DirectDebitPaymentLinks {
    
    @JsonProperty("self")
    private Link self;

    //Hidden because it is currently unused
    @Schema(hidden = true)
    @JsonProperty("events")
    private Link events;

    @JsonProperty("mandate")
    private Link mandate;
    
    public DirectDebitPaymentLinks(DirectDebitPaymentLinksBuilder builder) {
        this.events = builder.events;
        this.self = builder.self;
        this.mandate = builder.mandate;
    }

    public static final class DirectDebitPaymentLinksBuilder {
        private Link self;
        private Link events;
        private Link mandate;

        private DirectDebitPaymentLinksBuilder() {
        }

        public static DirectDebitPaymentLinksBuilder aDirectDebitPaymentLinks() {
            return new DirectDebitPaymentLinksBuilder();
        }

        public DirectDebitPaymentLinksBuilder withSelf(URI self) {
            this.self = new Link(self.toString(), GET);
            return this;
        }

        public DirectDebitPaymentLinksBuilder withEvents(URI events) {
            this.events = new Link(events.toString(), GET);
            return this;
        }

        public DirectDebitPaymentLinksBuilder withMandate(URI mandate) {
            this.mandate = new Link(mandate.toString(), GET);
            return this;
        }

        public DirectDebitPaymentLinks build() {
            return new DirectDebitPaymentLinks(this);
        }
    }
}
