package uk.gov.pay.api.utils.mocks;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import com.google.common.collect.ImmutableMap;
import com.google.gson.GsonBuilder;
import uk.gov.pay.api.model.PaymentState;
import uk.gov.pay.api.model.directdebit.agreement.MandateState;
import uk.gov.pay.api.model.directdebit.agreement.MandateType;
import uk.gov.pay.api.utils.JsonStringBuilder;
import uk.gov.pay.commons.model.ErrorIdentifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.HttpHeaders.LOCATION;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.eclipse.jetty.http.HttpStatus.BAD_REQUEST_400;
import static org.eclipse.jetty.http.HttpStatus.CREATED_201;
import static org.eclipse.jetty.http.HttpStatus.OK_200;
import static org.eclipse.jetty.http.HttpStatus.PRECONDITION_FAILED_412;

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
                validPostLink(nextUrlPost(), "next_url_post", "application/x-www-form-urlencoded",
                        new HashMap<String, String>() {{
                            put("chargeTokenId", chargeTokenId);
                        }}));
        whenGetCharge(gatewayAccountId, chargeId, aResponse()
                        .withStatus(OK_200)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withBody(chargeResponseBody));
    }

    public void respondOk_whenCreateAgreementRequest(String mandateId,
                                                     MandateType mandateType,
                                                     String providerId,
                                                     String serviceReference,
                                                     String returnUrl,
                                                     String createdDate,
                                                     MandateState state,
                                                     String gatewayAccountId,
                                                     String chargeTokenId) {
        setupCreateAgreement(gatewayAccountId, aResponse()
                        .withStatus(CREATED_201)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withHeader(LOCATION, format("/v1/api/accounts/%s/mandates/%s", gatewayAccountId, mandateId))
                        .withBody(buildCreateAgreementResponse(
                                mandateId,
                                mandateType,
                                providerId,
                                serviceReference,
                                returnUrl,
                                createdDate,
                                state,
                                validGetLink(mandateLocation(gatewayAccountId, mandateId), "self"),
                                validGetLink(nextUrl(chargeTokenId), "next_url"),
                                validPostLink(nextUrlPost(), "next_url_post", "application/x-www-form-urlencoded",
                                        new HashMap<String, String>() {{
                                            put("chargeTokenId", chargeTokenId);
                                        }})
                        ))
                );
    }

    public void respondOk_whenGetAgreementRequest(String mandateId,
                                                  MandateType mandateType,
                                                  String mandateReference,
                                                  String serviceReference,
                                                  String returnUrl,
                                                  MandateState state,
                                                  String gatewayAccountId,
                                                  String chargeTokenId) {
        setupGetAgreement(mandateId, gatewayAccountId, aResponse()
                        .withStatus(200)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withBody(buildGetAgreementResponse(
                                mandateId,
                                mandateType,
                                mandateReference,
                                serviceReference,
                                returnUrl,
                                state,
                                validGetLink(mandateLocation(gatewayAccountId, mandateId), "self"),
                                validGetLink(nextUrl(chargeTokenId), "next_url"),
                                validPostLink(nextUrlPost(), "next_url_post", "application/x-www-form-urlencoded",
                                        new HashMap<String, String>() {{
                                            put("chargeTokenId", chargeTokenId);
                                        }})
                        ))
                );
    }

    public void respondBadRequest_whenCreateAgreementRequest(String gatewayAccountId, String errorMsg) {
        setupCreateAgreement(gatewayAccountId, withStatusAndErrorMessage(BAD_REQUEST_400, errorMsg));
    }

    public void respondWithMandateTypeInvalid_whenCreateAgreementRequest(MandateType mandateType,
                                                                         String returnUrl,
                                                                         String gatewayAccountId,
                                                                         String errorMsg) {
        whenCreateAgreement(returnUrl, mandateType, gatewayAccountId)
                .respond(withErrorResponse(PRECONDITION_FAILED_412, errorMsg, ErrorIdentifier.INVALID_MANDATE_TYPE));
    }

    private String buildCreateAgreementResponse(String mandateId,
                                                MandateType mandateType,
                                                String mandateReference,
                                                String serviceReference,
                                                String returnUrl,
                                                String createdDate,
                                                MandateState state,
                                                ImmutableMap<?, ?>... links) {
        return new JsonStringBuilder()
                .add("mandate_id", mandateId)
                .add("mandate_type", mandateType)
                .add("mandate_reference", mandateReference)
                .add("service_reference", serviceReference)
                .add("return_url", returnUrl)
                .add("created_date", createdDate)
                .add("state", state)
                .add("links", asList(links))
                .build();
    }

    private String buildGetAgreementResponse(String mandateId,
                                             MandateType mandateType,
                                             String mandateReference,
                                             String serviceReference,
                                             String returnUrl,
                                             MandateState state,
                                             ImmutableMap<?, ?>... links) {
        return new JsonStringBuilder()
                .add("mandate_id", mandateId)
                .add("mandate_type", mandateType)
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

    private void setupCreateAgreement(String gatewayAccountId, ResponseDefinitionBuilder response) {
        wireMockClassRule.stubFor(post(urlPathEqualTo(format("/v1/api/accounts/%s/mandates", gatewayAccountId)))
                .withHeader(CONTENT_TYPE, matching(APPLICATION_JSON)).willReturn(response));
    }
    
    private void setupGetAgreement(String mandateId, String gatewayAccountId, ResponseDefinitionBuilder response) {
        wireMockClassRule.stubFor(get(urlPathEqualTo(format("/v1/api/accounts/%s/mandates/%s", gatewayAccountId, mandateId)))
                .willReturn(response));
    }

    private String createAgreementPayload(String returnUrl, MandateType mandateType) {
        return new JsonStringBuilder()
                .add("return_url", returnUrl)
                .add("agreement_type", mandateType)
                .build();
    }
    
    private ResponseDefinitionBuilder withErrorResponse(int statusCode, String errorMsg) {
        return withStatusAndErrorMessage(statusCode, errorMsg, ErrorIdentifier.GENERIC);
    }
    
    private ResponseDefinitionBuilder withStatusAndErrorMessage(int statusCode, String errorMsg, ErrorIdentifier errorIdentifier) {
        return aResponse()
                .withStatus(statusCode)
                .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                .withBody(new GsonBuilder().create().toJson(Map.of(
                        "message", List.of(errorMsg),
                        "error_identifier", errorIdentifier.toString()
                )));
    }
}
