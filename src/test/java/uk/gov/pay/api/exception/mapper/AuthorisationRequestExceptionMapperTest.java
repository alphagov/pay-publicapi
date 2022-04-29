package uk.gov.pay.api.exception.mapper;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.pay.api.exception.AuthorisationRequestException;
import uk.gov.pay.api.exception.ConnectorResponseErrorException;
import uk.gov.pay.api.model.RequestError;
import uk.gov.service.payments.commons.model.ErrorIdentifier;

import javax.ws.rs.core.Response;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.pay.api.model.RequestError.Code.CREATE_PAYMENT_CONNECTOR_ERROR;
import static uk.gov.service.payments.commons.model.ErrorIdentifier.AUTHORISATION_ERROR;
import static uk.gov.service.payments.commons.model.ErrorIdentifier.AUTHORISATION_REJECTED;
import static uk.gov.service.payments.commons.model.ErrorIdentifier.CARD_NUMBER_REJECTED;
import static uk.gov.service.payments.commons.model.ErrorIdentifier.INVALID_ATTRIBUTE_VALUE;
import static uk.gov.service.payments.commons.model.ErrorIdentifier.ONE_TIME_TOKEN_ALREADY_USED;
import static uk.gov.service.payments.commons.model.ErrorIdentifier.ONE_TIME_TOKEN_INVALID;
import static uk.gov.service.payments.commons.model.ErrorIdentifier.TELEPHONE_PAYMENT_NOTIFICATIONS_NOT_ALLOWED;

@ExtendWith(MockitoExtension.class)
class AuthorisationRequestExceptionMapperTest {

    @Mock
    private Response mockResponse;

    private final AuthorisationRequestExceptionMapper mapper = new AuthorisationRequestExceptionMapper();
    static Object[] parametersForMapping() {
        return new Object[] {
                new Object[]{CARD_NUMBER_REJECTED, "The card_number is not a valid card number", 402, "P0010"},
                new Object[]{AUTHORISATION_REJECTED, "The payment was rejected", 402, "P0010"},
                new Object[]{AUTHORISATION_ERROR, "There was an error authorising the payment", 500, "P0050"},
                new Object[]{ONE_TIME_TOKEN_INVALID, "The one_time_token has already been used", 400, "P1211"},
                new Object[]{ONE_TIME_TOKEN_ALREADY_USED, "The one_time_token has already been used", 400, "P1212"},
                new Object[]{INVALID_ATTRIBUTE_VALUE, "Invalid attribute value: card_number. Must be a valid card number", 422, "P0102"},
                new Object[]{TELEPHONE_PAYMENT_NOTIFICATIONS_NOT_ALLOWED, "Downstream system error", 500, "P0198"}

        };
    }

    @ParameterizedTest
    @MethodSource("parametersForMapping")
    void testMapping(ErrorIdentifier errorIdentifier, String exceptionMsg, int error, String errorCode) {
        when(mockResponse.readEntity(ConnectorResponseErrorException.ConnectorErrorResponse.class))
                .thenReturn(new ConnectorResponseErrorException.ConnectorErrorResponse(errorIdentifier, null, List.of(exceptionMsg)));
        Response returnedResponse = mapper.toResponse(new AuthorisationRequestException(mockResponse));
        RequestError returnedError = (RequestError) returnedResponse.getEntity();
        assertThat(returnedError.getCode(), is(errorCode));
        assertThat(returnedError.getDescription(), is(exceptionMsg));
        assertThat(returnedResponse.getStatus(), is(error));
    }

}