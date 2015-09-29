package uk.gov.pay.api.resources;

import com.fasterxml.jackson.databind.JsonNode;
import io.dropwizard.auth.Auth;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.api.model.LinksResponse;

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
import static javax.ws.rs.client.Entity.json;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.lang3.BooleanUtils.negate;
import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;
import static uk.gov.pay.api.model.CreatePaymentResponse.createPaymentResponse;
import static uk.gov.pay.api.utils.JsonStringBuilder.jsonStringBuilder;
import static uk.gov.pay.api.utils.ResponseUtil.*;

@Path("/")
public class PaymentsResource {
    private static final String PAYMENT_KEY = "paymentId";
    private static final String AMOUNT_KEY = "amount";
    private static final String GATEWAY_ACCOUNT_KEY = "gateway_account_id";
    private static final String SERVICE_RETURN_URL = "return_url";
    private static final String CHARGE_KEY = "charge_id";

    private static final String[] REQUIRED_FIELDS = {AMOUNT_KEY, SERVICE_RETURN_URL};

    private static final String PAYMENTS_PATH = "/v1/payments";
    private static final String PAYMENTS_ID_PLACEHOLDER = "{" + PAYMENT_KEY + "}";
    private static final String PAYMENT_BY_ID = "/v1/payments/" + PAYMENTS_ID_PLACEHOLDER;

    private static final String CANCEL_PATH_SUFFIX = "/cancel";
    private static final String CANCEL_PAYMENT_PATH = "/v1/payments/" + PAYMENTS_ID_PLACEHOLDER + CANCEL_PATH_SUFFIX;

    private final Logger logger = LoggerFactory.getLogger(PaymentsResource.class);
    private final Client client;
    private final String chargeUrl;

    public PaymentsResource(Client client, String chargeUrl) {
        this.client = client;
        this.chargeUrl = chargeUrl;
    }

    @GET
    @Path(PAYMENT_BY_ID)
    @Produces(APPLICATION_JSON)
    public Response getCharge(@PathParam(PAYMENT_KEY) String chargeId, @Context UriInfo uriInfo) {
        logger.info("received get payment request: [ {} ]", chargeId);

        Response connectorResponse = client.target(chargeUrl + "/" + chargeId)
                .request()
                .get();

        return responseFrom(uriInfo, connectorResponse, HttpStatus.SC_OK,
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
        Optional<String> fieldFormatError = checkFieldFormat(requestPayload);
        if (fieldFormatError.isPresent()) {
            return badRequestResponse(logger, fieldFormatError.get());
        }

        Response connectorResponse = client.target(chargeUrl)
                .request()
                .post(buildChargeRequestPayload(accountId, requestPayload));

        return responseFrom(uriInfo, connectorResponse, HttpStatus.SC_CREATED,
                (locationUrl, data) -> Response.created(locationUrl).entity(data),
                data -> badRequestResponse(logger, data));
    }

    @POST
    @Path(CANCEL_PAYMENT_PATH)
    @Produces(APPLICATION_JSON)
    public Response cancelCharge(@PathParam(PAYMENT_KEY) String chargeId) {
        logger.info("received cancel payment request: [{}]", chargeId);

        Response connectorResponse = client.target(chargeUrl + "/" + chargeId + "/cancel")
                .request()
                .post(Entity.json(""));

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

    private Optional<String> checkFieldFormat(JsonNode requestPayload) {
        String returnUrl = requestPayload.get(SERVICE_RETURN_URL).asText();
        if (negate(containsIgnoreCase(returnUrl, PAYMENTS_ID_PLACEHOLDER))) {
            String errorMessage = format("Payment-id placeholder is missing: '%s' does not contain a '%s' placeholder."
                    , returnUrl, PAYMENTS_ID_PLACEHOLDER);
            return Optional.of(errorMessage);
        }
        return Optional.empty();
    }

    private Entity buildChargeRequestPayload(String accountId, JsonNode requestPayload) {
        long amount = requestPayload.get(AMOUNT_KEY).asLong();
        String returnUrl = requestPayload.get(SERVICE_RETURN_URL).asText();

        return json(jsonStringBuilder()
                .add(AMOUNT_KEY, amount)
                .add(GATEWAY_ACCOUNT_KEY, accountId)
                .add(SERVICE_RETURN_URL, returnUrl)
                .build());
    }
}
