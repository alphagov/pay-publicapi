package uk.gov.pay.api.service;

import au.com.dius.pact.consumer.PactVerification;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.api.app.RestClientFactory;
import uk.gov.pay.api.app.config.PublicApiConfig;
import uk.gov.pay.api.app.config.RestClientConfig;
import uk.gov.pay.api.auth.Account;
import uk.gov.pay.commons.testing.pact.consumers.PactProviderRule;
import uk.gov.pay.commons.testing.pact.consumers.Pacts;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.pay.commons.model.TokenPaymentType.CARD;

@RunWith(MockitoJUnitRunner.class)
public class CancelPaymentServiceTest {
    
    private final String CHARGE_ID = "charge8133029783750964639";
    private final Account ACCOUNT = new Account("123456", CARD);
    
    private CancelPaymentService cancelPaymentService;
    
    @Rule
    public PactProviderRule connectorRule = new PactProviderRule("connector", this);

    @Mock
    private PublicApiConfig mockConfiguration;
    
    @Before
    public void setUp() {
        when(mockConfiguration.getConnectorUrl()).thenReturn(connectorRule.getUrl());

        ConnectorUriGenerator connectorUriGenerator = new ConnectorUriGenerator(mockConfiguration);
        Client client = RestClientFactory.buildClient(new RestClientConfig(false));
        cancelPaymentService = new CancelPaymentService(client, connectorUriGenerator);
    }

    @Test
    @PactVerification({"connector"})
    @Pacts(pacts = {"publicapi-connector-cancel-payment-with-created-state"})
    public void cancelAPaymentWithCreatedState() {
        
        Response cancelPaymentResponse = cancelPaymentService.cancel(ACCOUNT, CHARGE_ID);

        assertThat(cancelPaymentResponse.getStatus(), is(204));
    }
}
