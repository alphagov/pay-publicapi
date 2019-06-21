package uk.gov.pay.api.utils;

public class Urls {
    
    public static String directDebitFrontendSecureUrl() {
        return "http://frontend_direct_debit/secure/";
    }

    public static String paymentLocationFor(String publicApiBaseUrl, String chargeId) {
        return publicApiBaseUrl + "v1/payments/" + chargeId;
    }

    public static String mandateLocationFor(String mandateId) {
        return "http://publicapi.url/v1/directdebit/mandates/" + mandateId;
    }

    public static String directDebitPaymentLocationFor(String publicApiBaseUrl, String chargeId) {
        return publicApiBaseUrl + "v1/directdebit/payments/" + chargeId;
    }
}
