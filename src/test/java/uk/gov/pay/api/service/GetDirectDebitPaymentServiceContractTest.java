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
import uk.gov.pay.api.model.DirectDebitPaymentState;
import uk.gov.pay.api.model.TokenPaymentType;
import uk.gov.pay.api.model.directdebit.mandates.DirectDebitPayment;
import uk.gov.pay.commons.testing.pact.consumers.PactProviderRule;
import uk.gov.pay.commons.testing.pact.consumers.Pacts;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GetDirectDebitPaymentServiceContractTest {

    @Rule
    public PactProviderRule connectorRule = new PactProviderRule("direct-debit-connector", this);

    @Mock
    private PublicApiConfig configuration;

    private GetDirectDebitPaymentService getDirectDebitPaymentService;

    @Before
    public void setUp() {
        when(configuration.getConnectorDDUrl()).thenReturn(connectorRule.getUrl());

        when(configuration.getBaseUrl()).thenReturn("http://publicapi.test.localhost/");
        getDirectDebitPaymentService = new GetDirectDebitPaymentService(
                RestClientFactory.buildClient(new RestClientConfig(false)),
                new PublicApiUriGenerator(configuration), new ConnectorUriGenerator(configuration));
    }

    @Test
    @PactVerification("direct-debit-connector")
    @Pacts(pacts = {"publicapi-direct-debit-connector-get-directdebit-payment"}, publish = false)
    public void doGetShouldReturnValidDirectDebitPayment(){
        Account account = new Account("123456", TokenPaymentType.DIRECT_DEBIT);
        
        DirectDebitPayment payment = getDirectDebitPaymentService
                .getDirectDebitPayment(account, "ch_123abc456def");
        
        assertThat(payment.getAmount(), is(1000L));
        assertThat(payment.getCreatedDate(), is("1995-10-27T10:21:01.499Z"));
        assertThat(payment.getDescription(), is("a description"));
        assertThat(payment.getPaymentId(), is("f6q0m125b42cjcndf2joslahen"));
        assertThat(payment.getMandateId(), is("aaaa1111"));
        assertThat(payment.getPaymentProvider(), is("gocardless"));
        assertThat(payment.getReference(), is("ABCDE"));
        assertThat(payment.getProviderId(), is("AAAA1111"));
        assertThat(payment.getState(), is(new DirectDebitPaymentState("pending", false, "payment_state_details")));
    }

}
