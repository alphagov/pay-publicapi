package uk.gov.pay.api.common;

public final class ResponseConstants {
    
    public static final String RESPONSE_200_DESCRIPTION = "OK - your request was successful.";
    public static final String RESPONSE_201_DESCRIPTION = "Created";
    public static final String RESPONSE_400_DESCRIPTION = "Bad request";
    public static final String RESPONSE_401_DESCRIPTION = "Your API key is missing or invalid. " +
            "Read more about [authenticating GOV.UK Pay API requests](https://docs.payments.service.gov.uk/api_reference/#authentication)";
    public static final String RESPONSE_404_DESCRIPTION = "Not found";
    public static final String RESPONSE_409_DESCRIPTION = "Conflict";
    public static final String RESPONSE_422_DESCRIPTION = "Unprocessable entity";
    public static final String RESPONSE_429_DESCRIPTION = "Too many requests";
    public static final String RESPONSE_500_DESCRIPTION = "Downstream system error";
}
