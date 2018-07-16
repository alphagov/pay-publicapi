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
            setLinks(new Links(getMandateExternalId(), getTransactionExternalId()));
        } 
        return links;
    }

    public String getExternalId() {
        return externalId;
    }

    public String getEvent() {
        return event;
    }

    public String getMandateExternalId() {
        return mandateExternalId;
    }

    public String getTransactionExternalId() {
        return transactionExternalId;
    }

    public String getEventType() {
        return eventType;
    }

    public ZonedDateTime getEventDate() {
        return eventDate;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public void setMandateExternalId(String mandateExternalId) {
        this.mandateExternalId = mandateExternalId;
    }

    public void setTransactionExternalId(String transactionExternalId) {
        this.transactionExternalId = transactionExternalId;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public void setEventDate(ZonedDateTime eventDate) {
        this.eventDate = eventDate;
    }

    public void setLinks(Links links) {
        this.links = links;
    }


    class Links {
        
        @JsonProperty("agreement")
        private final String agreement;

        @JsonProperty("payment")
        private final String payment;
        
        Links(String agreementId, String paymentId) {
            agreement = agreementId == null ? null : "/v1/agreements/" + agreementId;
            payment = paymentId == null ? null : "/v1/payments/" + paymentId;
        }

        public String getAgreement() {
            return agreement;
        }

        public String getPayment() {
            return payment;
        }
    }
}
