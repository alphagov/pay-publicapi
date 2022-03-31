package uk.gov.pay.api.exception.mapper;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.pay.api.exception.ConnectorResponseErrorException.ConnectorErrorResponse;
import uk.gov.pay.api.exception.CreateAgreementException;
import uk.gov.pay.api.model.PaymentError;
import uk.gov.service.payments.commons.model.ErrorIdentifier;
import javax.ws.rs.core.Response;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;
import static uk.gov.pay.api.model.PaymentError.aPaymentError;

@ExtendWith(MockitoExtension.class)
public class CreateAgreementExceptionMapperTest {

    @Mock
    private Response mockResponse;

    private CreateAgreementExceptionMapper mapper = new CreateAgreementExceptionMapper();
    
    @ParameterizedTest
    @CsvSource({ "GENERIC,CREATE_AGREEMENT_CONNECTOR_ERROR,500",
    })
    public void testExceptionMapping(String errorIdentifier, String paymentError, int expectedStatusCode) {
        when(mockResponse.readEntity(ConnectorErrorResponse.class))
                .thenReturn(new ConnectorErrorResponse(ErrorIdentifier.valueOf(errorIdentifier), null, null));
        Response returnedResponse = mapper.toResponse(new CreateAgreementException(mockResponse));
        PaymentError returnedError = (PaymentError) returnedResponse.getEntity();
        PaymentError expectedError = aPaymentError(PaymentError.Code.valueOf(paymentError));
        assertThat(returnedResponse.getStatus(), is(expectedStatusCode));
        assertThat(returnedError.getDescription(),
                is(expectedError.getDescription()));
        assertThat(returnedError.getCode(), is(expectedError.getCode()));
    }
}
