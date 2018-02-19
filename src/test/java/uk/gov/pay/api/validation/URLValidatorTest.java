package uk.gov.pay.api.validation;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.gov.pay.api.validation.URLValidator.*;

public class URLValidatorTest {

    @Test
    public void shouldReturnExpectedURLValidatorWhenDisabledSecureConnectionIsTrue() {
        assertThat(urlValidatorValueOf(true), is(SECURITY_DISABLED));
    }

    @Test
    public void shouldReturnExpectedURLValidatorWhenDisabledSecureConnectionIsFalse() {
        assertThat(urlValidatorValueOf(false), is(SECURITY_ENABLED));
    }

    @Test
    public void testSupportsExpectedValidators() {

        List validators = Arrays.asList(URLValidator.values());

        assertThat(validators.size(), is(2));
        assertThat(validators.contains(SECURITY_ENABLED), is(true));
        assertThat(validators.contains(SECURITY_DISABLED), is(true));
    }

    @Test
    public void whenIsDisabledSecureConnection_httpUrlAreValid() throws Exception {
        assertThat(SECURITY_DISABLED.isValid("http://my-valid-url.com"), is(true));
    }

    @Test
    public void whenIsDisabledSecureConnection_allowingLocalUrl() throws Exception {
        assertThat(SECURITY_DISABLED.isValid("http://localhost:8080/pay"), is(true));
    }

    @Test
    public void whenIsDisabledSecureConnection_httpsUrlsAreValid() throws Exception {
        assertThat(SECURITY_DISABLED.isValid("https://my-valid-url.com"), is(true));
    }

    @Test
    public void whenIsEnabledSecureConnection_httpUrlsAreNotValid() throws Exception {
        assertThat(SECURITY_ENABLED.isValid("http://my-valid-url.com"), is(false));
    }

    @Test
    public void whenIsEnabledSecureConnection_httpsUrlsAreValid() throws Exception {
        assertThat(SECURITY_ENABLED.isValid("https://my-valid-url.com"), is(true));
    }

    @Test
    public void whenIsEnabledSecureConnection_allowingLocalUrl() throws Exception {
        assertThat(SECURITY_ENABLED.isValid("https://localhost:8080/pay"), is(true));
    }

    @Test
    public void whenIsEnabledSecureConnection_allowingLocalUrl_shouldFailForHttp() throws Exception {
        assertThat(SECURITY_ENABLED.isValid("http://localhost:8080/pay"), is(false));
    }

    @Test
    public void whenIsEnabledSecureConnection_allowingInternalDomains() {
        assertThat(SECURITY_ENABLED.isValid("https://staging.service.core.internal/claim/pay/id/receiver"), is(true));
    }

    @Test
    public void whenIsEnabledSecureConnection_disallowingEvilDomains() {
        assertThat(SECURITY_ENABLED.isValid("https://an.evil/claim/pay/id/receiver"), is(false));
    }

    @Test
    public void whenUrlIsBlank_shouldFailValidation_whenDisabledSecureConnection() {
        assertThat(SECURITY_DISABLED.isValid("   "), is(false));
    }

    @Test
    public void whenUrlIsBlank_shouldFailValidation_whenEnabledSecureConnection() {
        assertThat(SECURITY_ENABLED.isValid("   "), is(false));
    }

    @Test
    public void whenUrlIsNotAnAcceptedProtocol_disabledSecureConnection_shouldFailValidation() {
        assertThat(SECURITY_DISABLED.isValid("ftp://ftp.funet.fi/pub/standards/RFC/rfc959.txt"), is(false));
    }

    @Test
    public void whenUrlIsNotAnAcceptedProtocol_enabledSecureConnection_shouldFailValidation() {
        assertThat(SECURITY_ENABLED.isValid("ftp://ftp.funet.fi/pub/standards/RFC/rfc959.txt"), is(false));
    }
}

