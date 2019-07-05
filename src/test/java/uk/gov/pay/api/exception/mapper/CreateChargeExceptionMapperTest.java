package uk.gov.pay.api.exception.mapper;

import org.junit.Before;
import org.junit.Test;
import uk.gov.pay.api.exception.ConnectorResponseErrorException.ConnectorErrorResponse;
import uk.gov.pay.api.exception.CreateChargeException;
import uk.gov.pay.api.model.PaymentError;
import uk.gov.pay.commons.model.ErrorIdentifier;

import javax.ws.rs.core.Response;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.pay.api.model.PaymentError.Code.CREATE_PAYMENT_MANDATE_STATE_INVALID;
import static uk.gov.pay.api.model.PaymentError.aPaymentError;

public class CreateChargeExceptionMapperTest {

    private CreateChargeExceptionMapper mapper;
    
    @Before
    public void setUp() {
        mapper = new CreateChargeExceptionMapper();
    }
    
    @Test
    public void shouldThrow409_whenMandateStateInvalid(){
        Response response = mock(Response.class);
        when(response.readEntity(ConnectorErrorResponse.class))
                .thenReturn(new ConnectorErrorResponse(
                        ErrorIdentifier.MANDATE_STATE_INVALID, null, null));
        
        Response returnedResponse = mapper.toResponse(new CreateChargeException(response));
        PaymentError returnedError = (PaymentError) returnedResponse.getEntity();
        assertThat(returnedResponse.getStatus(), is(409));
        assertThat(returnedError.getDescription(), 
                is(aPaymentError(CREATE_PAYMENT_MANDATE_STATE_INVALID).getDescription()));
        assertThat(returnedError.getCode(), is(aPaymentError(CREATE_PAYMENT_MANDATE_STATE_INVALID).getCode()));
        
    }
    
}
