package uk.gov.pay.api.resources;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

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
}
