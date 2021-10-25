package uk.gov.pay.api.exception.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
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
import static uk.gov.pay.api.model.PaymentError.Code.CREATE_PAYMENT_VALIDATION_ERROR;
import static uk.gov.pay.api.model.PaymentError.Code.RESOURCE_ACCESS_FORBIDDEN;
import static uk.gov.pay.api.model.PaymentError.aPaymentError;

@ExtendWith(MockitoExtension.class)
public class CreateChargeExceptionMapperTest {

    @Mock
    private Response mockResponse;

    private CreateChargeExceptionMapper mapper;
    
    @BeforeEach
    public void setUp() {
        mapper = new CreateChargeExceptionMapper();
    }
    
    @ParameterizedTest
    @CsvSource({
            "TELEPHONE_PAYMENT_NOTIFICATIONS_NOT_ALLOWED, RESOURCE_ACCESS_FORBIDDEN, 403",
            "ACCOUNT_NOT_LINKED_WITH_PSP, ACCOUNT_NOT_LINKED_WITH_PSP, 403",
            "MOTO_NOT_ALLOWED, CREATE_PAYMENT_MOTO_NOT_ENABLED, 422",
    })
    public void shouldThrow409_whenMandateIdInvalid(String errorIdentifier, String paymentError, int expectedStatusCode) {
        when(mockResponse.readEntity(ConnectorErrorResponse.class))
                .thenReturn(new ConnectorErrorResponse(ErrorIdentifier.valueOf(errorIdentifier), null, null));

        Response returnedResponse = mapper.toResponse(new CreateChargeException(mockResponse));
        PaymentError returnedError = (PaymentError) returnedResponse.getEntity();
        PaymentError expectedError = aPaymentError(PaymentError.Code.valueOf(paymentError));

        assertThat(returnedResponse.getStatus(), is(expectedStatusCode));
        assertThat(returnedError.getDescription(),
                is(expectedError.getDescription()));
        assertThat(returnedError.getCode(), is(expectedError.getCode()));
    }
}
