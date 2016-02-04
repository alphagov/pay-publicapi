package uk.gov.pay.api.it.fixtures;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import uk.gov.pay.api.utils.DateTimeUtils;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.UUID.randomUUID;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class PaymentSearchResultBuilder {
    private static final String REFERENCE_KEY = "reference";
    private static final String STATUS_KEY = "status";
    private static final String CREATED_DATE_KEY = "created_date";


    String[] statuses = {"CREATED", "IN PROGRESS", "AUTHORIZED", "SUCCEEDED"};
    private int noOfResults = 3;
    private String reference;
    private String status;
    private String fromDate;
    private String toDate;

    public static PaymentSearchResultBuilder aSuccessfulSearchResponse() {
        return new PaymentSearchResultBuilder();
    }


    public PaymentSearchResultBuilder withMatchingReference(String reference) {
        this.reference = reference;
        return this;
    }

    public PaymentSearchResultBuilder withMatchingStatus(String status) {
        this.status = status;
        return this;
    }

    public PaymentSearchResultBuilder withCreatedDateBetween(String fromDate, String toDate) {
        this.fromDate = fromDate;
        this.toDate = toDate;
        return this;
    }

    public String build() {
        List<Map<String, Object>> results = newArrayList();
        for (int i = 0; i < noOfResults; i++) {
            results.add(modified(defaultPaymentResultFrom(i)));
        }
        return new Gson().toJson(ImmutableMap.of("results", results), new TypeToken<Map<String, List<Map<String, Object>>>>() {
        }.getType());
    }

    private Map<String, Object> modified(Map<String, Object> defaultPaymentResult) {
        if (isNotBlank(reference)) {
            defaultPaymentResult.put(REFERENCE_KEY, reference);
        }
        if (isNotBlank(status)) {
            defaultPaymentResult.put(STATUS_KEY, status);
        }
        if (isNotBlank(fromDate)) {
            //randomize time for something slightly more than fromDate, so that it falls in between
            ZonedDateTime updatedFromDate = DateTimeUtils.toUTCZonedDateTime(fromDate).get().plusMinutes(new Random().nextInt(15) + 1);
            defaultPaymentResult.put(CREATED_DATE_KEY, DateTimeUtils.toUTCDateString(updatedFromDate));
        }
        return defaultPaymentResult;
    }

    private Map<String, Object> defaultPaymentResultFrom(int i) {
        Map<String, Object> result = new HashMap<String, Object>() {{
            put("charge_id", i);
            put("description", "description-" + i);
            put(REFERENCE_KEY, randomUUID().toString());
            put(STATUS_KEY, statuses[new Random().nextInt(statuses.length)]);
            put("amount", new Random().nextInt(10000));
            put("gateway_transaction_id", randomUUID().toString());
            put(CREATED_DATE_KEY, DateTimeUtils.toUTCDateString(ZonedDateTime.now()));
        }};
        return result;
    }
}