package uk.gov.pay.api.service.directdebit;

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
import uk.gov.pay.api.model.directdebit.mandates.MandateState;
import uk.gov.pay.api.model.search.directdebit.SearchMandateConnectorResponse;
import uk.gov.pay.api.model.search.directdebit.SearchMandateResponse;
import uk.gov.pay.api.service.MandatesService;
import uk.gov.pay.api.service.PublicApiUriGenerator;
import uk.gov.pay.commons.testing.pact.consumers.PactProviderRule;
import uk.gov.pay.commons.testing.pact.consumers.Pacts;

import javax.ws.rs.client.Client;

import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.pay.api.model.search.directdebit.DirectDebitSearchMandatesParams.DirectDebitSearchMandatesParamsBuilder.aDirectDebitSearchMandatesParams;

@RunWith(MockitoJUnitRunner.class)
public class DirectDebitMandateSearchServiceTest {

    private static final String MANDATE_ID = "test_mandate_id_xyz";
    private static final String SERVICE_REFERENCE = "test_service_reference";

    @Rule
    public PactProviderRule connectorRule = new PactProviderRule("direct-debit-connector", this);

    @Mock
    private PublicApiConfig configuration;

    DirectDebitMandateSearchService searchService;
    
    @Before
    public void setUp() {
        when(configuration.getConnectorDDUrl()).thenReturn(connectorRule.getUrl());
        when(configuration.getBaseUrl()).thenReturn(connectorRule.getUrl());

        Client client = RestClientFactory.buildClient(new RestClientConfig(false));
        var directDebitConnectorUriGenerator = new DirectDebitConnectorUriGenerator(configuration);
        var publicApiUriGenerator = new PublicApiUriGenerator(configuration);
        searchService = new DirectDebitMandateSearchService(client, directDebitConnectorUriGenerator, publicApiUriGenerator);
    }

    @Test
    @PactVerification({"direct-debit-connector"})
    @Pacts(pacts = {"publicapi-direct-debit-connector-search-mandates"})
    public void shouldSearchSuccessfully() {
        Account account = new Account("9ddfcc27-acf5-43f9-92d5-52247540714c", TokenPaymentType.DIRECT_DEBIT);
        String expectedBankStatementReference = "410104";

        var searchParams = aDirectDebitSearchMandatesParams()
                .withBankStatementReference(expectedBankStatementReference)
                .build();

        SearchMandateResponse searchResponse = searchService.search(account, searchParams);

        assertThat(searchResponse.getCount(), is(1));
        assertThat(searchResponse.getTotal(), is(1));
        assertThat(searchResponse.getPage(), is(1));
        
        assertThat(searchResponse.getMandates().size(), is(1));
    }

    @Test
    @PactVerification({"direct-debit-connector"})
    @Pacts(pacts = {"publicapi-direct-debit-connector-search-mandates"})
    public void shouldGetMandateSearchResponseFromDirectDebitConnector() {
        Account account = new Account("9ddfcc27-acf5-43f9-92d5-52247540714c", TokenPaymentType.DIRECT_DEBIT);
        String expectedBankStatementReference = "410104";
        var expectedMandateLinks = new PaymentConnectorResponseLink(
                "self",
                "http://localhost:1234/v1/api/accounts/9ddfcc27-acf5-43f9-92d5-52247540714c/mandates/" + MANDATE_ID,
                "GET",
                null,
                null
        );

        var searchParams = aDirectDebitSearchMandatesParams()
                .withBankStatementReference(expectedBankStatementReference)
                .build();

        SearchMandateConnectorResponse searchResponse = searchService.getMandatesFromDDConnector(account, searchParams);

        var mandateConnectorResponse = searchResponse.getMandates().get(0);

        assertThat(mandateConnectorResponse.getMandateId(), is(MANDATE_ID));
        assertThat(mandateConnectorResponse.getMandateReference(), is("410104"));
        assertThat(mandateConnectorResponse.getProviderId(), is("MD1234"));
        assertThat(mandateConnectorResponse.getServiceReference(), is(SERVICE_REFERENCE));
        assertThat(mandateConnectorResponse.getReturnUrl(), is("https://example.com/return"));
        assertThat(mandateConnectorResponse.getState(), is(new MandateState("created", false, null)));
        assertThat(mandateConnectorResponse.getCreatedDate(), is("2016-01-01T12:00:00Z"));
        assertThat(mandateConnectorResponse.getPayer().getEmail(), is("payer@example.com"));
        assertThat(mandateConnectorResponse.getPayer().getName(), is("payer"));
        assertThat(mandateConnectorResponse.getLinks().size(), is(1));
        assertThat(mandateConnectorResponse.getLinks().get(0), is(expectedMandateLinks));
        assertThat(mandateConnectorResponse.getLinks().stream().noneMatch(p -> p.getRel().equals("next_url")), is(true));
        assertThat(mandateConnectorResponse.getLinks().stream().noneMatch(p -> p.getRel().equals("next_url_post")), is(true));
    }
}
