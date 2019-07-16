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
import uk.gov.pay.api.model.PaymentConnectorResponseLink;
import uk.gov.pay.api.model.TokenPaymentType;
import uk.gov.pay.api.model.directdebit.mandates.MandateConnectorRequest;
import uk.gov.pay.api.model.directdebit.mandates.MandateConnectorResponse;
import uk.gov.pay.api.model.directdebit.mandates.MandateState;
import uk.gov.pay.api.service.directdebit.DirectDebitConnectorUriGenerator;
import uk.gov.pay.commons.testing.pact.consumers.PactProviderRule;
import uk.gov.pay.commons.testing.pact.consumers.Pacts;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import java.util.Collections;

import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MandatesServiceTest {

    private static final String MANDATE_ID = "test_mandate_id_xyz";
    private static final String SERVICE_REFERENCE = "test_service_reference";

    private MandatesService mandatesService;

    @Rule
    public PactProviderRule ddConnectorRule = new PactProviderRule("direct-debit-connector", this);

    @Mock
    private PublicApiConfig mockConfiguration;
    
    @Mock
    private PublicApiUriGenerator mockPublicApiUriGenerator;

    @Before
    public void setup() {
        // We will actually send real requests here, which will be intercepted by pact
        when(mockConfiguration.getConnectorDDUrl()).thenReturn(ddConnectorRule.getUrl());
        Client client = RestClientFactory.buildClient(new RestClientConfig(false));
        mandatesService = new MandatesService(client, mockPublicApiUriGenerator, new DirectDebitConnectorUriGenerator(mockConfiguration));
    }
    
    @Test
    @PactVerification({"direct-debit-connector"})
    @Pacts(pacts = {"publicapi-direct-debit-connector-create-mandate"})
    public void shouldCreateAMandateSuccessfully() {
        Account account = new Account("7959d395-e720-4081-9e8a-a534cf76460a", TokenPaymentType.DIRECT_DEBIT);
        MandateConnectorRequest mandateConnectorRequest = new MandateConnectorRequest(
                "https://example.com/return",
                SERVICE_REFERENCE,
                "raindrops on roses and whiskers on kittens"
        );
        MandateConnectorResponse mandate = mandatesService.createMandate(account, mandateConnectorRequest);

        assertThat(mandate.getMandateId(), is(MANDATE_ID));
        assertThat(mandate.getMandateReference(), is(nullValue()));
        assertThat(mandate.getServiceReference(), is(SERVICE_REFERENCE));
        assertThat(mandate.getDescription(), is("raindrops on roses and whiskers on kittens"));
        assertThat(mandate.getCreatedDate(), is("2016-01-01T12:00:00.000Z"));
        assertThat(mandate.getReturnUrl(), is("https://example.com/return"));
        assertThat(mandate.getState(), is(new MandateState("created", false)));
        assertThat(mandate.getPaymentProvider(), is("sandbox"));
        assertThat(mandate.getLinks().get(0), is(new PaymentConnectorResponseLink(
                "self",
                "http://localhost:1234/v1/api/accounts/7959d395-e720-4081-9e8a-a534cf76460a/mandates/" + MANDATE_ID,
                "GET",
                null,
                null
        )));
        assertThat(mandate.getLinks().get(1), is(new PaymentConnectorResponseLink(
                "next_url",
                "http://frontend_direct_debit/secure/token_1234567asdf",
                "GET",
                null,
                null
        )));
        assertThat(mandate.getLinks().get(2), is(new PaymentConnectorResponseLink(
                "next_url_post",
                "http://frontend_direct_debit/secure/",
                "POST",
                "application/x-www-form-urlencoded",
                Collections.singletonMap("chargeTokenId", "token_1234567asdf")
        )));
    }

    @Test
    @PactVerification({"direct-debit-connector"})
    @Pacts(pacts = {"publicapi-direct-debit-connector-get-mandate"})
    public void shouldGetAMandateSuccessfully() {
        Account account = new Account("9ddfcc27-acf5-43f9-92d5-52247540714c", TokenPaymentType.DIRECT_DEBIT);
        Response response = mandatesService.getMandate(account, MANDATE_ID);
        MandateConnectorResponse mandateConnectorResponse = response.readEntity(MandateConnectorResponse.class);

        assertThat(mandateConnectorResponse.getMandateId(), is(MANDATE_ID));
        assertThat(mandateConnectorResponse.getMandateReference(), is("410104"));
        assertThat(mandateConnectorResponse.getProviderId(), is("MD1234"));
        assertThat(mandateConnectorResponse.getServiceReference(), is(SERVICE_REFERENCE));
        assertThat(mandateConnectorResponse.getReturnUrl(), is("https://example.com/return"));
        assertThat(mandateConnectorResponse.getState(), is(new MandateState("created", false, "example details")));
        assertThat(mandateConnectorResponse.getCreatedDate(), is("2016-01-01T12:00:00Z"));
        assertThat(mandateConnectorResponse.getPayer().getEmail(), is("i.died@titanic.com"));
        assertThat(mandateConnectorResponse.getPayer().getName(), is("Jack"));
        assertThat(mandateConnectorResponse.getLinks().size(), is(1));
        assertThat(mandateConnectorResponse.getLinks().get(0), is(new PaymentConnectorResponseLink(
                "self",
                "http://localhost:1234/v1/api/accounts/9ddfcc27-acf5-43f9-92d5-52247540714c/mandates/" + MANDATE_ID,
                "GET",
                null,
                null
        )));
        assertThat(mandateConnectorResponse.getLinks().stream().noneMatch(p -> p.getRel().equals("next_url")), is(true));
        assertThat(mandateConnectorResponse.getLinks().stream().noneMatch(p -> p.getRel().equals("next_url_post")), is(true));
    }
}
