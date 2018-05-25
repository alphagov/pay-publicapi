package uk.gov.pay.api.it.directdebit;

import com.jayway.jsonassert.JsonAssert;
import org.junit.Test;
import uk.gov.pay.api.it.PaymentResourceITestBase;
import uk.gov.pay.api.model.PaymentState;
import uk.gov.pay.api.utils.DateTimeUtils;

import javax.ws.rs.core.HttpHeaders;
import java.time.ZonedDateTime;

import static com.jayway.restassured.http.ContentType.JSON;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static uk.gov.pay.api.model.TokenPaymentType.DIRECT_DEBIT;

public class DirectDebitPaymentsResourceITest extends PaymentResourceITestBase {

    private static final ZonedDateTime TIMESTAMP = DateTimeUtils.toUTCZonedDateTime("2016-01-01T12:00:00Z").get();
    private static final int AMOUNT = 9999999;
    private static final String CHARGE_ID = "ch_ab2341da231434l";
    private static final String CHARGE_TOKEN_ID = "token_1234567asdf";
    private static final PaymentState CREATED = new PaymentState("created", false, null, null);
    private static final String PAYMENT_PROVIDER = "Sandbox";
    private static final String RETURN_URL = "https://somewhere.gov.uk/rainbow/1";
    private static final String REFERENCE = "Some reference <script> alert('This is a ?{simple} XSS attack.')</script>";
    private static final String EMAIL = "test@example.com";
    private static final String DESCRIPTION = "Some description <script> alert('This is a ?{simple} XSS attack.')</script>";
    private static final String CREATED_DATE = DateTimeUtils.toUTCDateString(TIMESTAMP);
    private static final String SUCCESS_PAYLOAD = paymentPayload(AMOUNT, RETURN_URL, DESCRIPTION, REFERENCE, EMAIL);
    
    @Test
    public void createDirectDebitPayment() {

        publicAuthMock.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID, DIRECT_DEBIT);

        connectorDDMock.respondOk_whenCreatePaymentRequest(AMOUNT, GATEWAY_ACCOUNT_ID, CHARGE_ID, CHARGE_TOKEN_ID,
                CREATED, RETURN_URL, DESCRIPTION, REFERENCE, EMAIL, PAYMENT_PROVIDER, CREATED_DATE);

        String responseBody = postPaymentResponse(API_KEY, SUCCESS_PAYLOAD)
                .statusCode(201)
                .contentType(JSON)
                .header(HttpHeaders.LOCATION, is(paymentLocationFor(CHARGE_ID)))
                .body("payment_id", is(CHARGE_ID))
                .body("amount", is(9999999))
                .body("reference", is(REFERENCE))
                .body("description", is(DESCRIPTION))
                .body("state.status", is(CREATED.getStatus()))
                .body("return_url", is(RETURN_URL))
                .body("email", is(EMAIL))
                .body("payment_provider", is(PAYMENT_PROVIDER))
                .body("created_date", is(CREATED_DATE))
                .body("_links.self.href", is(paymentLocationFor(CHARGE_ID)))
                .body("_links.self.method", is("GET"))
                .body("_links.next_url.href", is(frontendUrlFor(DIRECT_DEBIT) + CHARGE_TOKEN_ID))
                .body("_links.next_url.method", is("GET"))
                .body("_links.next_url_post.href", is(frontendUrlFor(DIRECT_DEBIT)))
                .body("_links.next_url_post.method", is("POST"))
                .body("_links.next_url_post.type", is("application/x-www-form-urlencoded"))
                .body("_links.next_url_post.params.chargeTokenId", is(CHARGE_TOKEN_ID))
                .body("card_brand", is(nullValue()))
                .body("refund_summary", is(nullValue()))
                .body("_links.cancel", is(nullValue()))
                .body("_links.events", is(nullValue()))
                .body("_links.refunds", is(nullValue()))
                .extract().body().asString();

        JsonAssert.with(responseBody)
                .assertNotDefined("_links.self.type")
                .assertNotDefined("_links.self.params")
                .assertNotDefined("_links.next_url.type")
                .assertNotDefined("_links.next_url.params");

        connectorDDMock.verifyCreateChargeConnectorRequest(AMOUNT, GATEWAY_ACCOUNT_ID, RETURN_URL, DESCRIPTION, REFERENCE);
    }

    @Test
    public void getPayment_ReturnsDirectDebitPayment() {
        publicAuthMock.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID, DIRECT_DEBIT);

        connectorDDMock.respondWithChargeFound(AMOUNT, GATEWAY_ACCOUNT_ID, CHARGE_ID, CREATED, RETURN_URL,
                DESCRIPTION, REFERENCE, EMAIL, PAYMENT_PROVIDER, CREATED_DATE, CHARGE_TOKEN_ID);

        getPaymentResponse(API_KEY, CHARGE_ID)
                .statusCode(200)
                .contentType(JSON)
                .body("payment_id", is(CHARGE_ID))
                .body("reference", is(REFERENCE))
                .body("email", is(EMAIL))
                .body("description", is(DESCRIPTION))
                .body("amount", is(AMOUNT))
                .body("state.status", is(CREATED.getStatus()))
                .body("return_url", is(RETURN_URL))
                .body("payment_provider", is(PAYMENT_PROVIDER))
                .body("created_date", is(CREATED_DATE))
                .body("_links.self.href", is(paymentLocationFor(CHARGE_ID)))
                .body("_links.self.method", is("GET"))
                .body("_links.next_url.href", is(frontendUrlFor(DIRECT_DEBIT) + CHARGE_ID))
                .body("_links.next_url.method", is("GET"))
                .body("_links.next_url_post.href", is(frontendUrlFor(DIRECT_DEBIT)))
                .body("_links.next_url_post.method", is("POST"))
                .body("_links.next_url_post.type", is("application/x-www-form-urlencoded"))
                .body("_links.next_url_post.params.chargeTokenId", is(CHARGE_TOKEN_ID))
                .body("_links.cancel", is(nullValue()))
                .body("_links.events", is(nullValue()))
                .body("_links.refunds", is(nullValue()));
    }
}
