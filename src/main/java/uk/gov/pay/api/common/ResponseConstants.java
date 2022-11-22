package uk.gov.pay.api.common;

public final class ResponseConstants {

    public static final String RESPONSE_200_DESCRIPTION = "OK - your request was successful";
    public static final String RESPONSE_201_DESCRIPTION = "Created: OK - you created a payment";
    public static final String RESPONSE_202_DESCRIPTION = "Accepted: Refund request accepted. The refund will reach the paying user soon.";
    public static final String RESPONSE_400_DESCRIPTION = "Bad request: Your request is invalid.<br><br>" +
            "Check the `code` and `description` response attributes to find out why the request failed.";
    public static final String RESPONSE_401_DESCRIPTION = "Unauthorised: Your API key is missing or invalid.<br><br>" +
            "<a href=\"https://docs.payments.service.gov.uk/api_reference/#authentication\">" +
            "Read more about authenticating GOV.UK Pay API requests</a>.";
    public static final String RESPONSE_404_DESCRIPTION = "Not found : The payment or refund you tried to access does not exist.<br><br>Check the `{PAYMENT_ID}` or `{REFUND_ID}`.";
    public static final String RESPONSE_409_DESCRIPTION = "Conflict : The payment you tried to access has already been captured or cancelled.";
    public static final String RESPONSE_412_DESCRIPTION = "Precondition failed : The `refund_amount_available` value you sent does not match the amount available to refund.<br><br>" +
            "[Read more about refunding payments](https://docs.payments.service.gov.uk/refunding_payments/#creating-a-refund).";
    public static final String RESPONSE_422_DESCRIPTION = "One of the values you sent is formatted incorrectly." +
            "This could be an invalid value, or a value that exceeds a character limit.<br><br>" +
            "Check the `field`, `code`, and `description` attributes in the response for more information.";
    public static final String RESPONSE_429_DESCRIPTION = "Too many requests : You've made too many requests using your API key.<br><br>" +
            "[Read more about rate limits](https://docs.payments.service.gov.uk/api_reference#rate-limits).";
    public static final String RESPONSE_500_DESCRIPTION = "Internal server error : There's something wrong with GOV.UK Pay. <br><br>" +
            "If there are no issues on [our status page](https://payments.statuspage.io), " +
            "you can [contact us with your error code](https://docs.payments.service.gov.uk/support_contact_and_more_information/) and we'll investigate.";
    
}
