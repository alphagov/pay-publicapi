package uk.gov.pay.api.model.links.directdebit;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import uk.gov.pay.commons.api.json.ApiResponseDateTimeDeserializer;
import uk.gov.pay.commons.api.json.ApiResponseDateTimeSerializer;

import java.time.ZonedDateTime;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public class DirectDebitEvent {

    private String externalId;

    @JsonProperty("event")
    private String event;

    private String mandateExternalId;
    private String paymentExternalId;

    @JsonProperty("event_type")
    private String eventType;

    @JsonProperty("event_date")
    @JsonDeserialize(using = ApiResponseDateTimeDeserializer.class)
    @JsonSerialize(using = ApiResponseDateTimeSerializer.class)
    private ZonedDateTime eventDate;

    @JsonProperty("_links")
    private Links links;


    public Links getLinks() {
        return links;
    }

    @JsonProperty("event_id")
    public String getExternalId() {
        return externalId;
    }

    public String getEvent() {
        return event;
    }

    @JsonProperty("agreement_id")
    public String getMandateExternalId() {
        return mandateExternalId;
    }

    @JsonProperty("payment_id")
    public String getPaymentExternalId() {
        return paymentExternalId;
    }

    public String getEventType() {
        return eventType;
    }

    public ZonedDateTime getEventDate() {
        return eventDate;
    }

    @JsonProperty("external_id")
    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    @JsonProperty("mandate_external_id")
    public void setMandateExternalId(String mandateExternalId) {
        this.mandateExternalId = mandateExternalId;
    }

    @JsonProperty("transaction_external_id")
    public void setPaymentExternalId(String paymentExternalId) {
        this.paymentExternalId = paymentExternalId;
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


    public static class Links {

        @JsonProperty("agreement")
        private final String agreement;

        @JsonProperty("payment")
        private final String payment;

        public Links(String agreement, String payment) {
            this.agreement = agreement;
            this.payment = payment;
        }

        public String getAgreement() {
            return agreement;
        }

        public String getPayment() {
            return payment;
        }
    }
}
