package uk.gov.pay.api.service;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class PaymentSearchParams {

    public static final String REFERENCE_KEY = "reference";
    public static final String EMAIL_KEY = "email";
    public static final String STATE_KEY = "state";
    public static final String CARD_BRAND_KEY = "card_brand";
    public static final String FIRST_DIGITS_CARD_NUMBER_KEY = "first_digits_card_number";
    public static final String LAST_DIGITS_CARD_NUMBER_KEY = "last_digits_card_number";
    public static final String CARDHOLDER_NAME_KEY = "cardholder_name";
    public static final String FROM_DATE_KEY = "from_date";
    public static final String TO_DATE_KEY = "to_date";
    public static final String PAGE = "page";
    public static final String DISPLAY_SIZE = "display_size";

    private String reference;
    private String email;
    private String state;
    private String cardBrand;
    private String fromDate;
    private String toDate;
    private String pageNumber;
    private String displaySize;
    private String agreementId;
    private String cardHolderName;
    private String firstDigitsCardNumber;
    private String lastDigitsCardNumber;

    public PaymentSearchParams(Builder builder) {
        this.reference = builder.reference;
        this.email = builder.email;
        this.state = builder.state;
        this.cardBrand = builder.cardBrand;
        this.fromDate = builder.fromDate;
        this.toDate = builder.toDate;
        this.pageNumber = builder.pageNumber;
        this.displaySize = builder.displaySize;
        this.agreementId = builder.agreementId;
        this.cardHolderName = builder.cardHolderName;
        this.firstDigitsCardNumber = builder.firstDigitsCardNumber;
        this.lastDigitsCardNumber = builder.lastDigitsCardNumber;
    }

    public Map<String, String> getParamsAsMap() {
        Map<String, String> params = new LinkedHashMap<>();
        params.put(REFERENCE_KEY, reference);
        params.put(EMAIL_KEY, email);
        params.put(STATE_KEY, state);
        params.put(CARD_BRAND_KEY, cardBrand);
        params.put(CARDHOLDER_NAME_KEY, cardHolderName);
        params.put(FIRST_DIGITS_CARD_NUMBER_KEY, firstDigitsCardNumber);
        params.put(LAST_DIGITS_CARD_NUMBER_KEY, lastDigitsCardNumber);
        params.put(FROM_DATE_KEY, fromDate);
        params.put(TO_DATE_KEY, toDate);
        params.put(PAGE, pageNumber);
        params.put(DISPLAY_SIZE, displaySize);

        return params;
    }

    public String getState() {
        return state;
    }

    public String getReference() {
        return reference;
    }

    public String getEmail() {
        return email;
    }

    public String getCardBrand() {
        return cardBrand;
    }

    public String getFromDate() {
        return fromDate;
    }

    public String getToDate() {
        return toDate;
    }

    public String getPageNumber() {
        return pageNumber;
    }

    public String getDisplaySize() {
        return displaySize;
    }

    public String getAgreementId() {
        return agreementId;
    }

    public String getFirstDigitsCardNumber() {
        return firstDigitsCardNumber;
    }

    public String getLastDigitsCardNumber() {
        return lastDigitsCardNumber;
    }

    public static class Builder {
        private String reference;
        private String email;
        private String state;
        private String cardBrand;
        private String fromDate;
        private String toDate;
        private String pageNumber;
        private String displaySize;
        private String agreementId;
        private String cardHolderName;
        private String firstDigitsCardNumber;
        private String lastDigitsCardNumber;

        public Builder() {
        }

        public PaymentSearchParams build() {
            return new PaymentSearchParams(this);
        }

        public Builder withReference(String reference) {
            this.reference = reference;
            return this;
        }

        public Builder withEmail(String email) {
            this.email = email;
            return this;
        }

        public Builder withState(String state) {
            this.state = state;
            return this;
        }

        public Builder withCardBrand(String cardBrand) {
            this.cardBrand = cardBrand;

            if (isNotBlank(cardBrand)) {
                this.cardBrand = cardBrand.toLowerCase();
            }

            return this;
        }

        public Builder withFromDate(String fromDate) {
            this.fromDate = fromDate;
            return this;
        }

        public Builder withToDate(String toDate) {
            this.toDate = toDate;
            return this;
        }

        public Builder withPageNumber(String pageNumber) {
            this.pageNumber = pageNumber;
            return this;
        }

        public Builder withDisplaySize(String displaySize) {
            this.displaySize = displaySize;
            return this;
        }

        public Builder withAgreementId(String agreementId) {
            this.agreementId = agreementId;
            return this;
        }

        public Builder withCardHolderName(String cardHolderName) {
            this.cardHolderName = cardHolderName;
            return this;
        }

        public Builder withFirstDigitsCardNumber(String firstDigitsCardNumber) {
            this.firstDigitsCardNumber = firstDigitsCardNumber;
            return this;
        }

        public Builder withLastDigitsCardNumber(String lastDigitsCardNumber) {
            this.lastDigitsCardNumber = lastDigitsCardNumber;
            return this;
        }
    }
}
