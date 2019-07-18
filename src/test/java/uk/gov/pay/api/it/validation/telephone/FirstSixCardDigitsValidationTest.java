package uk.gov.pay.api.it.validation.telephone;

import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.auth.oauth.OAuthCredentialAuthFilter;
import io.dropwizard.testing.junit.ResourceTestRule;
import org.apache.http.HttpHeaders;
import org.junit.ClassRule;
import org.junit.Test;
import uk.gov.pay.api.app.config.PublicApiConfig;
import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.auth.AccountAuthenticator;
import uk.gov.pay.api.exception.mapper.ViolationExceptionMapper;
import uk.gov.pay.api.resources.telephone.TelephonePaymentNotificationResource;
import uk.gov.pay.api.utils.ApiKeyGenerator;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static groovy.json.JsonOutput.toJson;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.pay.commons.testing.port.PortFactory.findFreePort;

public class FirstSixCardDigitsValidationTest {

    protected static final String API_KEY = ApiKeyGenerator.apiKeyValueOf("TEST_BEARER_TOKEN", "qwer9yuhgf");
    private static final int PUBLIC_AUTH_PORT = findFreePort();
    public static final PublicApiConfig mockPublicApiConfig = mock(PublicApiConfig.class);
    public static final Client client = ClientBuilder.newClient();
    public static ResourceTestRule resources;

    @ClassRule
    public static final WireMockClassRule publicAuthMock = new WireMockClassRule(PUBLIC_AUTH_PORT);

    @ClassRule
    public static ResourceTestRule setupResources() {
        when(mockPublicApiConfig.getPublicAuthUrl()).thenReturn("http://localhost:" + PUBLIC_AUTH_PORT + "/v1/auth/");
        publicAuthMock.stubFor(get(urlEqualTo("/v1/auth/"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBody(
                                "{\"account_id\": \"accountId\", \"token_type\": \"CARD\"}"
                        )));
        return resources = ResourceTestRule
                .builder()
                .addResource(new AuthDynamicFeature(new OAuthCredentialAuthFilter.Builder<Account>()
                        .setAuthenticator(new AccountAuthenticator(client, mockPublicApiConfig))
                        .setPrefix("Bearer")
                        .buildAuthFilter()))
                .addProvider(new AuthValueFactoryProvider.Binder<>(Account.class))
                .addProvider(ViolationExceptionMapper.class)
                .addResource(new TelephonePaymentNotificationResource())
                .build();
    }

    private Response sendPayload(String payload) {
        return resources.target("/v1/payment_notification")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + API_KEY)
                .post(Entity.json(payload));
    }

    @Test
    public void respondWith422_whenFiveDigitsProvidedOnly() {
        String payload = toJson(Map.of("amount", 100,
                "reference", "Some reference",
                "description","hi",
                "processor_id", "1PROC",
                "provider_id", "1PROV",
                "card_type", "visa",
                "card_expiry", "01/99",
                "last_four_digits", "123",
                "first_six_digits", "12345"));

        Response response = sendPayload(payload);
        assertThat(response.getStatus(), is(422));
    }

    @Test
    public void respondWith422_whenSevenDigitsProvidedOnly() {
        String payload = toJson(Map.of("amount", 100,
                "reference", "Some reference",
                "description","hi",
                "processor_id", "1PROC",
                "provider_id", "1PROV",
                "card_type", "visa",
                "card_expiry", "01/99",
                "last_four_digits", "12345",
                "first_six_digits", "1234567"));

        Response response = sendPayload(payload);
        assertThat(response.getStatus(), is(422));
    }
    
    @Test
    public void respondWith422_whenNullProvided() {
        String payload = "{" +
                "  \"amount\" : 100," +
                "  \"reference\" : \"Some reference\"," +
                "  \"description\" : \"hi\"," +
                "  \"processor_id\" : \"1PROC\"," +
                "  \"provider_id\" : \"1PROV\"," +
                "  \"card_type\" : \"visa\"," +
                "  \"card_expiry\" : \"01/99\"," +
                "  \"last_four_digits\" : \"1234\"," +
                "  \"first_six_digits\" : null" +
                "}";
        Response response = sendPayload(payload);
        assertThat(response.getStatus(), is(422));
    }
}
