package uk.gov.pay.api.exception.mapper;

import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.api.exception.CreateTelephonePaymentException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class CreateTelephonePaymentExceptionMapper implements ExceptionMapper<CreateTelephonePaymentException> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateTelephonePaymentExceptionMapper.class);
    
    @Override
    public Response toResponse(CreateTelephonePaymentException exception) {
        
        int statusCode = HttpStatus.INTERNAL_SERVER_ERROR_500;
        
        return null;
        
    }
    
    
}
