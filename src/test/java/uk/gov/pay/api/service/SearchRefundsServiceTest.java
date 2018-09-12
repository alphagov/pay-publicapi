package uk.gov.pay.api.service;

import au.com.dius.pact.consumer.PactVerification;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonassert.JsonAssert;
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
import uk.gov.pay.api.model.TokenPaymentType;
import uk.gov.pay.commons.testing.pact.consumers.PactProviderRule;
import uk.gov.pay.commons.testing.pact.consumers.Pacts;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SearchRefundsServiceTest {

    @Rule
    public PactProviderRule connectorRule = new PactProviderRule("connector", this);

    @Mock
    private PublicApiConfig publicApiConfig;

    private SearchRefundsService searchRefundsService;

    @Before
    public void setUp() {
        when(publicApiConfig.getConnectorUrl()).thenReturn(connectorRule.getUrl());
        when(publicApiConfig.getBaseUrl()).thenReturn("http://publicapi.test.localhost/");

        ConnectorUriGenerator uriGenerator = new ConnectorUriGenerator(publicApiConfig);

        Client client = RestClientFactory.buildClient(new RestClientConfig(false));
        ObjectMapper objectMapper = new ObjectMapper();
        searchRefundsService = new SearchRefundsService(client, publicApiConfig, uriGenerator, objectMapper);
    }

    @Test
    @PactVerification({"connector"})
    @Pacts(pacts = {"publicapi-connector-search-refunds-with-page-and-display"})
    public void getAllRefundsShouldReturnCorrectTotalAndPageAndDisplaySize() {
        String accountId = "777";
        String extChargeId = "someExternalId";
        String page = "1";
        String displaySize = "2";
        Account account = new Account(accountId, TokenPaymentType.CARD);
        Response response = searchRefundsService.getAllRefunds(account, page, displaySize);

        JsonAssert.with(response.getEntity().toString())
                .assertThat("$.results.*", hasSize(2))
                .assertThat("count", is(2))
                .assertThat("total", is(2))
                .assertThat("page", is(1))
                
                .assertThat("$.results[0].status", is("available"))
                .assertThat("$.results[0].created_date", is("2017-10-01T01:41:01Z"))
                .assertThat("$.results[0].refund_id", is("111111"))
                .assertThat("$.results[0].charge_id", is("99999"))
                .assertThat("$.results[0].amount_submitted", is(98))
                .assertThat("$.results[0].status", is("available"))
                .assertThat("$.results[0].links[0].rel", is("self"))
                .assertThat("$.results[0].links[1].rel", is("payment_url"))
                .assertThat("$.results[0].links[0].href", is("http://app.com/v1/refunds/account/{accountId}"
                        .replace("{accountId}", String.valueOf(accountId))))
                .assertThat("$.results[0].links[1].href", is("http://app.com/v1/payments/{chargeId}"
                        .replace("{chargeId}", extChargeId)))
                
                .assertThat("$.results[1].status", is("available"))
                .assertThat("$.results[1].created_date", is("2017-09-02T02:42:02Z"))
                .assertThat("$.results[1].refund_id", is("222222"))
                .assertThat("$.results[1].charge_id", is("88888"))
                .assertThat("$.results[1].amount_submitted", is(100))
                .assertThat("$.results[1].status", is("available"))
                .assertThat("$.results[1].links[0].rel", is("self"))
                .assertThat("$.results[1].links[1].rel", is("payment_url"))
                .assertThat("$.results[1].links[0].href", is("http://app.com/v1/refunds/account/{accountId}"
                        .replace("{accountId}", String.valueOf(accountId))))
                .assertThat("$.results[1].links[1].href", is("http://app.com/v1/payments/{chargeId}"
                        .replace("{chargeId}", extChargeId)))
                
                .assertThat("$._links.self.href", is("http://publicapi.test.localhost/v1/refunds/account/"
                        + accountId + "?page=" + page + "&display_size=" + displaySize));
    }

    @Test
    @PactVerification({"connector"})
    @Pacts(pacts = {"publicapi-connector-search-refunds-with-page-and-display-when-no-refunds-exist"})
    public void getAllRefundsShouldReturnNoRefundsWhenThereAreNone() {
        Account account = new Account("888", TokenPaymentType.CARD);

        Response response = searchRefundsService.getAllRefunds(account, "1", "1");
        JsonAssert.with(response.getEntity().toString())
                .assertThat("count", is(0))
                .assertThat("total", is(0))
                .assertThat("page", is(1));
    }

}
