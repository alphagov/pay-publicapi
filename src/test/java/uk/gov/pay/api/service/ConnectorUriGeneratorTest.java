package uk.gov.pay.api.service;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.api.app.config.PublicApiConfig;
import uk.gov.pay.api.auth.Account;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.time.ZonedDateTime;
import java.util.Optional;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.pay.api.model.TokenPaymentType.CARD;
import static uk.gov.pay.api.model.TokenPaymentType.DIRECT_DEBIT;

@RunWith(MockitoJUnitRunner.class)
public class ConnectorUriGeneratorTest {
    @Mock
    private PublicApiConfig mockPublicApiConfig;

    private ConnectorUriGenerator connectorUriGenerator;

    private final Account cardAccount = new Account("accountId", CARD);

    private final String chargeId = "charge_id_123";

    @Before
    public void setUp() {
        connectorUriGenerator = new ConnectorUriGenerator(mockPublicApiConfig);
        when(mockPublicApiConfig.getConnectorUrl()).thenReturn("https://bla.test");
        when(mockPublicApiConfig.getConnectorDDUrl()).thenReturn("https://dd-bla.test");
    }

    @Test
    public void shouldGenerateTheRightChargeURIForCardConnector() {
        String uri = connectorUriGenerator.chargesURI(cardAccount);
        assertThat(uri, is("https://bla.test/v1/api/accounts/accountId/charges"));
    }
    
    @Test
    public void shouldGenerateTheRightRefundURIForCardConnector_withQueryParams() {
        String uri = connectorUriGenerator.refundsURIWithParams(cardAccount, ImmutableMap.of(
                "param1", "value1",
                "param2", "value2"
        ));
        assertThat(uri, is("https://bla.test/v1/api/accounts/accountId/refunds?param1=value1&param2=value2"));
    }

    @Test
    public void shouldGenerateTheRightChargeURIForDirectDebitConnector() {
        Account account = new Account("accountId", DIRECT_DEBIT);
        
        String uri = connectorUriGenerator.chargesURI(account);
        assertThat(uri, is("https://dd-bla.test/v1/api/accounts/accountId/charges/collect"));
    }

    @Test
    public void shouldGenerateTheRightChargeURIWithQueryParams() {
        String uri = connectorUriGenerator.chargesURIWithParams(cardAccount,
                ImmutableMap.of(
                        "param1", "value1",
                        "param2", "value2"
                )
        );
        assertThat(uri, is("https://bla.test/v1/api/accounts/accountId/charges?param1=value1&param2=value2"));
    }

    @Test
    public void shouldGenerateTheRightChargeURIWhenNoQueryParamsArePassed() {
        String uri = connectorUriGenerator.chargesURIWithParams(cardAccount, Maps.newHashMap());
        assertThat(uri, is("https://bla.test/v1/api/accounts/accountId/charges"));
    }

    @Test
    public void shouldGenerateTheRightChargeURI() {
        String uri = connectorUriGenerator.chargeURI(cardAccount, chargeId);
        assertThat(uri, is("https://bla.test/v1/api/accounts/accountId/charges/" + chargeId));
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
    public void buildEventsURIFromBeforeParameter() throws Exception {
        String uri = connectorUriGenerator.eventsURI(cardAccount, Optional.of(ZonedDateTime.parse("2018-03-13T10:00:05.000Z")), Optional.empty(), null, null, null, null);
        assertThat(URLDecoder.decode(uri, "UTF-8"), is("https://bla.test/v1/events?to_date=2018-03-13T10:00:05.000Z&page=1&display_size=500"));
    }

    @Test
    public void buildEventsURIFromAfterParameter() throws UnsupportedEncodingException {
        String uri = connectorUriGenerator.eventsURI(cardAccount, Optional.empty(), Optional.of(ZonedDateTime.parse("2018-03-13T10:00:05.000Z")), null, null, null, null);
        assertThat(URLDecoder.decode(uri, "UTF-8"), is("https://bla.test/v1/events?from_date=2018-03-13T10:00:05.000Z&page=1&display_size=500"));
    }

    @Test
    public void buildEventsURIFromAgreementIdParameter() {
        String uri = connectorUriGenerator.eventsURI(cardAccount, Optional.empty(), Optional.empty(), null, null, "1", null);
        assertThat(uri, is("https://bla.test/v1/events?mandate_external_id=1&page=1&display_size=500"));
    }

    @Test
    public void buildEventsURIFromAllParameters() throws UnsupportedEncodingException {
        String uri = connectorUriGenerator.eventsURI(cardAccount, Optional.of(ZonedDateTime.parse("2018-03-13T10:00:05.000Z")), Optional.of(ZonedDateTime.parse("2018-03-13T10:00:05Z")), 1, 300, "1", "2");
        assertThat(URLDecoder.decode(uri, "UTF-8"), is("https://bla.test/v1/events?to_date=2018-03-13T10:00:05.000Z&from_date=2018-03-13T10:00:05.000Z&mandate_external_id=1&transaction_external_id=2&page=1&display_size=300"));
    }
}
