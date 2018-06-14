package uk.gov.pay.api.it.pact;

import uk.gov.pay.api.model.PaymentState;
import uk.gov.pay.api.model.TokenPaymentType;
import uk.gov.pay.api.utils.DateTimeUtils;

import java.time.ZonedDateTime;

public abstract class PaymentBaseTest {
    private static final ZonedDateTime TIMESTAMP = DateTimeUtils.toUTCZonedDateTime("2016-01-01T12:00:00Z").get();
    protected static final String PAYMENTS_PATH = "/v1/payments/";
    protected static final String RETURN_URL = "https://example.com/return";
    protected static final PaymentState CREATED = new PaymentState("created", false, null, null);
    protected static final String CREATED_DATE = DateTimeUtils.toUTCDateString(TIMESTAMP);
    protected static final String EMAIL = "pact-test@example.com";
    protected static final String SANDBOX_PAYMENT_PROVIDER = "Sandbox";

    protected String paymentLocationFor(String chargeId) {
        return "http://publicapi.url" + PAYMENTS_PATH + chargeId;
    }

    protected String frontendUrlFor(TokenPaymentType paymentType) {
        return "http://frontend_" + paymentType.toString().toLowerCase() + "/charge/";
    }
}
