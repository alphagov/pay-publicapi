package uk.gov.pay.api.resources;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import uk.gov.pay.api.model.CreatePaymentRequest;
import uk.gov.pay.api.model.CreatePaymentResponse;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

interface PaymentsResourceDoc {

    @ApiOperation(
            value = "Find a Payment by ID",
            notes = "Return information about the payment",
            code = 200,
            response = JsonNode.class)

    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
                            @ApiResponse(code = 401, message = "Credentials are required to access this resource"),
                            @ApiResponse(code = 404, message = "Not found") })

    public Response getPayment(String accountId, String paymentId, UriInfo uriInfo);


    @ApiOperation(
            value = "Creates a new payment",
            notes = "Creates a new payment for the account associated to the Authorization token",
            code = 201,
            nickname = "newPayment",
            response = CreatePaymentResponse.class)

    @ApiResponses(value = { @ApiResponse(code = 201, message = "Created"),
                            @ApiResponse(code = 400, message = "Bad request"),
                            @ApiResponse(code = 401, message = "Credentials are required to access this resource") })

    public Response createNewPayment(String accountId, CreatePaymentRequest requestPayload, UriInfo uriInfo);


    @ApiOperation(
            value = "Cancels a payment",
            notes = "Cancels a payment based on the provided payment ID and the Authorization token",
            code = 204)

    @ApiResponses(value = { @ApiResponse(code = 204, message = "No Content"),
                            @ApiResponse(code = 400, message = "Cancellation of charge failed"),
                            @ApiResponse(code = 401, message = "Credentials are required to access this resource") })

    public Response cancelCharge(String accountId, String chargeId);

}
