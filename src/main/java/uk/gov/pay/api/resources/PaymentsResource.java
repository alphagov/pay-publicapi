package uk.gov.pay.api.resources;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.auth.Auth;
import io.swagger.annotations.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.api.exception.CreateChargeConnectorErrorResponseException;
import uk.gov.pay.api.model.*;
import uk.gov.pay.api.utils.JsonStringBuilder;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.ResponseBuilder;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static javax.ws.rs.client.Entity.json;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.upperCase;
import static org.apache.http.HttpStatus.SC_OK;
import static uk.gov.pay.api.resources.ParamValidator.validateDate;
import static uk.gov.pay.api.resources.ParamValidator.validateStatus;
import static uk.gov.pay.api.utils.ResponseUtil.*;

@Path("/")
@Api(value = "/", description = "Public Api Endpoints")
@Produces({"application/json"})
public class PaymentsResource {
    private static final Logger logger = LoggerFactory.getLogger(PaymentsResource.class);

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
    private static final String CONNECTOR_ACCOUNT_RESOURCE = "/v1/api/accounts/%s";
    private static final String CONNECTOR_CHARGES_RESOURCE = CONNECTOR_ACCOUNT_RESOURCE + "/charges";
    private static final String CONNECTOR_CHARGE_RESOURCE = CONNECTOR_CHARGES_RESOURCE + "/%s";
    private static final String CONNECTOR_CHARGE_EVENTS_RESOURCE = CONNECTOR_CHARGES_RESOURCE + "/%s" + "/events";
    private static final String CONNECTOR_ACCOUNT_CHARGE_CANCEL_RESOURCE = CONNECTOR_CHARGE_RESOURCE + "/cancel";


    private final Client client;
    private final String connectorUrl;
    private final ObjectMapper objectMapper;

    public PaymentsResource(Client client, String connectorUrl) {
        this.client = client;
        this.connectorUrl = connectorUrl;
        this.objectMapper = new ObjectMapper();
    }

    @GET
    @Path(PAYMENT_BY_ID)
    @Produces(APPLICATION_JSON)
    @ApiOperation(
            value = "Find payment by ID",
            notes = "Return information about the payment",
            code = 200)

    @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = PaymentWithLinks.class),
            @ApiResponse(code = 401, message = "Credentials are required to access this resource"),
            @ApiResponse(code = 404, message = "Not found")})
    public Response getPayment(@ApiParam(value = "accountId", hidden = true) @Auth String accountId,
                               @PathParam(PAYMENT_KEY) String paymentId,
                               @Context UriInfo uriInfo) {

        logger.info("received get payment request: [ {} ]", paymentId);

        Response connectorResponse = client.target(connectorUrl + format(CONNECTOR_CHARGE_RESOURCE, accountId, paymentId))
                .request()
                .get();

        return responseForPaymentWithLinks(uriInfo, connectorResponse, SC_OK,
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

    @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = PaymentEvents.class),
            @ApiResponse(code = 401, message = "Credentials are required to access this resource"),
            @ApiResponse(code = 404, message = "Not found")})
    public Response getPaymentEvents(@ApiParam(value = "accountId", hidden = true) @Auth String accountId,
                                     @PathParam(PAYMENT_KEY) String paymentId,
                                     @Context UriInfo uriInfo) {

        logger.info("received get payment request: [ {} ]", paymentId);

        Response connectorResponse = client.target(connectorUrl + format(CONNECTOR_CHARGE_EVENTS_RESOURCE, accountId, paymentId))
                .request()
                .get();

        return eventsResponseFrom(
                    uriInfo,
                    connectorResponse,
                    SC_OK,
                    (locationUrl, data) -> Response.ok(data),
                    data -> notFoundResponse(logger, data)
        );
    }

    @GET
    @Path(PAYMENTS_PATH)
    @Produces(APPLICATION_JSON)
    @ApiOperation(
            value = "Search payments",
            notes = "Search payments by reference, status, 'from' and 'to' date",
            responseContainer = "List",
            code = 200)

    @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = PaymentSearchResults.class),
            @ApiResponse(code = 401, message = "Credentials are required to access this resource"),
            @ApiResponse(code = 500, message = "Search payments failed"),
            @ApiResponse(code = 422, message = "fields [from_date, to_date, status] are not in correct format. see public api documentation for the correct data formats")})
    public Response searchPayments(@ApiParam(value = "accountId", hidden = true)
                                   @Auth String accountId,
                                   @ApiParam(value = "Your payment reference to search", hidden = false)
                                   @QueryParam(REFERENCE_KEY) String reference,
                                   @ApiParam(value = "Status of payments to be searched. Example=SUCCEEDED", hidden = false, allowableValues = "range[SUCCEEDED,CREATED,IN PROGRESS,FAILED,SYSTEM CANCELLED")
                                   @QueryParam(STATUS_KEY) String status,
                                   @ApiParam(value = "From date of payments to be searched (this date is inclusive). Example=2015-08-13T12:35:00Z", hidden = false)
                                   @QueryParam(FROM_DATE_KEY) String fromDate,
                                   @ApiParam(value = "To date of payments to be searched (this date is exclusive). Example=2015-08-13T12:35:00Z", hidden = false)
                                   @QueryParam(TO_DATE_KEY) String toDate,
                                   @Context UriInfo uriInfo) {

        logger.info("received get search payments request: [ {} ]",
                format("reference:%s, status: %s, fromDate: %s, toDate: %s", reference, status, fromDate, toDate));

        List<Pair<String, String>> validationErrors = new LinkedList<>();

        if (!validateStatus(status)) {
            validationErrors.add(Pair.of(STATUS_KEY, status));
        }

        if (!validateDate(fromDate)) {
            validationErrors.add(Pair.of(FROM_DATE_KEY, fromDate));
        }

        if (!validateDate(toDate)) {
            validationErrors.add(Pair.of(TO_DATE_KEY, toDate));
        }

        if (validationErrors.isEmpty()) {
            List<Pair<String, String>> queryParams = asList(
                    Pair.of(REFERENCE_KEY, reference),
                    Pair.of(STATUS_KEY, upperCase(status)),
                    Pair.of(FROM_DATE_KEY, fromDate),
                    Pair.of(TO_DATE_KEY, toDate)
            );

            Response connectorResponse = client.target(getConnectorUlr(accountId, queryParams))
                    .request()
                    .header(HttpHeaders.ACCEPT, APPLICATION_JSON)
                    .get();

            return responseForPaymentsWithSelfLinks(
                    uriInfo,
                    connectorResponse, SC_OK,
                    Response::ok,
                    () -> serverErrorResponse(logger, "Search payments failed")
            );

        } else {
            return unprocessableEntityResponse(logger, errorMessageFrom(validationErrors));
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

    @ApiResponses(value = {@ApiResponse(code = 201, message = "Created", response = PaymentWithLinks.class),
            @ApiResponse(code = 400, message = "Bad request"),
            @ApiResponse(code = 401, message = "Credentials are required to access this resource"),
            @ApiResponse(code = 500, message = "There is an error with this account. Please contact support"),
            @ApiResponse(code = 500, message = "Downstream system error")})
    public Response createNewPayment(@ApiParam(value = "accountId", hidden = true) @Auth String accountId,
                                     @ApiParam(value = "requestPayload", required = true) @Valid CreatePaymentRequest requestPayload,
                                     @Context UriInfo uriInfo) {

        logger.info("received create payment request: [ {} ]", requestPayload);

        Response connectorResponse = client.target(connectorUrl + format(CONNECTOR_CHARGES_RESOURCE, accountId))
                .request()
                .post(buildChargeRequestPayload(requestPayload));

        if (connectorResponse.getStatus() == HttpStatus.SC_CREATED) {

            PaymentConnectorResponse response = connectorResponse.readEntity(PaymentConnectorResponse.class);

            URI paymentUri = uriInfo.getBaseUriBuilder()
                    .path(PAYMENT_BY_ID)
                    .build(response.getChargeId());

            PaymentWithLinks payment = PaymentWithLinks.valueOf(response, paymentUri);

            logger.info("payment returned: [ {} ]", payment);
            return Response.created(paymentUri).entity(payment).build();

        } else {
            throw new CreateChargeConnectorErrorResponseException(connectorResponse.getStatus(), connectorResponse.readEntity(String.class));
        }
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

    private String errorMessageFrom(List<Pair<String, String>> invalidParams) {
        List<String> keys = invalidParams.stream().map(Pair::getLeft).collect(Collectors.toList());
        return String.format("fields [%s] are not in correct format. see public api documentation for the correct data formats", StringUtils.join(keys, ", "));
    }

    private String getConnectorUlr(String accountId, List<Pair<String, String>> queryParams) {
        UriBuilder builder = UriBuilder
                .fromPath(connectorUrl)
                .path(format(CONNECTOR_CHARGES_RESOURCE, accountId));

        queryParams.stream().forEach(pair -> {
            if (isNotBlank(pair.getRight())) {
                builder.queryParam(pair.getKey(), pair.getValue());
            }
        });
        return builder.toString();
    }

    private Response responseForPaymentWithLinks(UriInfo uriInfo, Response connectorResponse, int okStatus,
                                                 BiFunction<URI, Object, ResponseBuilder> okResponse,
                                                 Function<JsonNode, Response> errorResponse) {
        if (connectorResponse.getStatus() == okStatus) {
            PaymentConnectorResponse response = connectorResponse.readEntity(PaymentConnectorResponse.class);
            URI documentLocation = uriInfo.getBaseUriBuilder()
                    .path(PAYMENT_BY_ID)
                    .build(response.getChargeId());

            PaymentWithLinks payment = PaymentWithLinks.valueOf(response, documentLocation);

            logger.info("payment returned: [ {} ]", payment);
            return okResponse.apply(documentLocation, payment).build();
        } else {
            return errorResponse.apply(connectorResponse.readEntity(JsonNode.class));
        }
    }

    private Response responseForPaymentsWithSelfLinks(UriInfo uriInfo, Response connectorResponse, int okStatus,
                                                      Function<Object, ResponseBuilder> okResponse,
                                                      Supplier<Response> errorResponse) {

        if (connectorResponse.getStatus() == okStatus) {
            try {
                JsonNode responseJson = connectorResponse.readEntity(JsonNode.class);

                TypeReference<HashMap<String, List<PaymentConnectorResponse>>> typeRef
                        = new TypeReference<HashMap<String, List<PaymentConnectorResponse>>>() {
                };

                Map<String, List<PaymentConnectorResponse>> chargesMap
                        = objectMapper.readValue(responseJson.traverse(), typeRef);

                List<PaymentWithSelfLinks> paymentsWithSelfLink = chargesMap.get("results")
                        .stream()
                        .map(charge -> {
                            URI paymentLink = uriInfo.getBaseUriBuilder()
                                    .path(PAYMENT_BY_ID)
                                    .build(charge.getChargeId());

                            return PaymentWithSelfLinks.valueOf(charge, paymentLink);
                        })
                        .collect(Collectors.toList());

                return okResponse.apply(new PaymentSearchResults(paymentsWithSelfLink)).build();

            } catch (IOException e) {
                return errorResponse.get();
            }
        }

        return errorResponse.get();
    }

    private Response eventsResponseFrom(UriInfo uriInfo, Response connectorResponse, int okStatus,
                                        BiFunction<URI, Object, ResponseBuilder> okResponse,
                                        Function<JsonNode, Response> errorResponse) {
        if (!connectorResponse.hasEntity()) {
            return badRequestResponse(logger, "Connector response contains no payload!");
        }

        JsonNode payload = connectorResponse.readEntity(JsonNode.class);
        if (connectorResponse.getStatus() == okStatus) {
            URI paymentEventsLink = uriInfo.getBaseUriBuilder()
                    .path(PAYMENT_EVENTS_BY_ID)
                    .build(payload.get(CHARGE_KEY).asText());

            URI paymentLink = uriInfo.getBaseUriBuilder()
                    .path(PAYMENT_BY_ID)
                    .build(payload.get(CHARGE_KEY).asText());

            PaymentEvents response =
                    PaymentEvents.createPaymentEventsResponse(payload, paymentLink.toString())
                            .withSelfLink(paymentEventsLink.toString());

            logger.info("payment returned: [ {} ]", response);

            return okResponse.apply(paymentEventsLink, response).build();
        }

        return errorResponse.apply(payload);
    }

    private Entity buildChargeRequestPayload(CreatePaymentRequest requestPayload) {
        long amount = requestPayload.getAmount();
        String reference = requestPayload.getReference();
        String description = requestPayload.getDescription();
        String returnUrl = requestPayload.getReturnUrl();
        return json(new JsonStringBuilder()
                .add(AMOUNT_KEY, amount)
                .add(REFERENCE_KEY, reference)
                .add(DESCRIPTION_KEY, description)
                .add(SERVICE_RETURN_URL, returnUrl)
                .build());
    }
}
