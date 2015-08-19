package uk.gov.pay.api.resources;

import com.fasterxml.jackson.databind.JsonNode;
import uk.gov.pay.api.config.PublicApiConfig;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static uk.gov.pay.api.utils.JsonStringBuilder.jsonStringBuilder;

@Path("/payments")
public class Payments {

    private final Client client;
    private final String connectorUrl;

    public Payments(Client client, String connectorUrl) {
        this.client = client;
        this.connectorUrl = connectorUrl;
    }

    @POST
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    public Response createNewPayment(JsonNode node) {
        int amount = node.get("amount").asInt();

        String connectorRequestPayload = jsonStringBuilder()
                .add("amount", amount).build();

        JsonNode connectorResponse = client
                .target(connectorUrl)
                .request(APPLICATION_JSON_TYPE)
                .post(
                        Entity.json(connectorRequestPayload),
                        JsonNode.class
                );

        String payId = connectorResponse.get("charge_id").asText();

        String responsePayload = jsonStringBuilder()
                .add("pay_id", payId)
                .build();

        return Response.ok(responsePayload).build();
    }
}
