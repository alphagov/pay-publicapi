package uk.gov.pay.api.resources;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.entity.StringEntity;
import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.model.ChargeFromResponse;
import uk.gov.pay.api.model.CreatePaymentRequest;
import uk.gov.pay.api.model.TokenPaymentType;
import uk.gov.pay.api.utils.JsonStringBuilder;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.List;

import static java.lang.String.format;
import static javax.ws.rs.client.Entity.json;
import static javax.ws.rs.core.HttpHeaders.ACCEPT;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static uk.gov.pay.api.model.TokenPaymentType.DIRECT_DEBIT;

public class ConnectorClient extends ApiClient {

    private static final String API_VERSION_PATH = "/v1";

    private static final String CONNECTOR_ACCOUNT_RESOURCE = API_VERSION_PATH + "/api/accounts/%s";
    private static final String CONNECTOR_CHARGES_RESOURCE = CONNECTOR_ACCOUNT_RESOURCE + "/charges";
    private static final String CONNECTOR_CHARGE_RESOURCE = CONNECTOR_CHARGES_RESOURCE + "/%s";
    private static final String CONNECTOR_CHARGE_EVENTS_RESOURCE = CONNECTOR_CHARGES_RESOURCE + "/%s" + "/events";
    private static final String CONNECTOR_ACCOUNT_CHARGE_CANCEL_RESOURCE = CONNECTOR_CHARGE_RESOURCE + "/cancel";

    private static final String REFERENCE_KEY = "reference";
    private static final String DESCRIPTION_KEY = "description";
    private static final String AMOUNT_KEY = "amount";
    private static final String SERVICE_RETURN_URL = "return_url";

    private final String connectorUrl;
    private final String connectorDDUrl;


    ConnectorClient(HttpClient httpClient, String connectorUrl, String connectorDDUrl) {
        super(httpClient);
        this.connectorUrl = connectorUrl;
        this.connectorDDUrl = connectorDDUrl;
    }


    public Response getPayment(String paymentId, Account account) {

        ChargeFromResponse chargeFromResponse = deserialize(request(RequestBuilder
                .get()
                .setUri(getConnectorUrl(
                        account.getPaymentType(),
                        format(CONNECTOR_CHARGE_RESOURCE, account.getName(), paymentId)))
                .setHeader(ACCEPT, APPLICATION_JSON)
                .build()), ChargeFromResponse.class);

        return Response.ok(chargeFromResponse).build();
    }

    public Response getPaymentEvents(String paymentId, Account account) {

        JsonNode paymentEvents = deserialize(request(RequestBuilder
                .get()
                .setUri(getConnectorUrl(
                        account.getPaymentType(),
                        format(CONNECTOR_CHARGE_EVENTS_RESOURCE, account.getName(), paymentId)))
                .setHeader(ACCEPT, APPLICATION_JSON)
                .build()), JsonNode.class);

        return Response.ok(paymentEvents).build();
    }

    public Response getCharges(Account account, List<Pair<String, String>> queryParams) {

        RequestBuilder requestBuilder = RequestBuilder.get();

        queryParams.forEach(queryParam -> requestBuilder.addParameter(queryParam.getKey(), queryParam.getValue()));

        JsonNode charges = deserialize(request(requestBuilder
                .setUri(getConnectorUrl(
                        account.getPaymentType(),
                        format(CONNECTOR_CHARGES_RESOURCE, account.getName())))
                .setHeader(ACCEPT, APPLICATION_JSON)
                .build()), JsonNode.class);

        return Response.ok(charges).build();
    }

    public Response createPayment(CreatePaymentRequest requestPayload, Account account) {

        ChargeFromResponse chargeFromResponse;
        try {
            chargeFromResponse = deserialize(request(RequestBuilder
                    .post()
                    .setUri(getConnectorUrl(
                            account.getPaymentType(),
                            format(CONNECTOR_CHARGES_RESOURCE, account.getName())))
                    .setHeader(ACCEPT, APPLICATION_JSON)
                    .setHeader(CONTENT_TYPE, APPLICATION_JSON)
                    .setEntity(new StringEntity(buildChargeRequestPayload(requestPayload)))
                    .build()), ChargeFromResponse.class);
        } catch (UnsupportedEncodingException e) {
            return Response.serverError().entity(e.getMessage()).build();
        }
        return Response.ok(chargeFromResponse).build();
    }

    public Response cancelPayment(String paymentId, Account account) {

        try {
            HttpResponse response = request(RequestBuilder
                    .post()
                    .setUri(getConnectorUrl(
                            account.getPaymentType(),
                            format(CONNECTOR_ACCOUNT_CHARGE_CANCEL_RESOURCE, account.getName(), paymentId)))
                    .setHeader(ACCEPT, APPLICATION_JSON)
                    .setHeader(CONTENT_TYPE, APPLICATION_JSON)
                    .setEntity(new StringEntity("{}"))
                    .build());
            JsonNode entity = deserialize(response, JsonNode.class);
            return Response.status(response.getStatusLine().getStatusCode()).entity(entity).build();
        } catch (UnsupportedEncodingException e) {
            return Response.serverError().entity(e.getMessage()).build();
        }
    }


    private String getConnectorUrl(TokenPaymentType paymentType, String urlPath) {
        return getConnectorUrl(paymentType, urlPath, Collections.emptyList());
    }

    private String getConnectorUrl(TokenPaymentType paymentType, String urlPath, List<Pair<String, String>> queryParams) {
        String url = paymentType.equals(DIRECT_DEBIT)? connectorDDUrl : connectorUrl;
        UriBuilder builder = UriBuilder
                .fromPath(url)
                .path(urlPath);

        queryParams.forEach(pair -> {
            if (isNotBlank(pair.getRight())) {
                builder.queryParam(pair.getKey(), pair.getValue());
            }
        });
        return builder.toString();
    }

    private String buildChargeRequestPayload(CreatePaymentRequest requestPayload) {
        int amount = requestPayload.getAmount();
        String reference = requestPayload.getReference();
        String description = requestPayload.getDescription();
        String returnUrl = requestPayload.getReturnUrl();
        return new JsonStringBuilder()
                .add(AMOUNT_KEY, amount)
                .add(REFERENCE_KEY, reference)
                .add(DESCRIPTION_KEY, description)
                .add(SERVICE_RETURN_URL, returnUrl)
                .build();
    }
}
