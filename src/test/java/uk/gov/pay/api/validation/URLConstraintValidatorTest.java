package uk.gov.pay.api.validation;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.pay.api.config.PublicApiConfig;
import uk.gov.pay.api.config.RestClientConfig;

import javax.validation.ConstraintValidatorContext;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class URLConstraintValidatorTest {

    @Mock
    private RestClientConfig mockRestClientConfig;
    @Mock
    private ConstraintValidatorContext mockContext;

    private URLConstraintValidator validator;

    @Before
    public void setup() {
        PublicApiConfig mockConfiguration = mock(PublicApiConfig.class);
        when(mockConfiguration.getRestClientConfig()).thenReturn(mockRestClientConfig);
        validator = new URLConstraintValidator();
        validator.setConfiguration(mockConfiguration);
    }

    @Test
    public void whenIsDisabledSecureConnection_httpUrlAreValid() throws Exception {

        when(mockRestClientConfig.isDisabledSecureConnection()).thenReturn(true);

        assertThat(validator.isValid("http://my-valid-url.com", mockContext), is(true));
    }

    @Test
    public void whenIsDisabledSecureConnection_httpsUrlsAreValid() throws Exception {

        when(mockRestClientConfig.isDisabledSecureConnection()).thenReturn(true);

        assertThat(validator.isValid("https://my-valid-url.com", mockContext), is(true));
    }

    @Test
    public void whenIsEnabledSecureConnection_httpUrlsAreNotValid() throws Exception {

        when(mockRestClientConfig.isDisabledSecureConnection()).thenReturn(false);

        assertThat(validator.isValid("http://my-valid-url.com", mockContext), is(false));
    }

    @Test
    public void whenIsEnabledSecureConnection_httpsUrlsAreValid() throws Exception {

        when(mockRestClientConfig.isDisabledSecureConnection()).thenReturn(false);

        assertThat(validator.isValid("https://my-valid-url.com", mockContext), is(true));
    }

    @Test
    public void whenUrlIsEmpty_shouldIgnoreValidation() {

        assertThat(validator.isValid("", mockContext), is(true));

        verifyZeroInteractions(mockRestClientConfig);
    }

    @Test
    public void whenUrlIsBlank_shouldFailValidation_whenDisabledSecureConnection() {

        when(mockRestClientConfig.isDisabledSecureConnection()).thenReturn(true);

        assertThat(validator.isValid("   ", mockContext), is(false));
    }

    @Test
    public void whenUrlIsBlank_shouldFailValidation_whenEnabledSecureConnection() {

        when(mockRestClientConfig.isDisabledSecureConnection()).thenReturn(false);

        assertThat(validator.isValid("   ", mockContext), is(false));
    }

    @Test
    public void whenUrlIsNotAnAcceptedProtocol_disabledSecureConnection_shouldFailValidation() {

        when(mockRestClientConfig.isDisabledSecureConnection()).thenReturn(true);

        assertThat(validator.isValid("ftp://ftp.funet.fi/pub/standards/RFC/rfc959.txt", mockContext), is(false));
    }

    @Test
    public void whenUrlIsNotAnAcceptedProtocol_enabledSecureConnection_shouldFailValidation() {

        when(mockRestClientConfig.isDisabledSecureConnection()).thenReturn(false);

        assertThat(validator.isValid("ftp://ftp.funet.fi/pub/standards/RFC/rfc959.txt", mockContext), is(false));
    }
}
