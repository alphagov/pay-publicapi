package uk.gov.pay.api.service;

import au.com.dius.pact.consumer.PactVerification;
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
import uk.gov.pay.api.ledger.service.LedgerUriGenerator;
import uk.gov.pay.api.model.TokenPaymentType;
import uk.gov.pay.api.model.search.PaginationDecorator;
import uk.gov.pay.commons.testing.pact.consumers.PactProviderRule;
import uk.gov.pay.commons.testing.pact.consumers.Pacts;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CardPaymentSearchServiceTest {

    @Rule
    public PactProviderRule ledgerRule = new PactProviderRule("ledger", this);

    @Mock
    private PublicApiConfig configuration;
    private PaymentSearchService paymentSearchService;

    @Before
    public void setUp() {
        when(configuration.getLedgerUrl()).thenReturn(ledgerRule.getUrl());

        when(configuration.getBaseUrl()).thenReturn("http://publicapi.test.localhost/");

        Client client = RestClientFactory.buildClient(new RestClientConfig(false));
        LedgerUriGenerator ledgerUriGenerator = new LedgerUriGenerator(configuration);
        paymentSearchService = new PaymentSearchService(
                new PublicApiUriGenerator(configuration),
                new PaginationDecorator(configuration),
                new LedgerService(client, ledgerUriGenerator));
    }

    @Test
    @PactVerification({"ledger"})
    @Pacts(pacts = {"publicapi-ledger-search-payment-by-cardholder-name"})
    public void searchShouldReturnAResponseWithOneTransaction_whenFilteringByCardHolderNameFromLedger() {
        Account account = new Account("123456", TokenPaymentType.CARD);
        var searchParams = new PaymentSearchParams.Builder()
                .withCardHolderName("J Doe")
                .withPageNumber("1")
                .withDisplaySize("500")
                .build();
        Response response = paymentSearchService.searchLedgerPayments(account, searchParams);
        JsonAssert.with(response.getEntity().toString())
                .assertThat("count", is(1))
                .assertThat("total", is(1))
                .assertThat("page", is(1))
                .assertThat("results", hasSize(equalTo(1)))
                .assertThat("results[0]", hasKey("amount"))
                .assertThat("results[0]", hasKey("state"))
                .assertThat("results[0]", hasKey("reference"))
                .assertThat("results[0]", hasKey("email"))
                .assertThat("results[0].card_details.cardholder_name", is("J Doe"))
                .assertThat("results[0].card_details", hasKey("first_digits_card_number"))
                .assertThat("results[0].card_details", hasKey("last_digits_card_number"))
                .assertThat("results[0].state", hasKey("status"))
                .assertThat("results[0].state", hasKey("finished"));
    }

    @Test
    @PactVerification({"ledger"})
    @Pacts(pacts = {"publicapi-ledger-search-payment-by-first-digits-card-number"})
    public void searchShouldReturnAResponseWithOneTransaction_whenFilteringByFirstDigitsCardNumberFromLedger() {
        Account account = new Account("123456", TokenPaymentType.CARD);
        var searchParams = new PaymentSearchParams.Builder()
                .withFirstDigitsCardNumber("424242")
                .withPageNumber("1")
                .withDisplaySize("500")
                .build();
        Response response = paymentSearchService.searchLedgerPayments(account, searchParams);
        JsonAssert.with(response.getEntity().toString())
                .assertThat("count", is(1))
                .assertThat("total", is(1))
                .assertThat("page", is(1))
                .assertThat("results", hasSize(equalTo(1)))
                .assertThat("results[0]", hasKey("amount"))
                .assertThat("results[0]", hasKey("state"))
                .assertThat("results[0]", hasKey("reference"))
                .assertThat("results[0]", hasKey("email"))
                .assertThat("results[0].card_details.cardholder_name", is("J Doe"))
                .assertThat("results[0].card_details", hasKey("first_digits_card_number"))
                .assertThat("results[0].card_details", hasKey("last_digits_card_number"))
                .assertThat("results[0].state", hasKey("status"))
                .assertThat("results[0].state", hasKey("finished"));
    }

    @Test
    @PactVerification({"ledger"})
    @Pacts(pacts = {"publicapi-ledger-search-payment-by-last-digits-card-number"})
    public void searchShouldReturnAResponseWithOneTransaction_whenFilteringByLastDigitsCardNumberFromLedger() {
        Account account = new Account("123456", TokenPaymentType.CARD);
        var searchParams = new PaymentSearchParams.Builder()
                .withLastDigitsCardNumber("4242")
                .withPageNumber("1")
                .withDisplaySize("500")
                .build();
        Response response = paymentSearchService.searchLedgerPayments(account, searchParams);
        JsonAssert.with(response.getEntity().toString())
                .assertThat("count", is(1))
                .assertThat("total", is(1))
                .assertThat("page", is(1))
                .assertThat("results", hasSize(equalTo(1)))
                .assertThat("results[0]", hasKey("amount"))
                .assertThat("results[0]", hasKey("state"))
                .assertThat("results[0]", hasKey("reference"))
                .assertThat("results[0]", hasKey("email"))
                .assertThat("results[0].card_details.cardholder_name", is("J Doe"))
                .assertThat("results[0].card_details", hasKey("first_digits_card_number"))
                .assertThat("results[0].card_details", hasKey("last_digits_card_number"))
                .assertThat("results[0].state", hasKey("status"))
                .assertThat("results[0].state", hasKey("finished"));
    }

    @Test
    @PactVerification({"ledger"})
    @Pacts(pacts = {"publicapi-ledger-search-payment-with-charge-in-success-state"})
    public void searchShouldReturnAResponseWithOneTransaction_whenFilteringByStateFromLedger() {
        Account account = new Account("123456", TokenPaymentType.CARD);
        var searchParams = new PaymentSearchParams.Builder()
                .withState("success")
                .withPageNumber("1")
                .withDisplaySize("500")
                .build();
        Response response = paymentSearchService.searchLedgerPayments(account, searchParams);
        JsonAssert.with(response.getEntity().toString())
                .assertThat("count", is(1))
                .assertThat("total", is(1))
                .assertThat("page", is(1))
                .assertThat("results", hasSize(equalTo(1)))
                .assertThat("results[0]", hasKey("amount"))
                .assertThat("results[0]", hasKey("state"))
                .assertThat("results[0]", hasKey("reference"))
                .assertThat("results[0]", hasKey("email"))
                .assertThat("results[0].card_details.cardholder_name", is("J Doe"))
                .assertThat("results[0].card_details", hasKey("first_digits_card_number"))
                .assertThat("results[0].card_details", hasKey("last_digits_card_number"))
                .assertThat("results[0].state", hasKey("status"))
                .assertThat("results[0].state", hasKey("finished"));
    }

    @Test
    @PactVerification({"ledger"})
    @Pacts(pacts = {"publicapi-ledger-search-payments"})
    public void ledgerSearchShouldReturnAResponseWithOneTransaction() {
        Account account = new Account("123456", TokenPaymentType.CARD);
        var searchParams = new PaymentSearchParams.Builder()
                .withCardHolderName("j.doe@example.org")
                .build();

        Response response = paymentSearchService.searchLedgerPayments(account, searchParams);
        JsonAssert.with(response.getEntity().toString())
                .assertThat("count", is(1))
                .assertThat("total", is(1))
                .assertThat("page", is(1))
                .assertThat("results", hasSize(equalTo(1)))
                .assertThat("results[0]", hasKey("amount"))
                .assertThat("results[0]", hasKey("state"))
                .assertThat("results[0]", hasKey("reference"))
                .assertThat("results[0]", hasKey("email"))
                .assertThat("results[0].card_details.cardholder_name", is("j.doe@example.org"))
                .assertThat("results[0].card_details", hasKey("first_digits_card_number"))
                .assertThat("results[0].card_details", hasKey("last_digits_card_number"))
                .assertThat("results[0].state", hasKey("status"))
                .assertThat("results[0].state", hasKey("finished"));
    }
}
