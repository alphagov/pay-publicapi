package uk.gov.pay.api.resources.telephone;

import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.auth.oauth.OAuthCredentialAuthFilter;
import io.dropwizard.testing.junit.ResourceTestRule;
import org.apache.http.HttpHeaders;
import org.junit.ClassRule;
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

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.pay.commons.testing.port.PortFactory.findFreePort;

public class ValidationResourceTest {

    protected static final String API_KEY = ApiKeyGenerator.apiKeyValueOf("TEST_BEARER_TOKEN", "qwer9yuhgf");
    private static final int PUBLIC_AUTH_PORT = findFreePort();
    private static final PublicApiConfig mockPublicApiConfig = mock(PublicApiConfig.class);
    private static final Client client = ClientBuilder.newClient();
    private static ResourceTestRule resources;

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

    protected Response sendPayload(String payload) {
        return resources.target("/v1/payment_notification")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + API_KEY)
                .post(Entity.json(payload));
    }
}
