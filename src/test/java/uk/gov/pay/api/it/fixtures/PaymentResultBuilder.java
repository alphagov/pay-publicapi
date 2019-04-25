package uk.gov.pay.api.it.fixtures;

import uk.gov.pay.api.utils.DateTimeUtils;
import uk.gov.pay.commons.model.SupportedLanguage;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static java.util.UUID.randomUUID;
import static uk.gov.pay.commons.model.ApiResponseDateTimeFormatter.ISO_INSTANT_MILLISECOND_PRECISION;

public abstract class PaymentResultBuilder {

    protected static final int DEFAULT_NUMBER_OF_RESULTS = 3;

    public static final String DEFAULT_CREATED_DATE = ISO_INSTANT_MILLISECOND_PRECISION.format(ZonedDateTime.now());
    public static final String DEFAULT_CAPTURE_SUBMIT_TIME = ISO_INSTANT_MILLISECOND_PRECISION.format(ZonedDateTime.now());
    public static final String DEFAULT_CAPTURED_DATE = DateTimeUtils.toLocalDateString(ZonedDateTime.now());
    public static final String DEFAULT_RETURN_URL = "http://example.com/service";
    public static final int DEFAULT_AMOUNT = 10000;
    public static final String DEFAULT_PAYMENT_PROVIDER = "worldpay";
    public static final String DEFAULT_CARD_BRAND_LABEL = "Mastercard";
    
    protected static class Address {
        public String line1;
        public String line2;
        public String postcode;
        public String city;
        public String county = null;
        public String country;

        public Address() {
        }

        public Address(uk.gov.pay.api.model.Address billingAddress) {
            this.line1 = billingAddress.getLine1();
            this.line2 = billingAddress.getLine2();
            this.postcode = billingAddress.getPostcode();
            this.city = billingAddress.getCity();
            this.country = billingAddress.getCountry();
        }
    }

    protected static class CardDetails {
        public String last_digits_card_number;
        public String first_digits_card_number;
        public String cardholder_name;
        public String expiry_date;
        public Address billing_address;
        public String card_brand;

        public CardDetails() {
        }

        public CardDetails(uk.gov.pay.api.model.CardDetails cardDetails) {
            this.last_digits_card_number = cardDetails.getLastDigitsCardNumber();
            this.first_digits_card_number = cardDetails.getFirstDigitsCardNumber();
            this.cardholder_name = cardDetails.getCardHolderName();
            this.expiry_date = cardDetails.getExpiryDate();
            this.card_brand = cardDetails.getCardBrand();
            this.billing_address = cardDetails.getBillingAddress().map(Address::new).orElse(null);
        }
    }

    protected static class RefundSummary {
        public String status;
        public long amount_available;
        public long amount_submitted;
        public String user_external_id;
        
        public RefundSummary(uk.gov.pay.api.model.RefundSummary refundSummary) {
            this.status = refundSummary.getStatus();
            this.amount_available = refundSummary.getAmountAvailable();
            this.amount_submitted = refundSummary.getAmountSubmitted();
            this.user_external_id = null;
        }
        
        public RefundSummary(String status, long amountAvailable, long amountSubmitted) {
            this.status = status;
            this.amount_available = amountAvailable;
            this.amount_submitted = amountSubmitted;
            this.user_external_id = null;
        }
    }

    protected static class SettlementSummary {
        public String capture_submit_time;
        public String captured_date;
        
        public SettlementSummary() {}
        
        public SettlementSummary(uk.gov.pay.api.model.SettlementSummary settlementSummary) {
            this.capture_submit_time = settlementSummary.getCaptureSubmitTime();
            this.captured_date = settlementSummary.getCapturedDate();
        }
        
        public SettlementSummary(String captureSubmitTime, String capturedDate) {
            this.capture_submit_time = captureSubmitTime;
            this.captured_date = capturedDate;
        }
    }

    protected static class TestPayment {
        public TestPaymentState state;
        public String charge_id, description, reference, email, created_date, gateway_transaction_id, return_url, 
                payment_provider, language;
        public long amount;
        public boolean delayed_capture;
        public RefundSummary refund_summary;
        public SettlementSummary settlement_summary;
        public CardDetails card_details = new CardDetails();
        public Long corporate_card_surcharge, total_amount;
        public List<Map<?,?>> links;
        public Map<String, ?> metadata;
    }

    protected static class TestPaymentState {
        public String status;
        public boolean finished;

        protected TestPaymentState(String status, boolean finished) {
            this.status = status;
            this.finished = finished;
        }
    }

    protected static class TestPaymentSuccessState extends TestPaymentState {
        private boolean success;

        protected TestPaymentSuccessState(String status) {
            super(status, true);
            this.success = true;
        }
    }

    protected static final List<TestPaymentState> states = new LinkedList<>();

    static {
        states.add(new TestPaymentState("created", false));
        states.add(new TestPaymentState("started", false));
        states.add(new TestPaymentState("submitted", false));
        states.add(new TestPaymentSuccessState("success"));
    }

    protected long amount = DEFAULT_AMOUNT;
    protected String reference = null;
    protected String email = null;
    protected String language = SupportedLanguage.ENGLISH.toString();
    protected Boolean delayedCapture;
    protected TestPaymentState state;
    protected String fromDate = null;
    protected String toDate = null;
    protected CardDetails cardDetails;
    protected Long corporateCardSurcharge = null;
    protected Long totalAmount = null;
    protected String chargeId = null;
    protected String description;
    protected String returnUrl;
    protected String paymentProvider;
    protected String createdDate;
    protected List<Map<?, ?>> links = new ArrayList<>();
    protected RefundSummary refundSummary;
    protected SettlementSummary settlementSummary;
    protected String gatewayTransactionId;
    protected Map<String, ?> metadata;

    public abstract String build();
    
    protected TestPayment getPayment(int i) {
        this.chargeId = chargeId == null ? "" + i : chargeId;
        this.description = "description-" + i;
        return defaultPaymentResult();
    }
    
    protected TestPayment getPayment() {
        return defaultPaymentResult();
    }

    private TestPayment defaultPaymentResult() {
        TestPayment payment = new TestPayment();

        payment.charge_id = chargeId;
        payment.description = description;
        payment.reference = reference == null ? randomUUID().toString() : reference;
        payment.email = email;
        payment.state = state == null ? states.get(new Random().nextInt(states.size())) : state;
        payment.amount = amount;
        payment.gateway_transaction_id = gatewayTransactionId == null ? randomUUID().toString() : gatewayTransactionId;
        payment.created_date = getCreatedDate();
        payment.return_url = returnUrl == null ? DEFAULT_RETURN_URL : returnUrl;
        payment.payment_provider = paymentProvider == null ? DEFAULT_PAYMENT_PROVIDER : paymentProvider;
        payment.language = SupportedLanguage.ENGLISH.toString();
        payment.delayed_capture = delayedCapture == null ? false : delayedCapture;
        payment.card_details = cardDetails == null ? new CardDetails() : cardDetails;
        payment.card_details.card_brand = DEFAULT_CARD_BRAND_LABEL;
        payment.refund_summary = refundSummary == null ? 
                new RefundSummary("available", 100, 300) : refundSummary;
        payment.settlement_summary =  settlementSummary == null ? 
                new SettlementSummary(DEFAULT_CAPTURE_SUBMIT_TIME, DEFAULT_CAPTURED_DATE) : settlementSummary;
        payment.corporate_card_surcharge = corporateCardSurcharge;
        payment.total_amount = totalAmount;
        payment.links = links ;
        payment.metadata = metadata;

        return payment;
    }

    private String getCreatedDate() {
        if (fromDate != null) {
            ZonedDateTime updatedFromDate = DateTimeUtils.toUTCZonedDateTime(fromDate).get().plusMinutes(new Random().nextInt(15) + 1);
            return ISO_INSTANT_MILLISECOND_PRECISION.format(updatedFromDate);
        }
        return createdDate == null ? DEFAULT_CREATED_DATE : createdDate;
    }
}
