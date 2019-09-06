package uk.gov.pay.api.resources;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import uk.gov.pay.api.app.config.PublicApiConfig;
import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.model.TokenPaymentType;
import uk.gov.pay.api.service.PaymentSearchParams;
import uk.gov.pay.api.service.PaymentSearchService;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(JUnitParamsRunner.class)
public class SearchPaymentsStrategyTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    private PublicApiConfig configuration;

    private PaymentSearchService mockPaymentSearchService = mock(PaymentSearchService.class);
    private SearchPaymentsStrategy searchPaymentsStrategy;

    private PaymentSearchParams paymentSearchParams;
    private Account account = new Account("account-id", TokenPaymentType.CARD);

    @Test
    @Parameters({"ledger-only", "future-behaviour"})
    public void validateAndExecuteShouldUseLedgerOnlyForListedStrategies(String strategy) {
        searchPaymentsStrategy = new SearchPaymentsStrategy(configuration, strategy, account, paymentSearchParams, mockPaymentSearchService);
        searchPaymentsStrategy.validateAndExecute();

        verify(mockPaymentSearchService).searchLedgerPayments(account, paymentSearchParams);
        verify(mockPaymentSearchService, never()).searchConnectorPayments(account, paymentSearchParams);
    }

    @Test
    @Parameters({"", "unknown"})
    public void validateAndExecuteShouldUseConnectorOnlyForDefaultOrUnknownStrategy(String strategy) {
        searchPaymentsStrategy = new SearchPaymentsStrategy(configuration, strategy, account, paymentSearchParams, mockPaymentSearchService);

        searchPaymentsStrategy.validateAndExecute();

        verify(mockPaymentSearchService).searchConnectorPayments(account, paymentSearchParams);
        verify(mockPaymentSearchService, never()).searchLedgerPayments(account, paymentSearchParams);
    }
}
