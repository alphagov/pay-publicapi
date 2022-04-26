package uk.gov.pay.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AuthorisationAPIRequest {
    public static final String ONE_TIME_TOKEN_FIELD_NAME = "one_time_token";
    public static final String CARD_NUMBER_FIELD_NAME = "card_number";
    public static final String CVC_FIELD_NAME = "cvc";
    public static final String EXPIRY_DATE_FIELD_NAME = "expiry_date";
    public static final String CARDHOLDER_NAME_FIELD_NAME = "cardholder_name";

    public static final int CARD_NUMBER_MIN_VALUE = 12;
    public static final int CARD_NUMBER_MAX_VALUE = 19;
    public static final int CVC_MIN_VALUE = 3;
    public static final int CVC_MAX_VALUE = 4;
    public static final int EXPIRY_DATE_SIZE = 5;
    public static final int CARDHOLDER_NAME_MAX_LENGTH = 255;

    @JsonProperty(ONE_TIME_TOKEN_FIELD_NAME)
    private String oneTimeToken;

    @JsonProperty(CARD_NUMBER_FIELD_NAME)
    private String cardNumber;

    @JsonProperty(CVC_FIELD_NAME)
    private String cvc;

    @JsonProperty(EXPIRY_DATE_FIELD_NAME)
    private String expiryDate;

    @JsonProperty(CARDHOLDER_NAME_FIELD_NAME)
    private String cardholderName;

    public AuthorisationAPIRequest() {
        // for Jackson
    }

    public AuthorisationAPIRequest(AuthorisationAPIRequestBuilder builder) {
        this.oneTimeToken = builder.getOneTimeToken();
        this.cardNumber = builder.getCardNumber();
        this.cvc = builder.getCvc();
        this.expiryDate = builder.getExpiryDate();
        this.cardholderName = builder.getCardholderName();
    }

    public String getOneTimeToken() {
        return oneTimeToken;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public String getCvc() {
        return cvc;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public String getCardholderName() {
        return cardholderName;
    }
}
