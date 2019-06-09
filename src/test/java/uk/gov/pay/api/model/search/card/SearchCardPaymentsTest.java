package uk.gov.pay.api.model.search.card;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.jackson.Jackson;
import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.api.app.config.PublicApiConfig;
import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.model.Address;
import uk.gov.pay.api.model.card.ConnectorSearchResponse;
import uk.gov.pay.api.model.card.SearchCardPayments;
import uk.gov.pay.api.model.response.CardDetails;
import uk.gov.pay.api.service.ConnectorUriGenerator;
import uk.gov.pay.api.service.PaymentUriGenerator;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.pay.api.it.fixtures.PaginatedPaymentSearchResultFixture.aPaginatedPaymentSearchResult;
import static uk.gov.pay.api.it.fixtures.PaymentSearchResultBuilder.aSuccessfulSearchPayment;

@RunWith(MockitoJUnitRunner.class)
public class SearchCardPaymentsTest {
    private static final ObjectMapper MAPPER = Jackson.newObjectMapper();

    public Client mockClient;
    @Mock public PublicApiConfig configuration;
    @Mock public ConnectorUriGenerator connectorUriGenerator;
    @Mock public PaymentUriGenerator paymentUriGenerator;
    @Mock public Account account;
    
    private static final CardDetails CARD_DETAILS = new CardDetails(
            "1234",
            "876543",
            "Ms Cardholder",
            "12/19",
            new Address(
                    "line1",
                    "line2",
                    "NR2 5 6EG",
                    "city",
                    "UK"),
            "Mastercard");

    @Before
    public void setup() throws Exception {
        
        final String result = aPaginatedPaymentSearchResult()
                .withCount(10)
                .withPage(2)
                .withTotal(20)
                .withPayments(aSuccessfulSearchPayment()
                        .withCardDetails(CARD_DETAILS)
                        .getResults())
                .build();
        

        final ConnectorSearchResponse response = MAPPER.readValue(result, ConnectorSearchResponse.class);

        mockClient = mock(Client.class, RETURNS_DEEP_STUBS);

        when(mockClient.target(Mockito.anyString())
                .request()
                .header(Mockito.anyString(), Mockito.any())
                .get(ConnectorSearchResponse.class)
        ).thenReturn(response);
    }

    @Test
    public void serializesToJSON() throws Exception {
        final SearchCardPayments s = new SearchCardPayments(
                mockClient,
                configuration,
                connectorUriGenerator,
                paymentUriGenerator,
                MAPPER);

        Map<String, String> queryParams = Map.of("last_digits_card_number", "1234");
        final Response results = s.getSearchResponse(account, queryParams);
        
        assertThat(MAPPER.writeValueAsString(results), Is.is("{}"));
    }
}
