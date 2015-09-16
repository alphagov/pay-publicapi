package uk.gov.pay.api.resources;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.api.model.LinksResponse;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
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

import static javax.ws.rs.client.Entity.json;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.pay.api.model.CreatePaymentResponse.createPaymentResponse;
import static uk.gov.pay.api.utils.JsonStringBuilder.jsonStringBuilder;
import static uk.gov.pay.api.utils.ResponseUtil.badRequestResponse;
import static uk.gov.pay.api.utils.ResponseUtil.fieldsMissingResponse;
import static uk.gov.pay.api.utils.ResponseUtil.internalServerErrorResponse;
import static uk.gov.pay.api.utils.ResponseUtil.notFoundResponse;

@Path("/")
public class PaymentsResource {
    private static final String PAYMENT_KEY = "paymentId";
    private static final String AMOUNT_KEY = "amount";
    private static final String ACCOUNT_KEY = "account_id";
    private static final String GATEWAY_ACCOUNT_KEY = "gateway_account_id";
    private static final String SERVICE_RETURN_URL = "return_url";
    private static final String CHARGE_KEY = "charge_id";

    private static final String[] REQUIRED_FIELDS = {AMOUNT_KEY, ACCOUNT_KEY, SERVICE_RETURN_URL};

    public static final String PAYMENTS_PATH = "/v1/payments";
    public static final String PAYMENT_BY_ID = "/v1/payments/{" + PAYMENT_KEY + "}";

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
    public Response createNewPayment(JsonNode requestPayload, @Context UriInfo uriInfo) throws IOException {
        logger.info("received create payment request: [ {} ]", requestPayload);

        Optional<List<String>> missingFields = checkMissingFields(requestPayload);
        if (missingFields.isPresent()) {
            return fieldsMissingResponse(logger, missingFields.get());
        }

        Response connectorResponse = client.target(chargeUrl)
                .request()
                .post(buildChargeRequestPayload(requestPayload));

        return responseFrom(uriInfo, connectorResponse, HttpStatus.SC_CREATED,
                (locationUrl, data) -> Response.created(locationUrl).entity(data),
                data -> badRequestResponse(logger, data));
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

            return getNextLink(payload).map(
                    nextLink -> {
                        LinksResponse response =
                                createPaymentResponse(payload)
                                        .addSelfLink(documentLocation)
                                        .addLink(
                                                nextLink.get("rel").asText(),
                                                nextLink.get("method").asText(),
                                                nextLink.get("href").asText()
                                        );

                        logger.info("payment returned: [ {} ]", response);

                        return okResponse.apply(documentLocation, response).build();
                    }).orElseGet(() -> internalServerErrorResponse(logger, "Missing link next_url from connector response", "Internal Server Error"));
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

    private Entity buildChargeRequestPayload(JsonNode requestPayload) {
        long amount = requestPayload.get(AMOUNT_KEY).asLong();
        String accountId = requestPayload.get(ACCOUNT_KEY).asText();
        String returnUrl = requestPayload.get(SERVICE_RETURN_URL).asText();

        return json(jsonStringBuilder()
                .add(AMOUNT_KEY, amount)
                .add(GATEWAY_ACCOUNT_KEY, accountId)
                .add(SERVICE_RETURN_URL, returnUrl)
                .build());
    }
}
