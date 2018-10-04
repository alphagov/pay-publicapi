package uk.gov.pay.api.it.fixtures;

import com.google.common.collect.ImmutableMap;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import uk.gov.pay.api.utils.DateTimeUtils;
import uk.gov.pay.commons.model.SupportedLanguage;

import java.time.ZonedDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.UUID.randomUUID;

public class PaymentSearchResultBuilder {

    private static final String STATE_KEY = "state";
    private static final String CREATED_DATE_KEY = "created_date";
    private static final int DEFAULT_NUMBER_OF_RESULTS = 3;

    public static final String DEFAULT_CREATED_DATE = DateTimeUtils.toUTCDateString(ZonedDateTime.now());
    public static final String DEFAULT_CAPTURE_SUBMIT_TIME = DateTimeUtils.toUTCDateString(ZonedDateTime.now());
    public static final String DEFAULT_CAPTURED_DATE = DateTimeUtils.toLocalDateString(ZonedDateTime.now());
    public static final String DEFAULT_RETURN_URL = "http://example.com/service";
    public static final int DEFAULT_AMOUNT = 10000;
    public static final String DEFAULT_EMAIL = "alice.111@mail.fake";
    public static final String DEFAULT_PAYMENT_PROVIDER = "worldpay";
    public static final String DEFAULT_CARD_BRAND = "master-card";
    public static final String DEFAULT_CARD_BRAND_LABEL = "Mastercard";

    private static class Address {
        public String line1;
        public String line2;
        public String postcode;
        public String city;
        public String country;

        public Address() {
        }

        public Address(uk.gov.pay.api.model.generated.Address billingAddress) {
            this.line1 = billingAddress.getLine1();
            this.line2 = billingAddress.getLine2();
            this.postcode = billingAddress.getPostcode();
            this.city = billingAddress.getCity();
            this.country = billingAddress.getCountry();
        }
    }

    private static class CardDetails {
        public String last_digits_card_number;
        public String first_digits_card_number;
        public String cardholder_name;
        public String expiry_date;
        public Address billing_address;
        public String card_brand;

        public CardDetails() {
        }

        public CardDetails(uk.gov.pay.api.model.generated.CardDetails cardDetails) {
            this.last_digits_card_number = cardDetails.getLastDigitsCardNumber();
            this.first_digits_card_number = cardDetails.getFirstDigitsCardNumber();
            this.cardholder_name = cardDetails.getCardholderName();
            this.expiry_date = cardDetails.getExpiryDate();
            this.card_brand = cardDetails.getCardBrand();
            this.billing_address = new Address(cardDetails.getBillingAddress());
        }
    }

    private static class RefundSummary {
        public String status;
        public long amount_available;
        public long amount_submitted;
    }

    private static class SettlementSummary {
        public String capture_submit_time;
        public String captured_date;
    }

    private static class TestPayment {
        public TestPaymentState state;
        public String charge_id, description, reference, email, created_date;
        public int amount;
        public String gateway_transaction_id, return_url, payment_provider, card_brand;
        public String language;
        public boolean delayed_capture;
        public RefundSummary refund_summary = new RefundSummary();
        public SettlementSummary settlement_summary = new SettlementSummary();
        public CardDetails card_details = new CardDetails();
    }

    private static class TestPaymentState {
        public String status;
        public boolean finished;

        private TestPaymentState(String status, boolean finished) {
            this.status = status;
            this.finished = finished;
        }
    }

    private static class TestPaymentSuccessState extends TestPaymentState {
        private boolean success;

        private TestPaymentSuccessState(String status) {
            super(status, true);
            this.success = true;
        }
    }

    private static class TestPaymentFailureState extends TestPaymentState {
        private boolean success;
        private String message, code;

        private TestPaymentFailureState(String status, String message, String code) {
            super(status, true);
            this.success = false;
            this.message = message;
            this.code = code;
        }
    }

    private static final List<TestPaymentState> states = new LinkedList<>();

    static {
        states.add(new TestPaymentState("created", false));
        states.add(new TestPaymentState("started", false));
        states.add(new TestPaymentState("submitted", false));
        states.add(new TestPaymentSuccessState("success"));
    }

    private int noOfResults = DEFAULT_NUMBER_OF_RESULTS;

    private String reference = null;
    private String email = null;
    private String language = SupportedLanguage.ENGLISH.toString();
    private boolean delayedCapture;
    private TestPaymentState state = null;
    private String fromDate = null;
    private String toDate = null;
    private CardDetails cardDetails = new CardDetails();

    public static PaymentSearchResultBuilder aSuccessfulSearchPayment() {
        return new PaymentSearchResultBuilder();
    }


    public PaymentSearchResultBuilder withMatchingCardDetails(uk.gov.pay.api.model.generated.CardDetails cardDetails) {
        this.cardDetails = new CardDetails(cardDetails);
        return this;
    }

    public PaymentSearchResultBuilder withMatchingReference(String reference) {
        this.reference = reference;
        return this;
    }

    public PaymentSearchResultBuilder withMatchingEmail(String email) {
        this.email = email;
        return this;
    }

    public PaymentSearchResultBuilder withMatchingInProgressState(String status) {
        this.state = new TestPaymentState(status, false);
        return this;
    }

    public PaymentSearchResultBuilder withMatchingSuccessState(String status) {
        this.state = new TestPaymentSuccessState(status);
        return this;
    }

    public PaymentSearchResultBuilder withMatchingCardBrand(String cardBrand) {
        this.cardDetails.card_brand = cardBrand;
        return this;
    }

    public PaymentSearchResultBuilder withLanguage(SupportedLanguage language) {
        this.language = language.toString();
        return this;
    }

    public PaymentSearchResultBuilder withDelayedCapture(boolean delayedCapture) {
        this.delayedCapture = delayedCapture;
        return this;
    }

    public PaymentSearchResultBuilder withMatchingFailuresState(String status, String message, String code) {
        this.state = new TestPaymentFailureState(status, message, code);
        return this;
    }

    public PaymentSearchResultBuilder withCreatedDateBetween(String fromDate, String toDate) {
        this.fromDate = fromDate;
        this.toDate = toDate;
        return this;
    }

    public PaymentSearchResultBuilder withNumberOfResults(int numberOfResults) {
        this.noOfResults = numberOfResults;
        return this;
    }

    public List<TestPayment> getResults() {
        List<TestPayment> results = newArrayList();
        for (int i = 0; i < noOfResults; i++) {
            results.add(modified(defaultPaymentResultFrom(i)));
        }
        return results;
    }

    public String build() {
        List<TestPayment> results = newArrayList();
        for (int i = 0; i < noOfResults; i++) {
            results.add(modified(defaultPaymentResultFrom(i)));
        }

        String json = new GsonBuilder().create().toJson(
                ImmutableMap.of("results", results),
                new TypeToken<Map<String, List<TestPayment>>>() {
                }.getType()
        );

        return json;
    }

    private TestPayment modified(TestPayment defaultPaymentResult) {
        if (reference != null) {
            defaultPaymentResult.reference = reference;
        }
        if (state != null) {
            defaultPaymentResult.state = state;
        }
        if (fromDate != null) {
            //randomize time for something slightly more than fromDate, so that it falls in between
            ZonedDateTime updatedFromDate = DateTimeUtils.toUTCZonedDateTime(fromDate).get().plusMinutes(new Random().nextInt(15) + 1);
            defaultPaymentResult.created_date = DateTimeUtils.toUTCDateString(updatedFromDate);
        }

        if (cardDetails != null) {
            defaultPaymentResult.card_details = cardDetails;
            defaultPaymentResult.card_brand = cardDetails.card_brand;
        }

        defaultPaymentResult.delayed_capture = delayedCapture;

        return defaultPaymentResult;
    }

    private TestPayment defaultPaymentResultFrom(int i) {
        TestPayment payment = new TestPayment();
        TestPaymentState state = states.get(new Random().nextInt(states.size()));

        payment.charge_id = "" + i;
        payment.description = "description-" + i;
        payment.reference = randomUUID().toString();
        payment.email = DEFAULT_EMAIL;
        payment.state = state;
        payment.amount = DEFAULT_AMOUNT;
        payment.gateway_transaction_id = randomUUID().toString();
        payment.created_date = DEFAULT_CREATED_DATE;
        payment.return_url = DEFAULT_RETURN_URL;
        payment.payment_provider = DEFAULT_PAYMENT_PROVIDER;
        payment.language = SupportedLanguage.ENGLISH.toString();
        payment.delayed_capture = false;
        payment.card_brand = DEFAULT_CARD_BRAND_LABEL;
        payment.card_details.card_brand = DEFAULT_CARD_BRAND_LABEL;
        payment.refund_summary.status = "available";
        payment.refund_summary.amount_available = 100;
        payment.refund_summary.amount_submitted = 300;
        payment.settlement_summary.capture_submit_time = DEFAULT_CAPTURE_SUBMIT_TIME;
        payment.settlement_summary.captured_date = DEFAULT_CAPTURED_DATE;

        return payment;
    }
}
