package uk.gov.pay.api.model.directdebit.mandates;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import uk.gov.pay.api.model.DirectDebitPaymentState;
import uk.gov.pay.api.model.Payment;
import uk.gov.pay.api.model.PaymentState;
import uk.gov.pay.api.model.directdebit.DirectDebitConnectorPaymentResponse;
import uk.gov.pay.api.model.directdebit.DirectDebitPaymentLinks;
import uk.gov.pay.api.service.PublicApiUriGenerator;

import java.net.URI;
import java.util.Objects;

import static uk.gov.pay.api.model.directdebit.DirectDebitPaymentLinks.DirectDebitPaymentLinksBuilder.aDirectDebitPaymentLinks;
import static uk.gov.pay.api.model.directdebit.mandates.DirectDebitPayment.DirectDebitPaymentBuilder.aDirectDebitPayment;


@JsonInclude(value = JsonInclude.Include.NON_NULL)
@ApiModel(value = "DirectDebitPayment")
public class DirectDebitPayment extends Payment {

    @JsonProperty("mandate_id")
    private String mandateId;
    
    @JsonProperty("provider_id")
    private String providerId;
    
    @JsonProperty("_links")
    private DirectDebitPaymentLinks links;

    @JsonProperty("state")
    private DirectDebitPaymentState state;

    @Deprecated
    public DirectDebitPayment(String chargeId, long amount, PaymentState state, String description,
                              String reference, String paymentProvider, String createdDate) {
        super(chargeId, amount, description, reference, paymentProvider, createdDate);
    }

    public String getMandateId() {
        return mandateId;
    }

    public String getProviderId() {
        return providerId;
    }

    public DirectDebitPaymentState getState() {
        return state;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DirectDebitPayment that = (DirectDebitPayment) o;
        return Objects.equals(links, that.links);
    }

    @Override
    public int hashCode() {
        return Objects.hash(links);
    }

    public DirectDebitPayment(DirectDebitPaymentBuilder builder) {
        this.paymentId = builder.paymentId;
        this.amount = builder.amount;
        this.state = builder.state;
        this.description = builder.description;
        this.reference = builder.reference;
        this.paymentProvider = builder.paymentProvider;
        this.createdDate = builder.createdDate;
        this.mandateId = builder.mandateId;
        this.providerId = builder.providerId;
        this.links = aDirectDebitPaymentLinks()
                .withSelf(builder.selfLink)
                .withEvents(builder.eventsLink)
                .withMandate(builder.mandateLink)
                .build();
    }

    @Override
    public String toString() {
        // Some services put PII in the description, so don’t include it in the stringification
        return "Direct Debit Payment{" +
                "paymentId='" + super.paymentId + '\'' +
                ", paymentProvider='" + paymentProvider + '\'' +
                ", amount=" + amount +
                ", state='" + state + '\'' +
                ", reference='" + reference + '\'' +
                ", createdDate='" + createdDate + '\'' +
                '}';
    }

    public static DirectDebitPayment from(DirectDebitConnectorPaymentResponse paymentResponse, 
                                          PublicApiUriGenerator generator) {
        return aDirectDebitPayment()
                .withAmount(paymentResponse.getAmount())
                .withCreatedDate(paymentResponse.getCreatedDate())
                .withDescription(paymentResponse.getDescription())
                .withPaymentId(paymentResponse.getPaymentExternalId())
                .withMandateId(paymentResponse.getMandateId())
                .withPaymentProvider(paymentResponse.getPaymentProvider())
                .withReference(paymentResponse.getReference())
                .withState(paymentResponse.getState())
                .withProviderId(paymentResponse.getProviderId())
                .withSelfLink(generator.getDirectDebitPaymentURI(paymentResponse.getPaymentExternalId()))
                .withMandateLink(generator.getMandateURI(paymentResponse.getMandateId()))
                .withEventsLink(generator.getDirectDebitPaymentEventsURI(paymentResponse.getPaymentExternalId()))
                .build();
    }

    public static final class DirectDebitPaymentBuilder {
        protected String paymentId;
        protected String paymentProvider;
        protected long amount;
        protected DirectDebitPaymentState state;
        protected String description;
        protected String reference;
        protected String createdDate;
        protected String mandateId;
        protected String providerId;
        protected URI selfLink;
        protected URI eventsLink;
        protected URI mandateLink;

        private DirectDebitPaymentBuilder() {
        }

        public static DirectDebitPaymentBuilder aDirectDebitPayment() {
            return new DirectDebitPaymentBuilder();
        }

        public DirectDebitPaymentBuilder withPaymentId(String paymentId) {
            this.paymentId = paymentId;
            return this;
        }

        public DirectDebitPaymentBuilder withPaymentProvider(String paymentProvider) {
            this.paymentProvider = paymentProvider;
            return this;
        }

        public DirectDebitPaymentBuilder withAmount(long amount) {
            this.amount = amount;
            return this;
        }

        public DirectDebitPaymentBuilder withState(DirectDebitPaymentState state) {
            this.state = state;
            return this;
        }

        public DirectDebitPaymentBuilder withDescription(String description) {
            this.description = description;
            return this;
        }


        public DirectDebitPaymentBuilder withReference(String reference) {
            this.reference = reference;
            return this;
        }

        public DirectDebitPaymentBuilder withCreatedDate(String createdDate) {
            this.createdDate = createdDate;
            return this;
        }
        
        public DirectDebitPaymentBuilder withMandateId(String mandateId) {
            this.mandateId = mandateId;
            return this;
        }
        
        public DirectDebitPaymentBuilder withProviderId(String providerId) {
            this.providerId = providerId;
            return this;
        }

        public DirectDebitPaymentBuilder withSelfLink(URI selfLink) {
            this.selfLink = selfLink;
            return this;
        }

        public DirectDebitPaymentBuilder withEventsLink(URI eventsLink) {
            this.eventsLink = eventsLink;
            return this;
        }

        public DirectDebitPaymentBuilder withMandateLink(URI mandateLink) {
            this.mandateLink = mandateLink;
            return this;
        }

        public DirectDebitPayment build() {
            return new DirectDebitPayment(this);
        }
    }
}
