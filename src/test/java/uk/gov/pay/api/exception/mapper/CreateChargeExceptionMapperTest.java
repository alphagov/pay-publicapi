package uk.gov.pay.api.exception.mapper;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.pay.api.exception.ConnectorResponseErrorException.ConnectorErrorResponse;
import uk.gov.pay.api.exception.CreateChargeException;
import uk.gov.pay.api.model.RequestError;
import uk.gov.service.payments.commons.model.ErrorIdentifier;

import javax.ws.rs.core.Response;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;
import static uk.gov.service.payments.commons.model.ErrorIdentifier.ACCOUNT_DISABLED;
import static uk.gov.service.payments.commons.model.ErrorIdentifier.ACCOUNT_NOT_LINKED_WITH_PSP;
import static uk.gov.service.payments.commons.model.ErrorIdentifier.AUTHORISATION_API_NOT_ALLOWED;
import static uk.gov.service.payments.commons.model.ErrorIdentifier.INVALID_ATTRIBUTE_VALUE;
import static uk.gov.service.payments.commons.model.ErrorIdentifier.MISSING_MANDATORY_ATTRIBUTE;
import static uk.gov.service.payments.commons.model.ErrorIdentifier.MOTO_NOT_ALLOWED;
import static uk.gov.service.payments.commons.model.ErrorIdentifier.TELEPHONE_PAYMENT_NOTIFICATIONS_NOT_ALLOWED;
import static uk.gov.service.payments.commons.model.ErrorIdentifier.UNEXPECTED_ATTRIBUTE;
import static uk.gov.service.payments.commons.model.ErrorIdentifier.ZERO_AMOUNT_NOT_ALLOWED;

@ExtendWith(MockitoExtension.class)
class CreateChargeExceptionMapperTest {

    @Mock
    private Response mockResponse;

    private final CreateChargeExceptionMapper mapper = new CreateChargeExceptionMapper();

    static Object[] parametersForMapping() {
        return new Object[] {
                new Object[]{ACCOUNT_DISABLED, false, "GOV.UK Pay has disabled payment and refund creation on this account. Contact support with your error code.", 422, "P0941"},
                new Object[]{ZERO_AMOUNT_NOT_ALLOWED, false, "Invalid attribute value: amount. Must be greater than or equal to 1", 422, "P0102"},
                new Object[]{MOTO_NOT_ALLOWED, false, "MOTO payments are not enabled for this account. Please contact support if you would like to process MOTO payments", 422, "P0196"},
                new Object[]{TELEPHONE_PAYMENT_NOTIFICATIONS_NOT_ALLOWED, false, "Access to this resource is not enabled for this account. Please contact support.", 403, "P0930"},
                new Object[]{ACCOUNT_NOT_LINKED_WITH_PSP, false, "Account is not fully configured. Please refer to documentation to setup your account or contact support.", 403, "P0940"},
                new Object[]{AUTHORISATION_API_NOT_ALLOWED, false, "Using authorisation_mode of moto_api is not allowed for this account", 422, "P0195"},
                new Object[]{MISSING_MANDATORY_ATTRIBUTE, true, "An error message from connector", 400, "P0101"},
                new Object[]{UNEXPECTED_ATTRIBUTE, true, "An error message from connector", 400, "P0104"},
                new Object[]{INVALID_ATTRIBUTE_VALUE, true, "An error message from connector", 422, "P0102"}
        };
    }
    
    @ParameterizedTest
    @MethodSource("parametersForMapping")
    void testExceptionMapping(ErrorIdentifier errorIdentifier, boolean messageFromConnector, String expectedDescription, int expectedStatusCode, String expectedErrorCode) {
        List<String> connectorErrorMessages = messageFromConnector ? List.of(expectedDescription) : List.of();
        when(mockResponse.readEntity(ConnectorErrorResponse.class))
                .thenReturn(new ConnectorErrorResponse(errorIdentifier, connectorErrorMessages));

        Response returnedResponse = mapper.toResponse(new CreateChargeException(mockResponse));
        RequestError returnedError = (RequestError) returnedResponse.getEntity();

        assertThat(returnedResponse.getStatus(), is(expectedStatusCode));
        assertThat(returnedError.getDescription(), is(expectedDescription));
        assertThat(returnedError.getCode(), is(expectedErrorCode));
    }
}
