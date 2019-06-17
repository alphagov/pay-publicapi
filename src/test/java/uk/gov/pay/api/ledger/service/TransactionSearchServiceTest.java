package uk.gov.pay.api.ledger.service;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.api.app.config.PublicApiConfig;
import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.exception.BadRequestException;
import uk.gov.pay.api.ledger.model.TransactionSearchParams;
import uk.gov.pay.api.model.TokenPaymentType;
import uk.gov.pay.api.service.PaymentUriGenerator;

import javax.ws.rs.client.Client;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.pay.api.matcher.BadRequestExceptionMatcher.aBadRequestExceptionWithError;
@RunWith(MockitoJUnitRunner.class)
public class TransactionSearchServiceTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    @Mock
    private Client mockClient;
    @Mock
    private PublicApiConfig mockPublicApiConfiguration;
    private LedgerUriGenerator ledgerUriGenerator;
    @Mock
    private PaymentUriGenerator mockPaymentUriGenerator;
    
    private TransactionSearchService searchService;
    
    @Before
    public void setUp() {
        ledgerUriGenerator = new LedgerUriGenerator(mockPublicApiConfiguration);
        when(mockPublicApiConfiguration.getBaseUrl()).thenReturn("http://localhost");
        searchService = new TransactionSearchService(mockClient, mockPublicApiConfiguration, ledgerUriGenerator,
                mockPaymentUriGenerator);
    }

    @Test
    public void shouldThrowBadRequestException() {
        expectedException.expect(BadRequestException.class);
        expectedException.expect(aBadRequestExceptionWithError("P0401", "Invalid parameters: not_supported. See Public API documentation for the correct data formats"));
        TransactionSearchParams searchParams = mock(TransactionSearchParams.class);
        when(searchParams.getQueryMap()).thenReturn(Map.of("not_supported", "hello"));
        searchService.doSearch(new Account("1", TokenPaymentType.CARD), searchParams);
    }
}
