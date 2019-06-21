package uk.gov.pay.api.utils.mocks;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import com.google.common.collect.ImmutableMap;
import com.google.gson.GsonBuilder;
import uk.gov.pay.api.model.PaymentState;
import uk.gov.pay.api.model.directdebit.DirectDebitConnectorCreatePaymentResponse;
import uk.gov.pay.api.model.directdebit.mandates.MandateConnectorRequest;
import uk.gov.pay.api.model.directdebit.mandates.MandateState;
import uk.gov.pay.api.utils.JsonStringBuilder;
import uk.gov.pay.commons.model.ErrorIdentifier;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.HttpHeaders.LOCATION;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.eclipse.jetty.http.HttpStatus.BAD_REQUEST_400;
import static org.eclipse.jetty.http.HttpStatus.CREATED_201;
import static org.eclipse.jetty.http.HttpStatus.FORBIDDEN_403;
import static org.eclipse.jetty.http.HttpStatus.OK_200;
import static uk.gov.pay.commons.model.ErrorIdentifier.GENERIC;
import static uk.gov.pay.commons.model.ErrorIdentifier.GO_CARDLESS_ACCOUNT_NOT_LINKED;

public class ConnectorDDMockClient extends BaseConnectorMockClient {

    public ConnectorDDMockClient(WireMockClassRule wireMockClassRule) {
        super(wireMockClassRule);
    }

    public void respondWithChargeFound(
            long amount, String gatewayAccountId, String chargeId, PaymentState state, String returnUrl,
            String description, String reference, String email, String paymentProvider, String createdDate,
            String chargeTokenId) {
        String chargeResponseBody = buildPaymentRequestResponse(amount, chargeId, state, returnUrl,
                description, reference, email, paymentProvider, createdDate,
                validGetLink(chargeLocation(gatewayAccountId, chargeId), "self"),
                validGetLink(nextUrl(chargeId), "next_url"),
                validPostLink(nextUrlPost(), "next_url_post", "application/x-www-form-urlencoded", Map.of("chargeTokenId", chargeTokenId)));
        whenGetCharge(gatewayAccountId, chargeId, aResponse()
                .withStatus(OK_200)
                .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                .withBody(chargeResponseBody));
    }

    public void respondWithPaymentCreated(DirectDebitConnectorCreatePaymentResponse response,
                                          String gatewayAccountId) throws JsonProcessingException {
        var body = new ObjectMapper().writeValueAsString(response);
        var responseEnclosed = new ResponseDefinitionBuilder()
                .withStatus(201)
                .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                .withBody(body);
        wireMockClassRule
                .stubFor(post(urlPathEqualTo(format("/v1/api/accounts/%s/charges/collect", gatewayAccountId)))
                        .willReturn(responseEnclosed));
    }

    public void respondOk_whenCreateMandateRequest(CreateMandateRequestParams params) {
        setupCreateMandate(params.getGatewayAccountId(), aResponse()
                .withStatus(CREATED_201)
                .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                .withHeader(LOCATION, format("/v1/api/accounts/%s/mandates/%s", params.getGatewayAccountId(), params.getMandateId()))
                .withBody(buildCreateMandateResponse(
                        params.getMandateId(),
                        params.getProviderId(),
                        params.getServiceReference(),
                        params.getReturnUrl(),
                        params.getCreatedDate(),
                        params.getState(),
                        params.getDescription(),
                        validGetLink(mandateLocation(params.getGatewayAccountId(), params.getMandateId()), "self"),
                        validGetLink(nextUrl(params.getChargeTokenId()), "next_url"),
                        validPostLink(nextUrlPost(), "next_url_post", "application/x-www-form-urlencoded", Map.of("chargeTokenId", params.getChargeTokenId())
                        )))
        );
    }

    public void respondOk_whenGetAgreementRequest(DDConnectorResponseToGetMandateParams params) {
        setupGetAgreement(params.getMandateId(), params.getGatewayAccountId(), aResponse()
                .withStatus(200)
                .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                .withBody(buildGetAgreementResponse(
                        params.getMandateId(),
                        params.getMandateReference(),
                        params.getServiceReference(),
                        params.getReturnUrl(),
                        params.getState(),
                        validGetLink(mandateLocation(params.getGatewayAccountId(), params.getMandateId()), "self"),
                        validGetLink(nextUrl(params.getChargeTokenId()), "next_url"),
                        validPostLink(nextUrlPost(), "next_url_post", "application/x-www-form-urlencoded", Map.of("chargeTokenId", params.getChargeTokenId())
                        )))
        );
    }

    public void respondBadRequest_whenCreateAgreementRequest(String gatewayAccountId, String errorMsg) {
        setupCreateMandate(gatewayAccountId, withStatusAndErrorMessage(BAD_REQUEST_400, errorMsg, GENERIC));
    }

    public void respondWithGCAccountNotLinked_whenCreateMandateRequest(String gatewayAccountId, String errorMsg) {
        setupCreateMandate(gatewayAccountId, withStatusAndErrorMessage(FORBIDDEN_403, errorMsg, GO_CARDLESS_ACCOUNT_NOT_LINKED));
    }


    private String buildCreateMandateResponse(String mandateId,
                                              String mandateReference,
                                              String serviceReference,
                                              String returnUrl,
                                              String createdDate,
                                              MandateState state,
                                              String description,
                                              ImmutableMap<?, ?>... links) {
        JsonStringBuilder builder = new JsonStringBuilder()
                .add("mandate_id", mandateId)
                .add("mandate_reference", mandateReference)
                .add("service_reference", serviceReference)
                .add("return_url", returnUrl)
                .add("payment_provider", "gocardless")
                .add("created_date", createdDate)
                .add("state", state)
                .add("links", asList(links));

        Optional.ofNullable(description).ifPresent(x -> builder.add("description", description));
        
        return builder.build();
    }

    private String buildGetAgreementResponse(String mandateId,
                                             String mandateReference,
                                             String serviceReference,
                                             String returnUrl,
                                             MandateState state,
                                             ImmutableMap<?, ?>... links) {
        return new JsonStringBuilder()
                .add("mandate_id", mandateId)
                .add("mandate_reference", mandateReference)
                .add("return_url", returnUrl)
                .add("service_reference", serviceReference)
                .add("state", state)
                .add("links", asList(links))
                .build();
    }

    private String buildPaymentRequestResponse(long amount, String chargeId, PaymentState state, String returnUrl, String description,
                                               String reference, String email, String paymentProvider,
                                               String createdDate, ImmutableMap<?, ?>... links) {
        JsonStringBuilder jsonStringBuilder = new JsonStringBuilder()
                .add("charge_id", chargeId)
                .add("amount", amount)
                .add("reference", reference)
                .add("email", email)
                .add("description", description)
                .add("state", state)
                .add("return_url", returnUrl)
                .add("payment_provider", paymentProvider)
                .add("created_date", createdDate)
                .add("links", asList(links));

        return jsonStringBuilder.build();
    }

    @Override
    String nextUrlPost() {
        return "http://frontend_direct_debit/secure/";
    }

    private void setupCreateMandate(String gatewayAccountId, ResponseDefinitionBuilder response) {
        wireMockClassRule.stubFor(post(urlPathEqualTo(format("/v1/api/accounts/%s/mandates", gatewayAccountId)))
                .withHeader(CONTENT_TYPE, matching(APPLICATION_JSON)).willReturn(response));
    }

    private void setupGetAgreement(String mandateId, String gatewayAccountId, ResponseDefinitionBuilder response) {
        wireMockClassRule.stubFor(get(urlPathEqualTo(format("/v1/api/accounts/%s/mandates/%s", gatewayAccountId, mandateId)))
                .willReturn(response));
    }

    private ResponseDefinitionBuilder withStatusAndErrorMessage(int statusCode, String errorMsg, ErrorIdentifier errorIdentifier) {
        return aResponse()
                .withStatus(statusCode)
                .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                .withBody(new GsonBuilder().create().toJson(Map.of(
                        "message", List.of(errorMsg),
                        "error_identifier", errorIdentifier.toString())));
    }

    public void verifyCreateMandateConnectorRequest(MandateConnectorRequest mandateConnectorRequest, String gatewayAccountId) throws JsonProcessingException {
        wireMockClassRule.verify(1,
                postRequestedFor(urlEqualTo(format("/v1/api/accounts/%s/mandates", gatewayAccountId)))
                        .withRequestBody(equalToJson(objectMapper.writeValueAsString(mandateConnectorRequest), true, true)));
    }
}
