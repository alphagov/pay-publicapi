package uk.gov.pay.api.resources;

import black.door.hate.HalRepresentation;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.auth.Auth;
import io.swagger.annotations.*;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.exception.*;
import uk.gov.pay.api.model.*;
import uk.gov.pay.api.model.links.PaymentWithAllLinks;
import uk.gov.pay.api.utils.JsonStringBuilder;

import javax.ws.rs.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static javax.ws.rs.client.Entity.json;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.http.HttpStatus.SC_OK;
import static uk.gov.pay.api.model.TokenPaymentType.*;
import static uk.gov.pay.api.validation.PaymentSearchValidator.validateSearchParameters;

@Path("/")
@Api(value = "/", description = "Public Api Endpoints")
@Produces({"application/json"})
public class PaymentsResource {
    private static final Logger logger = LoggerFactory.getLogger(PaymentsResource.class);

    public static final String API_VERSION_PATH = "/v1";

    public static final String REFERENCE_KEY = "reference";
    public static final String EMAIL_KEY = "email";
    public static final String STATE_KEY = "state";
    public static final String CARD_BRAND_KEY = "card_brand";
    public static final String FROM_DATE_KEY = "from_date";
    public static final String TO_DATE_KEY = "to_date";
    public static final String PAGE = "page";
    public static final String DISPLAY_SIZE = "display_size";
    public static final String TRANSACTION_TYPE_KEY = "transactionType";
    public static final String TRANSACTION_TYPE_KEY_VALUE = "charge";


    private static final String PAYMENT_KEY = "paymentId";
    private static final String DESCRIPTION_KEY = "description";
    private static final String AMOUNT_KEY = "amount";
    private static final String SERVICE_RETURN_URL = "return_url";
    private static final String CHARGE_KEY = "charge_id";

    private static final String PAYMENTS_PATH = API_VERSION_PATH + "/payments";
    private static final String PAYMENTS_ID_PLACEHOLDER = "{" + PAYMENT_KEY + "}";
    private static final String PAYMENT_BY_ID = API_VERSION_PATH + "/payments/" + PAYMENTS_ID_PLACEHOLDER;
    private static final String PAYMENT_EVENTS_BY_ID = API_VERSION_PATH + "/payments/" + PAYMENTS_ID_PLACEHOLDER + "/events";

    private static final String CANCEL_PATH_SUFFIX = "/cancel";
    private static final String CANCEL_PAYMENT_PATH = API_VERSION_PATH + "/payments/" + PAYMENTS_ID_PLACEHOLDER + CANCEL_PATH_SUFFIX;
    private static final String REFUNDS_PAYMENT_PATH = PAYMENT_BY_ID + "/refunds";

    private static final String CONNECTOR_ACCOUNT_RESOURCE = API_VERSION_PATH + "/api/accounts/%s";
    private static final String CONNECTOR_CHARGES_RESOURCE = CONNECTOR_ACCOUNT_RESOURCE + "/charges";
    private static final String CONNECTOR_CHARGE_RESOURCE = CONNECTOR_CHARGES_RESOURCE + "/%s";
    private static final String CONNECTOR_CHARGE_EVENTS_RESOURCE = CONNECTOR_CHARGES_RESOURCE + "/%s" + "/events";
    private static final String CONNECTOR_ACCOUNT_CHARGE_CANCEL_RESOURCE = CONNECTOR_CHARGE_RESOURCE + "/cancel";

    private final String baseUrl;

    private final Client client;
    private final String connectorUrl;
    private final String connectorDDUrl;
    private final ObjectMapper objectMapper;

    public PaymentsResource(String baseUrl, Client client, String connectorUrl, String connectorDDUrl, ObjectMapper objectMapper) {
        this.baseUrl = baseUrl;
        this.client = client;
        this.connectorUrl = connectorUrl;
        this.connectorDDUrl = connectorDDUrl;
        this.objectMapper = objectMapper;
    }

    @GET
    @Path(PAYMENT_BY_ID)
    @Produces(APPLICATION_JSON)
    @ApiOperation(
            value = "Find payment by ID",
            notes = "Return information about the payment " +
                    "The Authorisation token needs to be specified in the 'authorization' header " +
                    "as 'authorization: Bearer YOUR_API_KEY_HERE'",
            code = 200)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = PaymentWithAllLinks.class),
            @ApiResponse(code = 401, message = "Credentials are required to access this resource"),
            @ApiResponse(code = 404, message = "Not found", response = PaymentError.class),
            @ApiResponse(code = 500, message = "Downstream system error", response = PaymentError.class)})
    public Response getPayment(@ApiParam(value = "accountId", hidden = true) @Auth Account account,
                               @PathParam(PAYMENT_KEY) String paymentId) {

        logger.info("Payment request - paymentId={}", paymentId);
        Response connectorResponse = client
                .target(getConnectorUrl(
                        account.getPaymentType(),
                        format(CONNECTOR_CHARGE_RESOURCE, account.getName(), paymentId)))
                .request()
                .get();

        if (connectorResponse.getStatus() == SC_OK) {
            ChargeFromResponse chargeFromResponse = connectorResponse.readEntity(ChargeFromResponse.class);
            URI paymentURI = getPaymentURI(baseUrl, chargeFromResponse.getChargeId());

            PaymentWithAllLinks payment = PaymentWithAllLinks.getPaymentWithLinks(
                    account.getPaymentType(),
                    chargeFromResponse,
                    paymentURI,
                    getPaymentEventsURI(baseUrl, chargeFromResponse.getChargeId()),
                    getPaymentCancelURI(baseUrl, chargeFromResponse.getChargeId()),
                    getPaymentRefundsURI(baseUrl, chargeFromResponse.getChargeId()));

            logger.info("Payment returned - [ {} ]", payment);
            return Response.ok(payment).build();
        }
        throw new GetChargeException(connectorResponse);
    }

    @GET
    @Path(PAYMENT_EVENTS_BY_ID)
    @Produces(APPLICATION_JSON)
    @ApiOperation(
            value = "Return payment events by ID",
            notes = "Return payment events information about a certain payment " +
                    "The Authorisation token needs to be specified in the 'authorization' header " +
                    "as 'authorization: Bearer YOUR_API_KEY_HERE'",
            code = 200)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = PaymentEvents.class),
            @ApiResponse(code = 401, message = "Credentials are required to access this resource"),
            @ApiResponse(code = 404, message = "Not found", response = PaymentError.class),
            @ApiResponse(code = 500, message = "Downstream system error", response = PaymentError.class)})
    public Response getPaymentEvents(@ApiParam(value = "accountId", hidden = true) @Auth Account account,
                                     @PathParam(PAYMENT_KEY) String paymentId) {

        logger.info("Payment events request - payment_id={}", paymentId);

        Response connectorResponse = client
                .target(getConnectorUrl(
                        account.getPaymentType(),
                        format(CONNECTOR_CHARGE_EVENTS_RESOURCE, account.getName(), paymentId)))
                .request()
                .get();

        if (connectorResponse.getStatus() == SC_OK) {

            JsonNode payload = connectorResponse.readEntity(JsonNode.class);
            URI paymentEventsLink = getPaymentEventsURI(baseUrl, payload.get(CHARGE_KEY).asText());

            URI paymentLink = getPaymentURI(baseUrl, payload.get(CHARGE_KEY).asText());

            PaymentEvents response =
                    PaymentEvents.createPaymentEventsResponse(payload, paymentLink.toString())
                            .withSelfLink(paymentEventsLink.toString());

            logger.info("Payment events returned - [ {} ]", response);

            return Response.ok(response).build();
        }

        throw new GetEventsException(connectorResponse);
    }

    @GET
    @Path(PAYMENTS_PATH)
    @Produces(APPLICATION_JSON)
    @ApiOperation(
            value = "Search payments",
            notes = "Search payments by reference, state, 'from' and 'to' date. " +
                    "The Authorisation token needs to be specified in the 'authorization' header " +
                    "as 'authorization: Bearer YOUR_API_KEY_HERE'",
            responseContainer = "List",
            code = 200)

    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = PaymentSearchResults.class),
            @ApiResponse(code = 401, message = "Credentials are required to access this resource"),
            @ApiResponse(code = 422, message = "Invalid parameters: from_date, to_date, status, display_size. See Public API documentation for the correct data formats", response = PaymentError.class),
            @ApiResponse(code = 500, message = "Downstream system error", response = PaymentError.class)})
    public Response searchPayments(@ApiParam(value = "accountId", hidden = true)
                                   @Auth Account account,
                                   @ApiParam(value = "Your payment reference to search", hidden = false)
                                   @QueryParam(REFERENCE_KEY) String reference,
                                   @ApiParam(value = "The user email used in the payment to be searched", hidden = false)
                                   @QueryParam(EMAIL_KEY) String email,
                                   @ApiParam(value = "State of payments to be searched. Example=success", hidden = false, allowableValues = "range[created,started,submitted,success,failed,cancelled,error")
                                   @QueryParam(STATE_KEY) String state,
                                   @ApiParam(value = "Card brand used for payment. Example=master-card", hidden = false)
                                   @QueryParam(CARD_BRAND_KEY) String cardBrand,
                                   @ApiParam(value = "From date of payments to be searched (this date is inclusive). Example=2015-08-13T12:35:00Z", hidden = false)
                                   @QueryParam(FROM_DATE_KEY) String fromDate,
                                   @ApiParam(value = "To date of payments to be searched (this date is exclusive). Example=2015-08-14T12:35:00Z", hidden = false)
                                   @QueryParam(TO_DATE_KEY) String toDate,
                                   @ApiParam(value = "Page number requested for the search, should be a positive integer (optional, defaults to 1)", hidden = false)
                                   @QueryParam(PAGE) String pageNumber,
                                   @ApiParam(value = "Number of results to be shown per page, should be a positive integer (optional, defaults to 500, max 500)", hidden = false)
                                   @QueryParam(DISPLAY_SIZE) String displaySize,
                                   @Context UriInfo uriInfo) {

        logger.info("Payments search request - [ {} ]",
                format("reference:%s, email: %s, status: %s, card_brand %s, fromDate: %s, toDate: %s, page: %s, display_size: %s",
                        reference, email, state, cardBrand, fromDate, toDate, pageNumber, displaySize));

        validateSearchParameters(state, reference, email, cardBrand, fromDate, toDate, pageNumber, displaySize);

        if(isNotBlank(cardBrand)){
            cardBrand = cardBrand.toLowerCase();
        }

        List<Pair<String, String>> queryParams = asList(
                Pair.of(REFERENCE_KEY, reference),
                Pair.of(EMAIL_KEY, email),
                Pair.of(STATE_KEY, state),
                Pair.of(CARD_BRAND_KEY, cardBrand),
                Pair.of(FROM_DATE_KEY, fromDate),
                Pair.of(TO_DATE_KEY, toDate),
                Pair.of(TRANSACTION_TYPE_KEY, TRANSACTION_TYPE_KEY_VALUE),
                Pair.of(PAGE, pageNumber),
                Pair.of(DISPLAY_SIZE, displaySize)
        );
        Response connectorResponse = client
                .target(getConnectorUrl(
                        account.getPaymentType(),
                        format(CONNECTOR_CHARGES_RESOURCE, account.getName()),
                        queryParams))
                .request()
                .header(HttpHeaders.ACCEPT, APPLICATION_JSON)
                .get();

        logger.info("response from connector form charge search: " + connectorResponse);

        if (connectorResponse.getStatus() == SC_OK) {
            try {
                JsonNode responseJson = connectorResponse.readEntity(JsonNode.class);
                logger.debug("json response from connector from charge search: " + responseJson);

                TypeReference<PaymentSearchResponse> typeRef = new TypeReference<PaymentSearchResponse>() {};
                PaymentSearchResponse searchResponse = objectMapper.readValue(responseJson.traverse(), typeRef);
                List<PaymentForSearchResult> paymentsForSearchResults = searchResponse.getPayments()
                        .stream()
                        .map(charge -> PaymentForSearchResult.valueOf(
                                charge,
                                getPaymentURI(baseUrl, charge.getChargeId()),
                                getPaymentEventsURI(baseUrl, charge.getChargeId()),
                                getPaymentCancelURI(baseUrl, charge.getChargeId()),
                                getPaymentRefundsURI(baseUrl, charge.getChargeId())))
                        .collect(Collectors.toList());

                HalRepresentation.HalRepresentationBuilder halRepresentation = HalRepresentation.builder()
                        .addProperty("results", paymentsForSearchResults)
                        .addProperty("count", searchResponse.getCount())
                        .addProperty("total", searchResponse.getTotal())
                        .addProperty("page", searchResponse.getPage());

                addLink(halRepresentation, "self", transformIntoPublicUri(baseUrl, searchResponse.getLinks().getSelf()));
                addLink(halRepresentation, "first_page", transformIntoPublicUri(baseUrl, searchResponse.getLinks().getFirstPage()));
                addLink(halRepresentation, "last_page", transformIntoPublicUri(baseUrl, searchResponse.getLinks().getLastPage()));
                addLink(halRepresentation, "prev_page", transformIntoPublicUri(baseUrl, searchResponse.getLinks().getPrevPage()));
                addLink(halRepresentation, "next_page", transformIntoPublicUri(baseUrl, searchResponse.getLinks().getNextPage()));

                return Response.ok(halRepresentation.build().toString()).build();
            } catch (IOException | ProcessingException | URISyntaxException e) {
                throw new SearchChargesException(e);
            }
        }
        throw new SearchChargesException(connectorResponse);
    }

    private void addLink(HalRepresentation.HalRepresentationBuilder halRepresentationBuilder, String name, URI uri) {
        if (uri != null) {
            halRepresentationBuilder.addLink(name, uri);
        }
    }

    private URI transformIntoPublicUri(String baseUrl, uk.gov.pay.api.model.links.Link link) throws URISyntaxException {
        if (link == null)
            return null;

        return UriBuilder.fromUri(baseUrl)
                .path(PAYMENTS_PATH)
                .replaceQuery(new URI(link.getHref()).getQuery())
                .build();
    }

    @POST
    @Path(PAYMENTS_PATH)
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @ApiOperation(
            value = "Create new payment",
            notes = "Create a new payment for the account associated to the Authorisation token. " +
                    "The Authorisation token needs to be specified in the 'authorization' header " +
                    "as 'authorization: Bearer YOUR_API_KEY_HERE'",
            code = 201,
            nickname = "newPayment")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Created", response = PaymentWithAllLinks.class),
            @ApiResponse(code = 400, message = "Bad request", response = PaymentError.class),
            @ApiResponse(code = 401, message = "Credentials are required to access this resource"),
            @ApiResponse(code = 422, message = "Invalid attribute value: description. Must be less than or equal to 255 characters length", response = PaymentError.class),
            @ApiResponse(code = 500, message = "Downstream system error", response = PaymentError.class)})
    public Response createNewPayment(@ApiParam(value = "accountId", hidden = true) @Auth Account account,
                                     @ApiParam(value = "requestPayload", required = true) CreatePaymentRequest requestPayload) {

        logger.info("Payment create request - [ {} ]", requestPayload);

        Response connectorResponse = client
                .target(getConnectorUrl(
                        account.getPaymentType(),
                        format(CONNECTOR_CHARGES_RESOURCE, account.getName())))
                .request()
                .post(buildChargeRequestPayload(requestPayload));

        if (connectorResponse.getStatus() == HttpStatus.SC_CREATED) {
            ChargeFromResponse chargeFromResponse = connectorResponse.readEntity(ChargeFromResponse.class);
            URI paymentUri = getPaymentURI(baseUrl, chargeFromResponse.getChargeId());
            PaymentWithAllLinks payment = PaymentWithAllLinks.getPaymentWithLinks(
                    account.getPaymentType(),
                    chargeFromResponse,
                    paymentUri,
                    getPaymentEventsURI(baseUrl, chargeFromResponse.getChargeId()),
                    getPaymentCancelURI(baseUrl, chargeFromResponse.getChargeId()),
                    getPaymentRefundsURI(baseUrl, chargeFromResponse.getChargeId()));
            logger.info("Payment returned (created): [ {} ]", payment);
            return Response.created(paymentUri).entity(payment).build();

        }

        throw new CreateChargeException(connectorResponse);
    }

    @POST
    @Path(CANCEL_PAYMENT_PATH)
    @Produces(APPLICATION_JSON)
    @ApiOperation(
            value = "Cancel payment",
            notes = "Cancel a payment based on the provided payment ID and the Authorisation token. " +
                    "The Authorisation token needs to be specified in the 'authorization' header " +
                    "as 'authorization: Bearer YOUR_API_KEY_HERE'. A payment can only be cancelled if it's in " +
                    "a state that isn't finished.",
            code = 204)
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "No Content"),
            @ApiResponse(code = 400, message = "Cancellation of payment failed", response = PaymentError.class),
            @ApiResponse(code = 401, message = "Credentials are required to access this resource"),
            @ApiResponse(code = 404, message = "Not found", response = PaymentError.class),
            @ApiResponse(code = 409, message = "Conflict", response = PaymentError.class),
            @ApiResponse(code = 500, message = "Downstream system error", response = PaymentError.class)
    })
    public Response cancelPayment(@ApiParam(value = "accountId", hidden = true) @Auth Account account,
                                  @PathParam(PAYMENT_KEY) String paymentId) {

        logger.info("Payment cancel request - payment_id=[{}]", paymentId);

        Response connectorResponse = client
                .target(getConnectorUrl(
                        account.getPaymentType(),
                        format(CONNECTOR_ACCOUNT_CHARGE_CANCEL_RESOURCE, account.getName(), paymentId)))
                .request()
                .post(Entity.json("{}"));

        if (connectorResponse.getStatus() == HttpStatus.SC_NO_CONTENT) {
            connectorResponse.close();
            return Response.noContent().build();
        }

        throw new CancelChargeException(connectorResponse);
    }

    private URI getPaymentURI(String baseUrl, String chargeId) {
        return UriBuilder.fromUri(baseUrl)
                .path(PAYMENT_BY_ID)
                .build(chargeId);
    }

    private URI getPaymentEventsURI(String baseUrl, String chargeId) {
        return UriBuilder.fromUri(baseUrl)
                .path(PAYMENT_EVENTS_BY_ID)
                .build(chargeId);
    }

    private URI getPaymentCancelURI(String baseUrl, String chargeId) {
        return UriBuilder.fromUri(baseUrl)
                .path(CANCEL_PAYMENT_PATH)
                .build(chargeId);
    }

    private URI getPaymentRefundsURI(String baseUrl, String chargeId) {
        return UriBuilder.fromUri(baseUrl)
                .path(REFUNDS_PAYMENT_PATH)
                .build(chargeId);
    }

    private String getConnectorUrl(TokenPaymentType paymentType, String urlPath) {
        return getConnectorUrl(paymentType, urlPath, Collections.emptyList());
    }


    private String getConnectorUrl(TokenPaymentType paymentType, String urlPath, List<Pair<String, String>> queryParams) {
        String url = paymentType.equals(DIRECT_DEBIT)? connectorDDUrl : connectorUrl;
        UriBuilder builder = UriBuilder
                .fromPath(url)
                .path(urlPath);

        queryParams.stream().forEach(pair -> {
            if (isNotBlank(pair.getRight())) {
                builder.queryParam(pair.getKey(), pair.getValue());
            }
        });
        return builder.toString();
    }

    private Entity buildChargeRequestPayload(CreatePaymentRequest requestPayload) {
        int amount = requestPayload.getAmount();
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
