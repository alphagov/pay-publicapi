package uk.gov.pay.api.model.directdebit.agreement;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import uk.gov.pay.api.model.Payment;
import uk.gov.pay.api.model.PaymentState;
import uk.gov.pay.api.model.directdebit.DirectDebitPaymentLinks;

import java.net.URI;
import java.util.Objects;

import static uk.gov.pay.api.model.directdebit.DirectDebitPaymentLinks.DirectDebitPaymentLinksBuilder.aDirectDebitPaymentLinks;


@JsonInclude(value = JsonInclude.Include.NON_NULL)
@ApiModel(value = "DirectDebitPayment")
public class DirectDebitPayment extends Payment {

    @JsonProperty("_links")
    private DirectDebitPaymentLinks links;

    @Deprecated
    public DirectDebitPayment(String chargeId, long amount, PaymentState state, String description,
                              String reference, String paymentProvider, String createdDate) {
        super(chargeId, amount, state, description, reference, paymentProvider, createdDate);
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
        this.links = aDirectDebitPaymentLinks()
                .withSelf(builder.selfLink)
                .withEvents(builder.eventsLink)
                // TODO - enable mandate link when dd-connector returns mandate id
//                .withMandate(builder.mandateLink)
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

    public static final class DirectDebitPaymentBuilder {
        protected String paymentId;
        protected String paymentProvider;
        protected long amount;
        protected PaymentState state;
        protected String description;
        protected String reference;
        protected String createdDate;
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

        public DirectDebitPaymentBuilder withState(PaymentState state) {
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
