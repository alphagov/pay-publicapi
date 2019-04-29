package uk.gov.pay.api.utils.mocks;

import com.google.common.collect.ImmutableMap;
import org.mockserver.client.ForwardChainExpectation;
import org.mockserver.client.MockServerClient;
import uk.gov.pay.api.utils.JsonStringBuilder;

import java.util.Map;

import static java.lang.String.format;
import static javax.ws.rs.HttpMethod.GET;
import static javax.ws.rs.HttpMethod.POST;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.JsonBody.json;
import static org.mockserver.verify.VerificationTimes.once;

public abstract class BaseConnectorMockClient {

    static String CONNECTOR_MOCK_ACCOUNTS_PATH = "/v1/api/accounts/%s";
    static String CONNECTOR_MOCK_CHARGES_PATH = CONNECTOR_MOCK_ACCOUNTS_PATH + "/charges";
    static String CONNECTOR_MOCK_CHARGE_PATH = CONNECTOR_MOCK_CHARGES_PATH + "/%s";
    static String CONNECTOR_MOCK_MANDATES_PATH = CONNECTOR_MOCK_ACCOUNTS_PATH + "/mandates";
    static String CONNECTOR_MOCK_MANDATE_PATH = CONNECTOR_MOCK_MANDATES_PATH + "/%s";

    final MockServerClient mockClient;
    final String baseUrl;

    BaseConnectorMockClient(int port, String baseUrl) {
        this.mockClient = new MockServerClient("localhost", port);
        this.baseUrl = baseUrl;
    }

    ImmutableMap<String, String> validGetLink(String href, String rel) {
        return ImmutableMap.of(
                "href", href,
                "rel", rel,
                "method", GET);
    }

    ImmutableMap<String, Object> validPostLink(String href, String rel, String type, Map<String, String> params) {
        return ImmutableMap.of(
                "href", href,
                "rel", rel,
                "type", type,
                "params", params,
                "method", POST);
    }

    String chargeLocation(String accountId, String chargeId) {
        return baseUrl + format(CONNECTOR_MOCK_CHARGE_PATH, accountId, chargeId);
    }

    public String mandateLocation(String accountId, String mandateId) {
        return baseUrl + format(CONNECTOR_MOCK_MANDATE_PATH, accountId, mandateId);
    }

    abstract String nextUrlPost();

    String nextUrl(String tokenId) {
        return nextUrlPost() + tokenId;
    }

    ForwardChainExpectation whenCreateCharge(long amount, String gatewayAccountId, String returnUrl, String description, String reference) {
        return mockClient.when(request()
                .withMethod(POST)
                .withPath(format(CONNECTOR_MOCK_CHARGES_PATH, gatewayAccountId))
                .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                .withBody(createChargePayload(amount, returnUrl, description, reference))
        );
    }

    ForwardChainExpectation whenGetCharge(String gatewayAccountId, String chargeId) {
        return mockClient.when(request()
                .withMethod(GET)
                .withPath(format(CONNECTOR_MOCK_CHARGE_PATH, gatewayAccountId, chargeId))
        );
    }

    String createChargePayload(long amount, String returnUrl, String description, String reference) {
        return new JsonStringBuilder()
                .add("amount", amount)
                .add("reference", reference)
                .add("description", description)
                .add("return_url", returnUrl)
                .build();
    }

    String createChargePayload(CreateChargeRequestParams createChargeRequestParams) {
        JsonStringBuilder payload = new JsonStringBuilder()
                .add("amount", createChargeRequestParams.getAmount())
                .add("reference", createChargeRequestParams.getReference())
                .add("description", createChargeRequestParams.getDescription())
                .add("return_url", createChargeRequestParams.getReturnUrl());

        if (!createChargeRequestParams.getMetadata().isEmpty())
            payload.add("metadata", createChargeRequestParams.getMetadata());

        return payload.build();
    }

    public void verifyCreateChargeConnectorRequest(int amount, String gatewayAccountId, String returnUrl, String description, String reference) {
        mockClient.verify(request()
                        .withMethod(POST)
                        .withPath(format(CONNECTOR_MOCK_CHARGES_PATH, gatewayAccountId))
                        .withBody(json(createChargePayload(amount, returnUrl, description, reference))),
                once()
        );
    }

    public void verifyCreateChargeConnectorRequest(String gatewayAccountId, CreateChargeRequestParams createChargeRequestParams) {
        mockClient.verify(request()
                        .withMethod(POST)
                        .withPath(format(CONNECTOR_MOCK_CHARGES_PATH, gatewayAccountId))
                        .withBody(json(createChargePayload(createChargeRequestParams))),
                once()
        );
    }
}
