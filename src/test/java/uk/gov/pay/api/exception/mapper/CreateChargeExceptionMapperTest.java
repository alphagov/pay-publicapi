package uk.gov.pay.api.exception.mapper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
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

@RunWith(MockitoJUnitRunner.class)
public class CreateChargeExceptionMapperTest {


    @Mock
    private Response mockResponse = mock(Response.class);

    private CreateChargeExceptionMapper mapper;
    
    @Before
    public void setUp() {
        mapper = new CreateChargeExceptionMapper();
    }
    
    @Test
    public void shouldThrow409_whenMandateStateInvalid(){
        when(mockResponse.readEntity(ConnectorErrorResponse.class))
                .thenReturn(new ConnectorErrorResponse(
                        ErrorIdentifier.MANDATE_STATE_INVALID, null, null));
        
        Response returnedResponse = mapper.toResponse(new CreateChargeException(mockResponse));
        PaymentError returnedError = (PaymentError) returnedResponse.getEntity();
        PaymentError expectedError = aPaymentError(CREATE_PAYMENT_MANDATE_STATE_INVALID);
        
        assertThat(returnedResponse.getStatus(), is(409));
        assertThat(returnedError.getDescription(), 
                is(expectedError.getDescription()));
        assertThat(returnedError.getCode(), is(expectedError.getCode()));
        
    }
    
}
