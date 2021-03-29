package uk.gov.pay.api.exception.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.pay.api.exception.ConnectorResponseErrorException.ConnectorErrorResponse;
import uk.gov.pay.api.exception.CreateChargeException;
import uk.gov.pay.api.model.PaymentError;
import uk.gov.service.payments.commons.model.ErrorIdentifier;

import javax.ws.rs.core.Response;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.pay.api.model.PaymentError.Code.CREATE_PAYMENT_MANDATE_ID_INVALID;
import static uk.gov.pay.api.model.PaymentError.Code.CREATE_PAYMENT_MANDATE_STATE_INVALID;
import static uk.gov.pay.api.model.PaymentError.aPaymentError;

@ExtendWith(MockitoExtension.class)
public class CreateChargeExceptionMapperTest {

    @Mock
    private Response mockResponse = mock(Response.class);

    private CreateChargeExceptionMapper mapper;
    
    @BeforeEach
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
    
    @Test
    public void shouldThrow409_whenMandateIdInvalid() {
        when(mockResponse.readEntity(ConnectorErrorResponse.class))
                .thenReturn(new ConnectorErrorResponse(ErrorIdentifier.MANDATE_ID_INVALID, null, null));

        Response returnedResponse = mapper.toResponse(new CreateChargeException(mockResponse));
        PaymentError returnedError = (PaymentError) returnedResponse.getEntity();
        PaymentError expectedError = aPaymentError(CREATE_PAYMENT_MANDATE_ID_INVALID);

        assertThat(returnedResponse.getStatus(), is(409));
        assertThat(returnedError.getDescription(),
                is(expectedError.getDescription()));
        assertThat(returnedError.getCode(), is(expectedError.getCode()));
    }
}
