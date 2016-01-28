package uk.gov.pay.api.it;


import com.google.common.collect.ImmutableMap;
import com.jayway.restassured.response.ValidatableResponse;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.pay.api.it.fixtures.PaymentSearchResultBuilder.aSuccessfulSearchResponse;

public class PaymentSearchITest extends PaymentResourceITestBase {

    protected static final String TEST_REFERENCE = "test_reference";
    protected static final String TEST_STATUS = "SUCCEEDED";
    protected static final String TEST_FROM_DATE = "2016-01-28 00:00:00";
    protected static final String TEST_TO_DATE = "2016-01-28 12:00:00";
    private static final String SEARCH_PATH = "/v1/payments";

    @Test
    public void searchPayments_filterByFullReference() throws Exception {
        publicAuthMock.mapBearerTokenToAccountId(BEARER_TOKEN, GATEWAY_ACCOUNT_ID);
        connectorMock.respondOk_whenSearchCharges(GATEWAY_ACCOUNT_ID, TEST_REFERENCE, null, null, null,
                aSuccessfulSearchResponse()
                        .withMatchingReference(TEST_REFERENCE)
                        .build()
        );

        ValidatableResponse response = searchPayments(BEARER_TOKEN, ImmutableMap.of("reference", TEST_REFERENCE))
                .statusCode(200)
                .contentType(JSON)
                .body("results.size()", equalTo(3));

        List<Map<String, Object>> results = response.extract().body().jsonPath().getList("results");
        assertThat(results.get(0).get("reference"), is(TEST_REFERENCE));
        assertThat(results.get(1).get("reference"), is(TEST_REFERENCE));
        assertThat(results.get(2).get("reference"), is(TEST_REFERENCE));

    }

    private ValidatableResponse searchPayments(String bearerToken, ImmutableMap<String, String> queryParams) {
        return given().port(app.getLocalPort())
                .accept(JSON)
                .contentType(JSON)
                .header(AUTHORIZATION, "Bearer " + bearerToken)
                .queryParameters(queryParams)
                .get(SEARCH_PATH)
                .then();
    }

}
