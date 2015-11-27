package uk.gov.pay.api.resources;

import com.fasterxml.jackson.databind.JsonNode;
import io.dropwizard.auth.Auth;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.api.model.LinksResponse;
import uk.gov.pay.api.utils.TolerantReaderUtil;

import javax.ws.rs.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import static java.lang.String.format;
import static java.lang.String.valueOf;
import static javax.ws.rs.client.Entity.json;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.lang3.StringEscapeUtils.escapeHtml4;
import static org.apache.http.HttpStatus.SC_OK;
import static uk.gov.pay.api.model.CreatePaymentResponse.createPaymentResponse;
import static uk.gov.pay.api.utils.JsonStringBuilder.jsonStringBuilder;
import static uk.gov.pay.api.utils.ResponseUtil.*;
import static uk.gov.pay.api.utils.TolerantReaderUtil.tolerantGet;

@Path("/")
public class PaymentsResource {
    private static final String PAYMENT_KEY = "paymentId";
    private static final String REFERENCE_KEY = "reference";
    private static final String DESCRIPTION_KEY = "description";
    private static final String AMOUNT_KEY = "amount";
    private static final String GATEWAY_ACCOUNT_KEY = "gateway_account_id";
    private static final String SERVICE_RETURN_URL = "return_url";
    private static final String CHARGE_KEY = "charge_id";

    private static final String[] REQUIRED_FIELDS = {DESCRIPTION_KEY, AMOUNT_KEY, SERVICE_RETURN_URL};

    private static final String PAYMENTS_PATH = "/v1/payments";
    private static final String PAYMENTS_ID_PLACEHOLDER = "{" + PAYMENT_KEY + "}";
    private static final String PAYMENT_BY_ID = "/v1/payments/" + PAYMENTS_ID_PLACEHOLDER;

    private static final String CANCEL_PATH_SUFFIX = "/cancel";
    private static final String CANCEL_PAYMENT_PATH = "/v1/payments/" + PAYMENTS_ID_PLACEHOLDER + CANCEL_PATH_SUFFIX;
    public static final String CONNECTOR_CHARGES_RESOURCE = "/v1/api/charges";
    public static final String CONNECTOR_ACCOUNT_CHARGE_CANCEL_RESOURCE = "/v1/api/accounts/%s/charges/%s/cancel";

    private final Logger logger = LoggerFactory.getLogger(PaymentsResource.class);
    private final Client client;
    private final String connectorUrl;

    public PaymentsResource(Client client, String connectorUrl) {
        this.client = client;
        this.connectorUrl = connectorUrl;
    }

    @GET
    @Path(PAYMENT_BY_ID)
    @Produces(APPLICATION_JSON)
    public Response getCharge(@Auth String accountId, @PathParam(PAYMENT_KEY) String chargeId, @Context UriInfo uriInfo) {
        logger.info("received get payment request: [ {} ]", chargeId);

        Response connectorResponse = client.target(connectorUrl + CONNECTOR_CHARGES_RESOURCE + "/" + chargeId)
                .request()
                .get();

        return responseFrom(uriInfo, connectorResponse, SC_OK,
                (locationUrl, data) -> Response.ok(data),
                data -> notFoundResponse(logger, data));
    }

    @POST
    @Path(PAYMENTS_PATH)
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    public Response createNewPayment(@Auth String accountId, JsonNode requestPayload, @Context UriInfo uriInfo) throws IOException {
        logger.info("received create payment request: [ {} ]", requestPayload);

        Optional<List<String>> missingFields = checkMissingFields(requestPayload);
        if (missingFields.isPresent()) {
            return fieldsMissingResponse(logger, missingFields.get());
        }

        Response connectorResponse = client.target(connectorUrl + CONNECTOR_CHARGES_RESOURCE)
                .request()
                .post(buildChargeRequestPayload(accountId, requestPayload));

        return responseFrom(uriInfo, connectorResponse, HttpStatus.SC_CREATED,
                (locationUrl, data) -> Response.created(locationUrl).entity(data),
                data -> badRequestResponse(logger, data));
    }

    @POST
    @Path(CANCEL_PAYMENT_PATH)
    @Produces(APPLICATION_JSON)
    public Response cancelCharge(@Auth String accountId, @PathParam(PAYMENT_KEY) String chargeId) {
        logger.info("received cancel payment request: [{}]", chargeId);

        Response connectorResponse = client.target(connectorUrl + format(CONNECTOR_ACCOUNT_CHARGE_CANCEL_RESOURCE, accountId, chargeId))
                .request()
                .post(Entity.json("{}"));

        if (connectorResponse.getStatus() == HttpStatus.SC_NO_CONTENT) {
            return Response.noContent().build();
        }

        return badRequestResponse(logger, "Cancellation of charge failed.");
    }

    private Response responseFrom(UriInfo uriInfo, Response connectorResponse, int okStatus,
                                  BiFunction<URI, Object, ResponseBuilder> okResponse,
                                  Function<JsonNode, Response> errorResponse) {
        if (!connectorResponse.hasEntity()) {
            return badRequestResponse(logger, "Connector response contains no payload!");
        }

        JsonNode payload = connectorResponse.readEntity(JsonNode.class);
        if (connectorResponse.getStatus() == okStatus) {
            URI documentLocation = uriInfo.getBaseUriBuilder()
                    .path(PAYMENT_BY_ID)
                    .build(payload.get(CHARGE_KEY).asText());

            Optional<JsonNode> nextLinkMaybe = getNextLink(payload);

            LinksResponse response =
                    createPaymentResponse(payload)
                            .addSelfLink(documentLocation);

            if (nextLinkMaybe.isPresent()) {
                JsonNode nextLink = nextLinkMaybe.get();
                response.addLink(
                        nextLink.get("rel").asText(),
                        nextLink.get("method").asText(),
                        nextLink.get("href").asText()
                );
            }

            logger.info("payment returned: [ {} ]", response);

            return okResponse.apply(documentLocation, response).build();
        }

        return errorResponse.apply(payload);
    }

    private Optional<JsonNode> getNextLink(JsonNode payload) {
        for (Iterator<JsonNode> it = payload.get("links").elements(); it.hasNext(); ) {
            JsonNode node = it.next();
            if ("next_url".equals(node.get("rel").asText())) {
                return Optional.of(node);
            }
        }

        return Optional.empty();
    }


    private Optional<List<String>> checkMissingFields(JsonNode node) {
        List<String> missing = new ArrayList<>();
        for (String field : REQUIRED_FIELDS) {
            if (!node.hasNonNull(field)) {
                missing.add(field);
            }
        }
        return missing.isEmpty()
                ? Optional.<List<String>>empty()
                : Optional.of(missing);
    }

    private Entity buildChargeRequestPayload(String accountId, JsonNode requestPayload) {
        long amount = requestPayload.get(AMOUNT_KEY).asLong();
        String reference = tolerantGet(requestPayload, REFERENCE_KEY);
        String description = requestPayload.get(DESCRIPTION_KEY).asText();
        String returnUrl = requestPayload.get(SERVICE_RETURN_URL).asText();

        return json(jsonStringBuilder()
                .add(AMOUNT_KEY, amount)
                .add(REFERENCE_KEY, escapeHtml4(reference))
                .add(DESCRIPTION_KEY, escapeHtml4(description))
                .add(GATEWAY_ACCOUNT_KEY, accountId)
                .add(SERVICE_RETURN_URL, returnUrl)
                .build());
    }

}
