package uk.gov.pay.api.resources;

import com.fasterxml.jackson.databind.JsonNode;
import io.dropwizard.auth.Auth;
import io.swagger.annotations.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.api.model.CreatePaymentRequest;
import uk.gov.pay.api.model.Payment;
import uk.gov.pay.api.model.PaymentEvents;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.ResponseBuilder;
import java.net.URI;
import java.util.Iterator;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import static java.lang.String.format;
import static javax.ws.rs.client.Entity.json;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.lang3.StringEscapeUtils.escapeHtml4;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.http.HttpStatus.SC_OK;
import static uk.gov.pay.api.model.Payment.createPaymentResponse;
import static uk.gov.pay.api.utils.JsonStringBuilder.jsonStringBuilder;
import static uk.gov.pay.api.utils.ResponseUtil.badRequestResponse;
import static uk.gov.pay.api.utils.ResponseUtil.notFoundResponse;

@Path("/")
@Api(value = "/", description = "Public Api Endpoints")
@Produces({"application/json"})
public class PaymentsResource {
    private static final String PAYMENT_KEY = "paymentId";
    private static final String REFERENCE_KEY = "reference";
    private static final String STATUS_KEY = "status";
    private static final String FROM_DATE_KEY = "from_date";
    private static final String TO_DATE_KEY = "to_date";
    private static final String DESCRIPTION_KEY = "description";
    private static final String AMOUNT_KEY = "amount";
    private static final String SERVICE_RETURN_URL = "return_url";
    private static final String CHARGE_KEY = "charge_id";

    private static final String PAYMENTS_PATH = "/v1/payments";
    private static final String PAYMENTS_ID_PLACEHOLDER = "{" + PAYMENT_KEY + "}";
    private static final String PAYMENT_BY_ID = "/v1/payments/" + PAYMENTS_ID_PLACEHOLDER;
    private static final String PAYMENT_EVENTS_BY_ID = "/v1/payments/" + PAYMENTS_ID_PLACEHOLDER + "/events";

    private static final String CANCEL_PATH_SUFFIX = "/cancel";
    private static final String CANCEL_PAYMENT_PATH = "/v1/payments/" + PAYMENTS_ID_PLACEHOLDER + CANCEL_PATH_SUFFIX;
    public static final String CONNECTOR_ACCOUNT_RESOURCE = "/v1/api/accounts/%s";
    public static final String CONNECTOR_CHARGES_RESOURCE = CONNECTOR_ACCOUNT_RESOURCE + "/charges";
    public static final String CONNECTOR_CHARGE_RESOURCE = CONNECTOR_CHARGES_RESOURCE + "/%s";
    public static final String CONNECTOR_CHARGE_EVENTS_RESOURCE = CONNECTOR_CHARGES_RESOURCE + "/%s" + "/events";
    public static final String CONNECTOR_ACCOUNT_CHARGE_CANCEL_RESOURCE = CONNECTOR_CHARGE_RESOURCE + "/cancel";

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
    @ApiOperation(
            value = "Find payment by ID",
            notes = "Return information about the payment",
            code = 200)

    @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = Payment.class),
            @ApiResponse(code = 401, message = "Credentials are required to access this resource"),
            @ApiResponse(code = 404, message = "Not found")})
    public Response getPayment(@ApiParam(value = "accountId", hidden = true) @Auth String accountId,
                               @PathParam(PAYMENT_KEY) String paymentId,
                               @Context UriInfo uriInfo) {

        logger.info("received get payment request: [ {} ]", paymentId);

        Response connectorResponse = client.target(connectorUrl + format(CONNECTOR_CHARGE_RESOURCE, accountId, paymentId))
                .request()
                .get();

        return responseFrom(uriInfo, connectorResponse, SC_OK,
                (locationUrl, data) -> Response.ok(data),
                data -> notFoundResponse(logger, data));
    }

    @GET
    @Path(PAYMENT_EVENTS_BY_ID)
    @Produces(APPLICATION_JSON)
    @ApiOperation(
            value = "Return payment events by ID",
            notes = "Return payment events information about a certain payment",
            code = 200)

    @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = Payment.class),
            @ApiResponse(code = 401, message = "Credentials are required to access this resource"),
            @ApiResponse(code = 404, message = "Not found")})
    public Response getPaymentEvents(@ApiParam(value = "accountId", hidden = true) @Auth String accountId,
                                     @PathParam(PAYMENT_KEY) String paymentId,
                                     @Context UriInfo uriInfo) {

        logger.info("received get payment request: [ {} ]", paymentId);

        Response connectorResponse = client.target(connectorUrl + format(CONNECTOR_CHARGE_EVENTS_RESOURCE, accountId, paymentId))
                .request()
                .get();

        return eventsResponseFrom(uriInfo, connectorResponse, SC_OK,
                (locationUrl, data) -> Response.ok(data),
                data -> notFoundResponse(logger, data));
    }

    @GET
    @Path(PAYMENTS_PATH)
    @Produces(APPLICATION_JSON)
    @ApiOperation(
            value = "Search payments",
            notes = "Search payments by reference, status, 'from' and 'to' date",
            code = 200)

    @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = Payment.class),
            @ApiResponse(code = 401, message = "Credentials are required to access this resource"),
            @ApiResponse(code = 404, message = "Not found")})
    public Response searchPayments(@ApiParam(value = "accountId", hidden = true) @Auth String accountId,
                                   @QueryParam(REFERENCE_KEY) String reference,
                                   @QueryParam(STATUS_KEY) String status,
                                   @QueryParam(FROM_DATE_KEY) String fromDate,
                                   @QueryParam(TO_DATE_KEY) String toDate,
                                   @Context UriInfo uriInfo) {

        logger.info("received get search payments request: [ {} ]",
                format("reference:%s, status: %s, fromDate: %s, toDate: %s", reference, status, fromDate, toDate));

        Response connectorResponse = client.target(getConnectorUlr(accountId, reference, status, fromDate, toDate))
                .request()
                .header(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON)
                .get();

        //TODO: New method to parse a list of payment results from connector.
        //      Important: We are still returning the updated date from connector when performing a search.
        //                 This needs to be replaced by created_date and avoid joining two tables on connector.
        //                 When performing that change on connector, we will also require a change in self-service,
        //                 as it is still looking at the 'updated' field from connector.
        //      E2E tests require updating and a new integration test
        if (connectorResponse.getStatus() == SC_OK) {
            return Response.ok(connectorResponse.readEntity(String.class)).build();
        } else {
            return badRequestResponse(logger, "Search payments failed.");
        }
    }

    @POST
    @Path(PAYMENTS_PATH)
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @ApiOperation(
            value = "Create new payment",
            notes = "Create a new payment for the account associated to the Authorisation token",
            code = 201,
            nickname = "newPayment")

    @ApiResponses(value = {@ApiResponse(code = 201, message = "Created", response = Payment.class),
            @ApiResponse(code = 400, message = "Bad request"),
            @ApiResponse(code = 401, message = "Credentials are required to access this resource")})
    public Response createNewPayment(@ApiParam(value = "accountId", hidden = true) @Auth String accountId,
                                     @ApiParam(value = "requestPayload", required = true) @Valid CreatePaymentRequest requestPayload,
                                     @Context UriInfo uriInfo) {

        logger.info("received create payment request: [ {} ]", requestPayload);

        Response connectorResponse = client.target(connectorUrl + format(CONNECTOR_CHARGES_RESOURCE, accountId))
                .request()
                .post(buildChargeRequestPayload(requestPayload));

        return responseFrom(uriInfo, connectorResponse, HttpStatus.SC_CREATED,
                (locationUrl, data) -> Response.created(locationUrl).entity(data),
                data -> badRequestResponse(logger, data));
    }

    @POST
    @Path(CANCEL_PAYMENT_PATH)
    @Produces(APPLICATION_JSON)
    @ApiOperation(
            value = "Cancel payment",
            notes = "Cancel a payment based on the provided payment ID and the Authorisation token",
            code = 204)

    @ApiResponses(value = {@ApiResponse(code = 204, message = "No Content"),
            @ApiResponse(code = 400, message = "Payment cancellation failed"),
            @ApiResponse(code = 401, message = "Credentials are required to access this resource")})

    public Response cancelPayment(@ApiParam(value = "accountId", hidden = true) @Auth String accountId,
                                  @PathParam(PAYMENT_KEY) String paymentId) {

        logger.info("received cancel payment request: [{}]", paymentId);

        Response connectorResponse = client.target(connectorUrl + format(CONNECTOR_ACCOUNT_CHARGE_CANCEL_RESOURCE, accountId, paymentId))
                .request()
                .post(Entity.json("{}"));

        if (connectorResponse.getStatus() == HttpStatus.SC_NO_CONTENT) {
            return Response.noContent().build();
        }

        return badRequestResponse(logger, "Cancellation of charge failed.");
    }

    private String getConnectorUlr(@ApiParam(value = "accountId", hidden = true) @Auth String accountId,
                                   String reference, String status, String fromDate, String toDate) {

        UriBuilder builder = UriBuilder
                .fromPath(connectorUrl)
                .path(format(CONNECTOR_CHARGES_RESOURCE, accountId));

        if (isNotBlank(reference)) {
            builder.queryParam(REFERENCE_KEY, reference);
        }

        if (isNotBlank(status)) {
            builder.queryParam(STATUS_KEY, status);
        }

        if (isNotBlank(fromDate)) {
            builder.queryParam(FROM_DATE_KEY, fromDate);
        }

        if (isNotBlank(fromDate)) {
            builder.queryParam(TO_DATE_KEY, toDate);
        }
        return builder.toString();
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

            Payment response =
                    createPaymentResponse(payload)
                            .withSelfLink(documentLocation.toString());

            if (nextLinkMaybe.isPresent()) {
                JsonNode nextLink = nextLinkMaybe.get();
                response.withNextLink(nextLink.get("href").asText());
            }

            logger.info("payment returned: [ {} ]", response);

            return okResponse.apply(documentLocation, response).build();
        }

        return errorResponse.apply(payload);
    }

    private Response eventsResponseFrom(UriInfo uriInfo, Response connectorResponse, int okStatus,
                                  BiFunction<URI, Object, ResponseBuilder> okResponse,
                                  Function<JsonNode, Response> errorResponse) {
        if (!connectorResponse.hasEntity()) {
            return badRequestResponse(logger, "Connector response contains no payload!");
        }

        JsonNode payload = connectorResponse.readEntity(JsonNode.class);
        if (connectorResponse.getStatus() == okStatus) {
            URI documentLocation = uriInfo.getBaseUriBuilder()
                    .path(PAYMENT_EVENTS_BY_ID)
                    .build(payload.get(CHARGE_KEY).asText());

            PaymentEvents response =
                    PaymentEvents.createPaymentEventsResponse(payload)
                            .withSelfLink(documentLocation.toString());

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

    private Entity buildChargeRequestPayload(CreatePaymentRequest requestPayload) {
        long amount = requestPayload.getAmount();
        String reference = requestPayload.getReference();
        String description = requestPayload.getDescription();
        String returnUrl = requestPayload.getReturnUrl();
        return json(jsonStringBuilder()
                .add(AMOUNT_KEY, amount)
                .add(REFERENCE_KEY, escapeHtml4(reference))
                .add(DESCRIPTION_KEY, escapeHtml4(description))
                .add(SERVICE_RETURN_URL, returnUrl)
                .build());
    }

}
