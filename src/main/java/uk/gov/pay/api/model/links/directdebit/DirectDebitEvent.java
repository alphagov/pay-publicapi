package uk.gov.pay.api.model.links.directdebit;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import uk.gov.pay.api.utils.CustomDateDeserializer;
import uk.gov.pay.api.utils.CustomDateSerializer;

import java.time.ZonedDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public class DirectDebitEvent {
    
    @JsonProperty("external_id")
    private String externalId;

    @JsonProperty("event")
    private String event;

    @JsonProperty("mandate_external_id")
    private String mandateExternalId;

    @JsonProperty("transaction_external_id")
    private String transactionExternalId;

    @JsonProperty("event_type")
    private String eventType;

    @JsonProperty("event_date")
    @JsonDeserialize(using = CustomDateDeserializer.class)
    @JsonSerialize(using = CustomDateSerializer.class)
    private ZonedDateTime eventDate;

    @JsonProperty("_links")
    private Links links;
    
    public Links getLinks() {
        if (links == null) {
            links = new Links("/v1/agreements/" + mandateExternalId, "/v1/payments/" + transactionExternalId);
        } 
        return links;
    }

    private class Links {
        
        @JsonProperty("agreement")
        public final String agreement;

        @JsonProperty("payment")
        public final String payment;

        private Links(String agreement, String payment) {
            this.agreement = agreement;
            this.payment = payment;
        }
    }
}
