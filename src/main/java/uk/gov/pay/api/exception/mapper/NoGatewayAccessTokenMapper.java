package uk.gov.pay.api.exception.mapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.api.exception.NoGatewayAccessTokenException;
import uk.gov.pay.api.model.PaymentError;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import static javax.ws.rs.core.Response.Status.FORBIDDEN;

public class NoGatewayAccessTokenMapper implements ExceptionMapper<NoGatewayAccessTokenException> {

    private static final Logger LOGGER = LoggerFactory.getLogger(NoGatewayAccessTokenMapper.class);
    
    @Override
    public Response toResponse(NoGatewayAccessTokenException exception) {
        PaymentError paymentError = PaymentError.aPaymentError(PaymentError.Code.CREATE_PAYMENT_ACCOUNT_ERROR);
        LOGGER.error("Connector invalid response was {}.\n Returning http status {} with error body {}",
                exception.getMessage(), FORBIDDEN, paymentError);
        return Response.status(Response.Status.FORBIDDEN).entity(paymentError).build();
    }
}
