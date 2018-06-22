package uk.gov.pay.api.utils.mocks;

import com.google.common.collect.ImmutableMap;
import org.mockserver.client.server.ForwardChainExpectation;
import org.mockserver.model.HttpResponse;
import uk.gov.pay.api.model.PaymentState;
import uk.gov.pay.api.model.directdebit.agreement.MandateState;
import uk.gov.pay.api.model.directdebit.agreement.MandateType;
import uk.gov.pay.api.utils.JsonStringBuilder;

import java.util.HashMap;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static javax.ws.rs.HttpMethod.POST;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.HttpHeaders.LOCATION;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.HttpMethod.GET;
import static org.eclipse.jetty.http.HttpStatus.BAD_REQUEST_400;
import static org.eclipse.jetty.http.HttpStatus.CREATED_201;
import static org.eclipse.jetty.http.HttpStatus.OK_200;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static uk.gov.pay.api.utils.JsonStringBuilder.jsonString;

public class ConnectorDDMockClient extends BaseConnectorMockClient {

    public ConnectorDDMockClient(int port, String baseUrl) {
        super(port, baseUrl);
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
        whenGetCharge(gatewayAccountId, chargeId)
                .respond(response()
                        .withStatusCode(OK_200)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withBody(chargeResponseBody));
    }

    public void respondOk_whenCreateAgreementRequest(String mandateId, MandateType mandateType,
                                                     String returnUrl, String createdDate,
                                                     MandateState state, String gatewayAccountId,
                                                     String chargeTokenId) {
        whenCreateAgreement(returnUrl, mandateType, gatewayAccountId)
                .respond(response()
                        .withStatusCode(CREATED_201)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withHeader(LOCATION, format("%s/v1/api/accounts/%s/mandates/%s", baseUrl, gatewayAccountId, mandateId))
                        .withBody(buildCreateAgreementResponse(
                                mandateId,
                                mandateType,
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
    public void respondOk_whenGetAgreementRequest(String mandateId, MandateType mandateType, String returnUrl, MandateState state, String gatewayAccountId, String chargeTokenId) {
        whenGetAgreement(mandateId, gatewayAccountId)
                .respond(response()
                        .withStatusCode(200)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withBody(buildGetAgreementResponse(
                                mandateId,
                                mandateType,
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
    public void respondBadRequest_whenCreateAgreementRequest(MandateType mandateType,
                                                             String returnUrl,
                                                             String gatewayAccountId,
                                                             String errorMsg) {
        whenCreateAgreement(returnUrl, mandateType, gatewayAccountId)
                .respond(withStatusAndErrorMessage(BAD_REQUEST_400, errorMsg));
    }

    private String buildCreateAgreementResponse(String mandateId, MandateType mandateType,
                                                String returnUrl, String createdDate,
                                                MandateState state, ImmutableMap<?, ?>... links) {
        return new JsonStringBuilder()
                .add("mandate_id", mandateId)
                .add("mandate_type", mandateType)
                .add("return_url", returnUrl)
                .add("created_date", createdDate)
                .add("state", state)
                .add("links", asList(links))
                .build();
    }

    private String buildGetAgreementResponse(String mandateId, MandateType mandateType,
            String returnUrl,
            MandateState state, ImmutableMap<?, ?>... links) {
        return new JsonStringBuilder()
                .add("mandate_id", mandateId)
                .add("mandate_type", mandateType)
                .add("return_url", returnUrl)
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

    private ForwardChainExpectation whenCreateAgreement(String returnUrl, MandateType mandateType, String gatewayAccountId) {
        return mockClient.when(request()
                .withMethod(POST)
                .withPath(format("/v1/api/accounts/%s/mandates", gatewayAccountId))
                .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                .withBody(
                        createAgreementPayload(returnUrl, mandateType)
                )
        );
    }


    private ForwardChainExpectation whenGetAgreement(String mandateId, String gatewayAccountId) {
        return mockClient.when(request()
                .withMethod(GET)
                .withPath(format("/v1/api/accounts/%s/mandates/%s", gatewayAccountId, mandateId))
        );
    }
    
    private String createAgreementPayload(String returnUrl, MandateType mandateType) {
        return new JsonStringBuilder()
                .add("return_url", returnUrl)
                .add("agreement_type", mandateType)
                .build();
    }

    private HttpResponse withStatusAndErrorMessage(int statusCode, String errorMsg) {
        return response()
                .withStatusCode(statusCode)
                .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                .withBody(jsonString("message", errorMsg));
    }
}
