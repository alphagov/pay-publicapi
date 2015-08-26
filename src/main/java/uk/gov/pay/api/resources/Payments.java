package uk.gov.pay.api.resources;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.api.model.CreatePaymentResponse;
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
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static javax.ws.rs.client.Entity.json;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.pay.api.utils.JsonStringBuilder.jsonStringBuilder;
import static uk.gov.pay.api.utils.ResponseUtil.badRequestResponse;
import static uk.gov.pay.api.utils.ResponseUtil.fieldsMissingResponse;

@Path("/")
public class Payments {
    public static final String PAYMENTS_PATH = "/payments";
    public static final String PAYMENT_BY_ID = "/payments/{paymentId}";

    private static final String[] REQUIRED_FIELDS = {"amount", "gateway_account"};

    private final Logger logger = LoggerFactory.getLogger(Payments.class);
    private final Client client;
    private final String connectorUrl;

    public Payments(Client client, String connectorUrl) {
        this.client = client;
        this.connectorUrl = connectorUrl;
    }

    @POST
    @Path(PAYMENTS_PATH)
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    public Response createNewPayment(JsonNode node, @Context UriInfo uriInfo) throws IOException {
        Optional<List<String>> missingFields = checkMissingFields(node);
        if (missingFields.isPresent()) {
            return fieldsMissingResponse(logger, missingFields.get());
        }

        Response connectorResponse = client.target(connectorUrl)
                .request()
                .post(buildChargeRequest(node));

        if (!connectorResponse.hasEntity()) {
            return badRequestResponse(logger, "Connector response contains no payload!");
        }

        JsonNode payload = connectorResponse.readEntity(JsonNode.class);
        if (connectorResponse.getStatus() == HttpStatus.SC_CREATED) {
            return buildPaymentCreatedResponse(payload, uriInfo);
        }
        return badRequestResponse(logger, payload);
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

    private Response buildPaymentCreatedResponse(JsonNode payload, UriInfo uriInfo) {
        String chargeId = payload.get("charge_id").asText();
        URI newLocation = uriInfo.getBaseUriBuilder()
                .path(PAYMENT_BY_ID).build(chargeId);

        LinksResponse response = new CreatePaymentResponse(chargeId)
                .addSelfLink(newLocation.toString());

        logger.info("payment created: chargeId: [ {} ]", response);
        return Response.created(newLocation).entity(response).build();
    }

    private Entity buildChargeRequest(JsonNode request) {
        int amount = request.get("amount").asInt();
        String gatewayAccountId = request.get("gateway_account").asText();

        return json(jsonStringBuilder()
                .add("amount", amount)
                .add("gateway_account", gatewayAccountId)
                .build());
    }

    @GET
    @Path(PAYMENT_BY_ID)
    @Produces(APPLICATION_JSON)
    public Response getCharge(@PathParam("paymentId") long chargeId, @Context UriInfo uriInfo) {
        return Response.ok().build();
    }
}
