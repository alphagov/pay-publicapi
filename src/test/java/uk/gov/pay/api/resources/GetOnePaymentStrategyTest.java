package uk.gov.pay.api.resources;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.model.TokenPaymentType;
import uk.gov.pay.api.service.GetPaymentService;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(JUnitParamsRunner.class)
public class GetOnePaymentStrategyTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    private GetPaymentService mockGetPaymentService;

    private GetOnePaymentStrategy getOnePaymentStrategy;
    private String mockPaymentId = "some-payment-id";
    private Account mockAccountId = new Account("some-account-id", TokenPaymentType.CARD);

    @Test
    public void validateAndExecuteUsesLedgerOnlyStrategy() {
        String strategy = "ledger-only";
        getOnePaymentStrategy = new GetOnePaymentStrategy(strategy, mockAccountId, mockPaymentId, mockGetPaymentService);
        getOnePaymentStrategy.validateAndExecute();

        verify(mockGetPaymentService).getLedgerTransaction(mockAccountId, mockPaymentId);
        verify(mockGetPaymentService, never()).getConnectorCharge(mockAccountId, mockPaymentId);
        verify(mockGetPaymentService, never()).getPayment(mockAccountId, mockPaymentId);
    }

    @Test
    public void validateAndExecuteUsesFutureStrategyOnly() {
        String strategy = "future-behaviour";
        getOnePaymentStrategy = new GetOnePaymentStrategy(strategy, mockAccountId, mockPaymentId, mockGetPaymentService);
        getOnePaymentStrategy.validateAndExecute();

        verify(mockGetPaymentService).getPayment(mockAccountId, mockPaymentId);
    }

    @Test
    @Parameters({"", "unknown"})
    public void validateAndExecuteShouldUseConnectorOnlyStrategy(String strategy) {
        getOnePaymentStrategy = new GetOnePaymentStrategy(strategy, mockAccountId, mockPaymentId, mockGetPaymentService);

        getOnePaymentStrategy.validateAndExecute();

        verify(mockGetPaymentService).getConnectorCharge(mockAccountId, mockPaymentId);
        verify(mockGetPaymentService, never()).getLedgerTransaction(mockAccountId, mockPaymentId);
        verify(mockGetPaymentService, never()).getPayment(mockAccountId, mockPaymentId);
    }
}
