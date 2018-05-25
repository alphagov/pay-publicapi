package uk.gov.pay.api.utils.mocks;

import com.google.common.collect.ImmutableMap;
import org.mockserver.client.server.ForwardChainExpectation;
import uk.gov.pay.api.model.PaymentState;
import uk.gov.pay.api.model.directdebit.AgreementStatus;
import uk.gov.pay.api.model.directdebit.AgreementType;
import uk.gov.pay.api.utils.JsonStringBuilder;

import java.util.HashMap;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static javax.ws.rs.HttpMethod.POST;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.HttpHeaders.LOCATION;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.eclipse.jetty.http.HttpStatus.CREATED_201;
import static org.eclipse.jetty.http.HttpStatus.OK_200;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

public class ConnectorDDMockClient extends BaseConnectorMockClient {

    public ConnectorDDMockClient(int port, String baseUrl) {
        super(port, baseUrl);
    }

    public void respondOk_whenCreatePaymentRequest(int amount, String gatewayAccountId, String chargeId, String chargeTokenId, PaymentState state, String returnUrl,
                                                   String description, String reference, String email, String paymentProvider, String createdDate) {
        whenCreateCharge(amount, gatewayAccountId, returnUrl, description, reference)
                .respond(response()
                        .withStatusCode(CREATED_201)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withHeader(LOCATION, chargeLocation(gatewayAccountId, chargeId))
                        .withBody(buildPaymentRequestResponse(
                                amount,
                                chargeId,
                                state,
                                returnUrl,
                                description,
                                reference,
                                email,
                                paymentProvider,
                                createdDate,
                                validGetLink(chargeLocation(gatewayAccountId, chargeId), "self"),
                                validGetLink(nextUrl(chargeTokenId), "next_url"), validPostLink(nextUrlPost(), "next_url_post", "application/x-www-form-urlencoded",
                                        new HashMap<String, String>() {{
                                            put("chargeTokenId", chargeTokenId);
                                        }}))));
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

    public void respondOk_whenCreateAgreementRequest(String name, String email, AgreementType agreementType, String agreementId,
                                                     AgreementStatus status, String gatewayAccountId) {
        whenCreateAgreement(name, email, agreementType, gatewayAccountId)
                .respond(response()
                        .withStatusCode(CREATED_201)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withHeader(LOCATION, format("%s/v1/api/accounts/%s/agreements/%s", baseUrl, gatewayAccountId, agreementId))
                        .withBody(buildCreateAgreementResponse(name, email, agreementType, agreementId, status)
                        )
                );
    }

    private String buildCreateAgreementResponse(String name, String email, AgreementType agreementType, String agreementId,
                                                AgreementStatus status) {
        return new JsonStringBuilder()
                .add("name", name)
                .add("email", email)
                .add("type", agreementType)
                .add("agreement_id", agreementId)
                .add("status", status)
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
        return "http://frontend_direct_debit/charge/";
    }


    private ForwardChainExpectation whenCreateAgreement(String name, String email, AgreementType agreementType, String gatewayAccountId) {
        return mockClient.when(request()
                .withMethod(POST)
                .withPath(format("/v1/api/accounts/%s/agreements", gatewayAccountId))
                .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                .withBody(
                        createAgreementPayload(name, email, agreementType)
                )
        );
    }

    private String createAgreementPayload(String name, String email, AgreementType agreementType) {
        return new JsonStringBuilder()
                .add("name", name)
                .add("email", email)
                .add("type", agreementType)
                .build();
    }
}
