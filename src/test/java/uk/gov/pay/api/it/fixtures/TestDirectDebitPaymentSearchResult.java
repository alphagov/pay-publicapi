package uk.gov.pay.api.it.fixtures;

import uk.gov.pay.api.model.DirectDebitPaymentState;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public class TestDirectDebitPaymentSearchResult {
    private Long amount;
    private String payment_id;
    private DirectDebitPaymentState state;
    private String description;
    private String reference;
    private String created_date;
    private String mandate_id;
    private String provider_id;
    private String payment_provider;

    public Long getAmount() {
        return amount;
    }

    public String getPayment_id() {
        return payment_id;
    }

    public DirectDebitPaymentState getState() {
        return state;
    }

    public String getDescription() {
        return description;
    }

    public String getReference() {
        return reference;
    }

    public String getCreated_date() {
        return created_date;
    }

    public String getMandate_id() {
        return mandate_id;
    }

    public String getProvider_id() {
        return provider_id;
    }

    public String getPayment_provider() {
        return payment_provider;
    }

    public static final class TestDirectDebitPaymentSearchResultBuilder {
        private Long amount = 100L;
        private String paymentId = "a-payment-id";
        private DirectDebitPaymentState state = new DirectDebitPaymentState("pending", false, "payment_state_details");
        private String description = "a-description";
        private String reference = "a-reference";
        private String createdDate = "2019-01-01T23:59:59Z";
        private String mandateId = "a-mandate-id";
        private String providerId = "a-provider-id";
        private String paymentProvider = "sandbox";

        private TestDirectDebitPaymentSearchResultBuilder() {
        }

        public static TestDirectDebitPaymentSearchResultBuilder aTestDirectDebitPaymentSearchResult() {
            return new TestDirectDebitPaymentSearchResultBuilder();
        }

        public TestDirectDebitPaymentSearchResultBuilder withAmount(Long amount) {
            this.amount = amount;
            return this;
        }

        public TestDirectDebitPaymentSearchResultBuilder withPaymentId(String paymentId) {
            this.paymentId = paymentId;
            return this;
        }

        public TestDirectDebitPaymentSearchResultBuilder withState(DirectDebitPaymentState state) {
            this.state = state;
            return this;
        }

        public TestDirectDebitPaymentSearchResultBuilder withDescription(String description) {
            this.description = description;
            return this;
        }

        public TestDirectDebitPaymentSearchResultBuilder withReference(String reference) {
            this.reference = reference;
            return this;
        }

        public TestDirectDebitPaymentSearchResultBuilder withCreatedDate(String createdDate) {
            this.createdDate = createdDate;
            return this;
        }

        public TestDirectDebitPaymentSearchResultBuilder withMandateId(String mandateId) {
            this.mandateId = mandateId;
            return this;
        }

        public TestDirectDebitPaymentSearchResultBuilder withProviderId(String providerId) {
            this.providerId = providerId;
            return this;
        }

        public TestDirectDebitPaymentSearchResultBuilder withPaymentProvider(String paymentProvider) {
            this.paymentProvider = paymentProvider;
            return this;
        }

        public TestDirectDebitPaymentSearchResult build() {
            TestDirectDebitPaymentSearchResult testDirectDebitPaymentSearchResult = new TestDirectDebitPaymentSearchResult();
            testDirectDebitPaymentSearchResult.state = this.state;
            testDirectDebitPaymentSearchResult.provider_id = this.providerId;
            testDirectDebitPaymentSearchResult.reference = this.reference;
            testDirectDebitPaymentSearchResult.created_date = this.createdDate;
            testDirectDebitPaymentSearchResult.mandate_id = this.mandateId;
            testDirectDebitPaymentSearchResult.payment_id = this.paymentId;
            testDirectDebitPaymentSearchResult.amount = this.amount;
            testDirectDebitPaymentSearchResult.description = this.description;
            testDirectDebitPaymentSearchResult.payment_provider = this.paymentProvider;
            return testDirectDebitPaymentSearchResult;
        }
        
        public List<TestDirectDebitPaymentSearchResult> buildMultiple(int numberOfResults) {
            List<TestDirectDebitPaymentSearchResult> results = newArrayList();
            for (int i = 0; i < numberOfResults; i++) {
                TestDirectDebitPaymentSearchResult payment = build();
                payment.payment_id = payment.payment_id + i;
                results.add(payment);
            }
            return results;
        }
        
    }
}
