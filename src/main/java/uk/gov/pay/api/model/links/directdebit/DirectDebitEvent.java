package uk.gov.pay.api.model.links.directdebit;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Builder;
import lombok.Getter;
import uk.gov.pay.api.utils.CustomDateDeserializer;
import uk.gov.pay.api.utils.CustomDateSerializer;

import java.time.ZonedDateTime;

@Builder
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
            links = new Links(mandateExternalId, transactionExternalId);
        } 
        return links;
    }
    
    @Getter
    class Links {
        
        @JsonProperty("agreement")
        private final String agreement;

        @JsonProperty("payment")
        private final String payment;
        
        Links(String agreementId, String paymentId) {
            agreement = agreementId == null ? null : "/v1/agreements/" + agreementId;
            payment = paymentId == null ? null : "/v1/payments/" + paymentId;
        }
    }
}
