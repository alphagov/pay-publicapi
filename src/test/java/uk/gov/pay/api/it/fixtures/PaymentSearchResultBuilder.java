package uk.gov.pay.api.it.fixtures;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import uk.gov.pay.api.utils.DateTimeUtils;

import java.time.ZonedDateTime;
import java.util.*;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.UUID.randomUUID;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class PaymentSearchResultBuilder {

    private static final String STATE_KEY = "state";
    private static final String STATUS_KEY = "status";
    private static final String CREATED_DATE_KEY = "created_date";
    private static final int DEFAULT_NUMBER_OF_RESULTS = 3;

    public static final String DEFAULT_CREATED_DATE = DateTimeUtils.toUTCDateString(ZonedDateTime.now());
    public static final String DEFAULT_RETURN_URL = "http://example.com/service";
    public static final int DEFAULT_AMOUNT = 10000;
    public static final String DEFAULT_PAYMENT_PROVIDER = "worldpay";

    private static class TestPayment {
        public TestPaymentState state;
        public String status;
        public String charge_id, description, reference, created_date;
        public int amount;
        public String gateway_transaction_id, return_url, payment_provider;
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
        states.add(new TestPaymentSuccessState("confirmed"));
    };

    private int noOfResults = DEFAULT_NUMBER_OF_RESULTS;

    private String reference = null;
    private TestPaymentState state = null;
    private String fromDate = null;
    private String toDate = null;

    public static PaymentSearchResultBuilder aSuccessfulSearchResponse() {
        return new PaymentSearchResultBuilder();
    }


    public PaymentSearchResultBuilder withMatchingReference(String reference) {
        this.reference = reference;
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

    public PaymentSearchResultBuilder withMatchingFailuresState(String status, String message, String code) {
        this.state = new TestPaymentFailureState(status, message, code);
        return this;
    }

    public PaymentSearchResultBuilder withCreatedDateBetween(String fromDate, String toDate) {
        this.fromDate = fromDate;
        this.toDate = toDate;
        return this;
    }

    public PaymentSearchResultBuilder numberOfResults(int numberOfResults) {
        this.noOfResults = numberOfResults;
        return this;
    }

    public String build() {
        List<TestPayment> results = newArrayList();
        for (int i = 0; i < noOfResults; i++) {
            results.add(modified(defaultPaymentResultFrom(i)));
        }

        String json = new GsonBuilder().create().toJson(
                ImmutableMap.of("results", results),
                new TypeToken<Map<String, List<TestPayment>>>() {}.getType()
        );

        return json;
    }

    private TestPayment modified(TestPayment defaultPaymentResult) {
        if (reference != null) {
            defaultPaymentResult.reference = reference;
        }
        if (state != null) {
            defaultPaymentResult.state = state;
            defaultPaymentResult.status = defaultPaymentResult.state.status; // backwards compat
        }
        if (fromDate != null) {
            //randomize time for something slightly more than fromDate, so that it falls in between
            ZonedDateTime updatedFromDate = DateTimeUtils.toUTCZonedDateTime(fromDate).get().plusMinutes(new Random().nextInt(15) + 1);
            defaultPaymentResult.created_date = DateTimeUtils.toUTCDateString(updatedFromDate);
        }

        return defaultPaymentResult;
    }

    private TestPayment defaultPaymentResultFrom(int i) {
        TestPayment payment = new TestPayment();
        TestPaymentState state = states.get(new Random().nextInt(states.size()));

        payment.charge_id = "" + i;
        payment.description = "description-" + i;
        payment.reference =  randomUUID().toString();
        payment.state = state;
        payment.status = payment.state.status;
        payment.amount = DEFAULT_AMOUNT;
        payment.gateway_transaction_id = randomUUID().toString();
        payment.created_date = DEFAULT_CREATED_DATE;
        payment.return_url = DEFAULT_RETURN_URL;
        payment.payment_provider = DEFAULT_PAYMENT_PROVIDER;

        return payment;
    }
}
