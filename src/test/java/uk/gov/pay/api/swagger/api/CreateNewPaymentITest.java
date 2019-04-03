package uk.gov.pay.api.swagger.api;

import com.atlassian.oai.validator.model.Response;
import com.atlassian.oai.validator.restassured.OpenApiValidationFilter;
import com.atlassian.oai.validator.restassured.RestAssuredResponse;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.restassured.response.ValidatableResponse;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import uk.gov.pay.api.it.PaymentResourceITestBase;
import uk.gov.pay.api.model.Address;
import uk.gov.pay.api.model.CardDetails;
import uk.gov.pay.api.model.PaymentState;
import uk.gov.pay.api.model.RefundSummary;
import uk.gov.pay.api.utils.DateTimeUtils;
import uk.gov.pay.api.utils.JsonStringBuilder;
import uk.gov.pay.commons.model.SupportedLanguage;

import java.time.ZonedDateTime;
import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder.responseDefinition;
import static com.github.tomakehurst.wiremock.client.WireMock.any;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.pay.api.model.TokenPaymentType.CARD;
import static uk.gov.pay.commons.model.ApiResponseDateTimeFormatter.ISO_INSTANT_MILLISECOND_PRECISION;

public class CreateNewPaymentITest extends PaymentResourceITestBase {

    private final OpenApiValidationFilter validationFilter = new OpenApiValidationFilter("swagger/swagger.json");

    private static final ZonedDateTime TIMESTAMP = DateTimeUtils.toUTCZonedDateTime("2016-01-01T12:00:00Z").get();
    private static final int AMOUNT = 9999999;
    private static final String CHARGE_ID = "ch_ab2341da231434l";
    private static final String CHARGE_TOKEN_ID = "token_1234567asdf";
    private static final PaymentState CREATED = new PaymentState("created", false, null, null);
    private static final RefundSummary REFUND_SUMMARY = new RefundSummary("pending", 100L, 50L);
    private static final String PAYMENT_PROVIDER = "Sandbox";
    private static final String CARD_BRAND_LABEL = "Mastercard";
    private static final String RETURN_URL = "https://somewhere.gov.uk/rainbow/1";
    private static final String REFERENCE = "Some reference <script> alert('This is a ?{simple} XSS attack.')</script>";
    private static final String EMAIL = "alice.111@mail.fake";
    private static final String DESCRIPTION = "Some description <script> alert('This is a ?{simple} XSS attack.')</script>";
    private static final String CREATED_DATE = ISO_INSTANT_MILLISECOND_PRECISION.format(TIMESTAMP);
    private static final Address BILLING_ADDRESS = new Address("line1", "line2", "NR2 5 6EG", "city", "UK");
    private static final CardDetails CARD_DETAILS = new CardDetails("1234", "123456", "Mr. Payment", "12/19", BILLING_ADDRESS, CARD_BRAND_LABEL);
    private static final String SUCCESS_PAYLOAD = paymentPayload(AMOUNT, RETURN_URL, DESCRIPTION, REFERENCE, EMAIL);
    private static final String GATEWAY_TRANSACTION_ID = "gateway-tx-123456";

    @Rule
    public WireMockRule wireMock = new WireMockRule();

    @Before
    public void setup() {
        super.setup();
    }

    @Test
    public void createCardPayment() {
        publicAuthMock.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID, CARD);

        connectorMock.respondOk_whenCreateCharge(AMOUNT, GATEWAY_ACCOUNT_ID, CHARGE_ID, CHARGE_TOKEN_ID,
                CREATED, RETURN_URL, DESCRIPTION, REFERENCE, null, PAYMENT_PROVIDER, CREATED_DATE, SupportedLanguage.ENGLISH, true, REFUND_SUMMARY,
                null, null, GATEWAY_TRANSACTION_ID);

        postPaymentResponse(API_KEY, SUCCESS_PAYLOAD)
                .statusCode(201);

    }


    @Test
    public void responseHeadersAreMappedCorrectly() {

        wireMock.stubFor(any(anyUrl()).willReturn(responseDefinition().withHeader("custom-header", "0").withStatus(200)));

        final Response response = RestAssuredResponse.of(given()
                .port(wireMock.port())
                .when()
                .get("/path")
                .then()
                .assertThat()
                .statusCode(200)
                .extract()
                .response());

        assertThat("custom-header must be preserved", response.getHeaderValue("custom-header"), is(Optional.of("0")));
        assertThat("ContentType must be empty", response.getContentType(), is(Optional.empty()));

    }

    private static String paymentPayload(long amount, String returnUrl, String description, String reference, String email) {
        return new JsonStringBuilder()
                .add("amount", amount)
                .add("reference", reference)
//                .add("email", email)
                .add("description", description)
                .add("return_url", returnUrl)
                .build();
    }

    private ValidatableResponse postPaymentResponse(String bearerToken, String payload) {
        return given().port(app.getLocalPort())
                .filter(validationFilter)
                .body(payload)
                .accept(JSON)
                .contentType(JSON)
                .header(AUTHORIZATION, "Bearer " + bearerToken)
                .post(PAYMENTS_PATH)
                .then();
    }

}
