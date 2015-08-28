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
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import static javax.ws.rs.client.Entity.json;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.pay.api.model.CreatePaymentResponse.createPaymentResponse;
import static uk.gov.pay.api.utils.JsonStringBuilder.jsonString;
import static uk.gov.pay.api.utils.ResponseUtil.badRequestResponse;
import static uk.gov.pay.api.utils.ResponseUtil.fieldsMissingResponse;
import static uk.gov.pay.api.utils.ResponseUtil.notFoundResponse;

@Path("/")
public class PaymentsResource {
    private static final String PAYMENT_KEY = "paymentId";
    private static final String AMOUNT_KEY = "amount";
    private static final String GATEWAY_ACCOUNT_KEY = "gateway_account_id";
    private static final String CHARGE_KEY = "charge_id";

    private static final String[] REQUIRED_FIELDS = {AMOUNT_KEY, GATEWAY_ACCOUNT_KEY};

    public static final String PAYMENTS_PATH = "/v1/payments";
    public static final String PAYMENT_BY_ID = "/v1/payments/{" + PAYMENT_KEY + "}";

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
    public Response getCharge(@PathParam(PAYMENT_KEY) String chargeId, @Context UriInfo uriInfo) {
        logger.info("received get payment request: [ {} ]", chargeId);

        Response connectorResponse = client.target(connectorUrl + "/" + chargeId)
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
    public Response createNewPayment(JsonNode node, @Context UriInfo uriInfo) throws IOException {
        logger.info("received create payment request: [ {} ]", node);

        Optional<List<String>> missingFields = checkMissingFields(node);
        if (missingFields.isPresent()) {
            return fieldsMissingResponse(logger, missingFields.get());
        }

        Response connectorResponse = client.target(connectorUrl)
                .request()
                .post(buildChargeRequest(node));

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
            URI newLocation = uriInfo.getBaseUriBuilder()
                    .path(PAYMENT_BY_ID)
                    .build(payload.get(CHARGE_KEY).asText());

            LinksResponse response = createPaymentResponse(payload)
                    .addSelfLink(newLocation.toString());

            logger.info("payment returned: [ {} ]", response);

            return okResponse.apply(newLocation, response).build();
        }
        return errorResponse.apply(payload);
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

    private Entity buildChargeRequest(JsonNode request) {
        long amount = request.get(AMOUNT_KEY).asLong();
        String gatewayAccountId = request.get(GATEWAY_ACCOUNT_KEY).asText();

        return json(jsonString(AMOUNT_KEY, amount, GATEWAY_ACCOUNT_KEY, gatewayAccountId));
    }
}
