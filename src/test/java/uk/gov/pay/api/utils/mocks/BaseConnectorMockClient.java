package uk.gov.pay.api.utils.mocks;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import com.google.common.collect.ImmutableMap;
import uk.gov.pay.api.utils.JsonStringBuilder;

import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static java.lang.String.format;
import static javax.ws.rs.HttpMethod.GET;
import static javax.ws.rs.HttpMethod.POST;

public abstract class BaseConnectorMockClient {

    static String CONNECTOR_MOCK_ACCOUNTS_PATH = "/v1/api/accounts/%s";
    static String CONNECTOR_MOCK_CHARGES_PATH = CONNECTOR_MOCK_ACCOUNTS_PATH + "/charges";
    static String CONNECTOR_MOCK_CHARGE_PATH = CONNECTOR_MOCK_CHARGES_PATH + "/%s";
    static String CONNECTOR_MOCK_MANDATES_PATH = CONNECTOR_MOCK_ACCOUNTS_PATH + "/mandates";
    static String CONNECTOR_MOCK_MANDATE_PATH = CONNECTOR_MOCK_MANDATES_PATH + "/%s";
    
    protected WireMockClassRule wireMockClassRule;

    public BaseConnectorMockClient(WireMockClassRule wireMockClassRule) {
        this.wireMockClassRule = wireMockClassRule;
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
        return format(CONNECTOR_MOCK_CHARGE_PATH, accountId, chargeId);
    }

    public String mandateLocation(String accountId, String mandateId) {
        return format(CONNECTOR_MOCK_MANDATE_PATH, accountId, mandateId);
    }

    abstract String nextUrlPost();

    String nextUrl(String tokenId) {
        return nextUrlPost() + tokenId;
    }

    void whenGetCharge(String gatewayAccountId, String chargeId, ResponseDefinitionBuilder response) {
        wireMockClassRule.stubFor(get(urlPathEqualTo(format(CONNECTOR_MOCK_CHARGE_PATH, gatewayAccountId, chargeId)))
                .willReturn(response));
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
        if (createChargeRequestParams.getEmail() != null) {
            payload.add("email", createChargeRequestParams.getEmail());
        }

        return payload.build();
    }

    public void verifyCreateChargeConnectorRequest(int amount, String gatewayAccountId, String returnUrl, String description, String reference) {
        wireMockClassRule.verify(1, 
                postRequestedFor(urlEqualTo(format(CONNECTOR_MOCK_CHARGES_PATH, gatewayAccountId)))
                        .withRequestBody(equalTo(createChargePayload(amount, returnUrl, description, reference))));
    }

    public void verifyCreateChargeConnectorRequest(String gatewayAccountId, CreateChargeRequestParams createChargeRequestParams) {
        wireMockClassRule.verify(1,
                postRequestedFor(urlEqualTo(format(CONNECTOR_MOCK_CHARGES_PATH, gatewayAccountId)))
                        .withRequestBody(equalToJson(createChargePayload(createChargeRequestParams))));
    }
}
