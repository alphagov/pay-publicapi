package uk.gov.pay.api.service;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.api.app.config.PublicApiConfig;
import uk.gov.pay.api.auth.Account;

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

    @Before
    public void setUp() {
        connectorUriGenerator  = new ConnectorUriGenerator(mockPublicApiConfig);
        when(mockPublicApiConfig.getConnectorUrl()).thenReturn("https://bla.test");
        when(mockPublicApiConfig.getConnectorDDUrl()).thenReturn("https://dd-bla.test");
    }
    
    @Test
    public void shouldGenerateTheRightChargeURIForCardConnector() {
        Account account = new Account("accountId", CARD);
        String uri = connectorUriGenerator.chargesURI(account, null);
        assertThat(uri, is("https://bla.test/v1/api/accounts/accountId/charges"));
    }

    @Test
    public void shouldGenerateTheRightChargeURIForCardConnectorIfAgreementIdIsPassed() {
        Account account = new Account("accountId", CARD);
        String uri = connectorUriGenerator.chargesURI(account, "shouldntbehere");
        assertThat(uri, is("https://bla.test/v1/api/accounts/accountId/charges"));
    }

    @Test
    public void shouldGenerateTheRightChargeURIForDirectDebitConnector() {
        Account account = new Account("accountId", DIRECT_DEBIT);
        String uri = connectorUriGenerator.chargesURI(account, null);
        assertThat(uri, is("https://dd-bla.test/v1/api/accounts/accountId/charges"));
    }
    
    @Test
    public void shouldGenerateTheRightChargeURIForDirectDebitConnectorIfAgreementIdIsPassed() {
        Account account = new Account("accountId", DIRECT_DEBIT);
        String uri = connectorUriGenerator.chargesURI(account, "agreement_id");
        assertThat(uri, is("https://dd-bla.test/v1/api/accounts/accountId/charges/collect"));
    }
    
    @Test
    public void shouldGenerateTheRightChargeURIWithQueryParams() {
        Account account = new Account("accountId", CARD);
        String uri = connectorUriGenerator.chargesURIWithParams(account, 
                ImmutableList.of(
                        Pair.of("param1", "value1"),
                        Pair.of("param2", "value2")
                )
        );
        assertThat(uri, is("https://bla.test/v1/api/accounts/accountId/charges?param1=value1&param2=value2"));
    }

    @Test
    public void shouldGenerateTheRightChargeURIWhenNoQueryParamsArePassed() {
        Account account = new Account("accountId", CARD);
        String uri = connectorUriGenerator.chargesURIWithParams(account, new ArrayList<>());
        assertThat(uri, is("https://bla.test/v1/api/accounts/accountId/charges"));
    }

    @Test
    public void shouldGenerateTheRightChargeURI() {
        Account account = new Account("accountId", CARD);
        String uri = connectorUriGenerator.chargeURI(account, "charge_id");
        assertThat(uri, is("https://bla.test/v1/api/accounts/accountId/charges/charge_id"));
    }
    
    @Test
    public void shouldGenerateTheRightCancelURI() {
        Account account = new Account("accountId", CARD);
        String uri = connectorUriGenerator.cancelURI(account, "charge_id");
        assertThat(uri, is("https://bla.test/v1/api/accounts/accountId/charges/charge_id/cancel"));
    }

    @Test
    public void shouldGenerateTheRightEventsURI() {
        Account account = new Account("accountId", CARD);
        String uri = connectorUriGenerator.chargeEventsURI(account, "charge_id");
        assertThat(uri, is("https://bla.test/v1/api/accounts/accountId/charges/charge_id/events"));
    }
}
