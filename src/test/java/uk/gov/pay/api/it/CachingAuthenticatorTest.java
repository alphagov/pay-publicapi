package uk.gov.pay.api.it;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import uk.gov.pay.api.app.config.PublicApiConfig;
import uk.gov.pay.api.model.ChargeFromResponse;
import uk.gov.pay.api.model.PaymentState;
import uk.gov.pay.api.utils.ApiKeyGenerator;
import uk.gov.pay.api.utils.JsonStringBuilder;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Map;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;
import static io.dropwizard.testing.ConfigOverride.config;
import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static java.lang.String.format;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CachingAuthenticatorTest {
    
    String publicAuthUrl = "http://public-auth";
    String accountId = "123";

    Client client = TestPublicApiModule.client;
    WebTarget webTarget = mock(WebTarget.class);
    Invocation.Builder builder = mock(Invocation.Builder.class);
    Response response = mock(Response.class);
    
    @Rule
    public DropwizardAppRule<PublicApiConfig> app = new DropwizardAppRule<>(
            TestPublicApi.class, 
            resourceFilePath("config/test-config.yaml"), 
            config("publicAuthUrl", publicAuthUrl));

    @Before
    public void setup() {
        when(webTarget.request()).thenReturn(builder);
        when(response.getStatus()).thenReturn(200);
        when(builder.get()).thenReturn(response);
    }
    
    @Test
    public void testAuthenticationRequestsAreCached() throws Exception {
        setUpMockForPublicAuth();
        setUpMockForConnector();

        makeRequest();

        Thread.sleep(1000); //pause for 1 second as there's a rate limit of 1 request per second

        makeRequest();

        verify(client, times(1)).target(publicAuthUrl);
    }

    private void makeRequest() {
        given().port(app.getLocalPort())
                .header(AUTHORIZATION, "Bearer " + ApiKeyGenerator.apiKeyValueOf("TEST_BEARER_TOKEN", "qwer9yuhgf"))
                .get("/v1/payments/paymentId")
                .then()
                .statusCode(200)
                .contentType(JSON);
    }

    private void setUpMockForConnector() throws IOException {
        when(response.readEntity(ChargeFromResponse.class)).thenReturn(new ObjectMapper().readValue(connectorResponse(), ChargeFromResponse.class));
        when(client.target(format("http://connector_card.url/v1/api/accounts/%s/charges/paymentId", accountId))).thenReturn(webTarget);
    }

    private void setUpMockForPublicAuth() {
        when(builder.header(anyString(), anyString())).thenReturn(builder);
        when(builder.accept(anyString())).thenReturn(builder);
        Map<String, String> entity = ImmutableMap.of("account_id", accountId, "token_type", "CARD");
        when(response.getStatus()).thenReturn(200);
        when(response.readEntity(JsonNode.class)).thenReturn(new ObjectMapper().convertValue(entity, JsonNode.class));
        when(client.target(publicAuthUrl)).thenReturn(webTarget);
    }
    
    private String connectorResponse() {
        JsonStringBuilder jsonStringBuilder = new JsonStringBuilder()
                .add("charge_id", "chargeId")
                .add("amount", 100)
                .add("reference", "ref 12")
                .add("state", new PaymentState("created", false, null, null))
                .add("email", "test@example.com")
                .add("description", "description")
                .add("return_url", "http://example.com")
                .add("payment_provider", "sandbox")
                .add("card_brand", "VISA")
                .add("created_date", "2018-07-25T13:12:00");
        return jsonStringBuilder.build();
    }
}
