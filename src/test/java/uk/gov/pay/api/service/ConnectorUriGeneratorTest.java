package uk.gov.pay.api.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.pay.api.app.config.PublicApiConfig;
import uk.gov.pay.api.auth.Account;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;
import static uk.gov.pay.api.model.TokenPaymentType.CARD;

@ExtendWith(MockitoExtension.class)
public class ConnectorUriGeneratorTest {
    @Mock
    private PublicApiConfig mockPublicApiConfig;

    private ConnectorUriGenerator connectorUriGenerator;

    private final Account cardAccount = new Account("accountId", CARD, "a-token-link");

    private final String chargeId = "charge_id_123";

    @BeforeEach
    public void setUp() {
        connectorUriGenerator = new ConnectorUriGenerator(mockPublicApiConfig);
        when(mockPublicApiConfig.getConnectorUrl()).thenReturn("https://bla.test");
    }

    @Test
    public void shouldGenerateTheRightChargeURIForCardConnector() {
        String uri = connectorUriGenerator.chargesURI(cardAccount);
        assertThat(uri, is("https://bla.test/v1/api/accounts/accountId/charges"));
    }

    @Test
    public void shouldGenerateTheRightChargeURI() {
        String uri = connectorUriGenerator.chargeURI(cardAccount, chargeId);
        assertThat(uri, is("https://bla.test/v1/api/accounts/accountId/charges/" + chargeId));
    }

    @Test
    public void shouldGenerateTheRightTelephoneChargeURI() {
        String uri = connectorUriGenerator.telephoneChargesURI(cardAccount);
        assertThat(uri, is("https://bla.test/v1/api/accounts/accountId/telephone-charges"));
    }

    @Test
    public void shouldGenerateTheRightCancelURI() {
        String uri = connectorUriGenerator.cancelURI(cardAccount, chargeId);
        assertThat(uri, is("https://bla.test/v1/api/accounts/accountId/charges/" + chargeId + "/cancel"));
    }

    @Test
    public void shouldGenerateTheRightCaptureURI() {
        String uri = connectorUriGenerator.captureURI(cardAccount, chargeId);
        assertThat(uri, is("https://bla.test/v1/api/accounts/" + cardAccount.getAccountId() + "/charges/" + chargeId + "/capture"));
    }

    @Test
    public void shouldGenerateTheRightEventsURI() {
        String uri = connectorUriGenerator.chargeEventsURI(cardAccount, chargeId);
        assertThat(uri, is("https://bla.test/v1/api/accounts/accountId/charges/" + chargeId + "/events"));
    }

    @Test
    public void buildEventsURIFromBeforeParameter() {
        String uri = connectorUriGenerator.eventsURI(cardAccount, Optional.of(ZonedDateTime.parse("2018-03-13T10:00:05.000Z")), Optional.empty(), null, null, null, null);
        assertThat(URLDecoder.decode(uri, StandardCharsets.UTF_8), is("https://bla.test/v1/events?to_date=2018-03-13T10:00:05.000Z&page=1&display_size=500"));
    }

    @Test
    public void buildEventsURIFromAfterParameter() {
        String uri = connectorUriGenerator.eventsURI(cardAccount, Optional.empty(), Optional.of(ZonedDateTime.parse("2018-03-13T10:00:05.000Z")), null, null, null, null);
        assertThat(URLDecoder.decode(uri, StandardCharsets.UTF_8), is("https://bla.test/v1/events?from_date=2018-03-13T10:00:05.000Z&page=1&display_size=500"));
    }

    @Test
    public void buildEventsURIFromAgreementIdParameter() {
        String uri = connectorUriGenerator.eventsURI(cardAccount, Optional.empty(), Optional.empty(), null, null, "1", null);
        assertThat(uri, is("https://bla.test/v1/events?mandate_external_id=1&page=1&display_size=500"));
    }

    @Test
    public void buildEventsURIFromAllParameters() {
        String uri = connectorUriGenerator.eventsURI(cardAccount, Optional.of(ZonedDateTime.parse("2018-03-13T10:00:05.000Z")), Optional.of(ZonedDateTime.parse("2018-03-13T10:00:05Z")), 1, 300, "1", "2");
        assertThat(URLDecoder.decode(uri, StandardCharsets.UTF_8), is("https://bla.test/v1/events?to_date=2018-03-13T10:00:05.000Z&from_date=2018-03-13T10:00:05.000Z&mandate_external_id=1&payment_external_id=2&page=1&display_size=300"));
    }
}
