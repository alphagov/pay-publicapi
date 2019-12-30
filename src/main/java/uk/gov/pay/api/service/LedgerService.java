package uk.gov.pay.api.service;

import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.exception.GetChargeException;
import uk.gov.pay.api.exception.GetEventsException;
import uk.gov.pay.api.exception.GetRefundsException;
import uk.gov.pay.api.exception.GetTransactionException;
import uk.gov.pay.api.exception.SearchPaymentsException;
import uk.gov.pay.api.exception.SearchRefundsException;
import uk.gov.pay.api.ledger.service.LedgerUriGenerator;
import uk.gov.pay.api.model.Charge;
import uk.gov.pay.api.model.TransactionEvents;
import uk.gov.pay.api.model.TransactionResponse;
import uk.gov.pay.api.model.ledger.RefundTransactionFromLedger;
import uk.gov.pay.api.model.ledger.RefundsFromLedger;
import uk.gov.pay.api.model.ledger.SearchRefundsResponseFromLedger;
import uk.gov.pay.api.model.search.card.PaymentSearchResponse;

import javax.inject.Inject;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

import static org.apache.http.HttpStatus.SC_OK;

public class LedgerService {
    private static final String PARAM_ACCOUNT_ID = "account_id";
    private static final String PARAM_TRANSACTION_TYPE = "transaction_type";
    private static final String PARAM_EXACT_REFERENCE_MATCH = "exact_reference_match";
    private static final String PAYMENT_TRANSACTION_TYPE = "PAYMENT";
    private static final String REFUND_TRANSACTION_TYPE = "REFUND";

    private final Client client;
    private final LedgerUriGenerator ledgerUriGenerator;

    @Inject
    public LedgerService(Client client, LedgerUriGenerator ledgerUriGenerator) {
        this.client = client;
        this.ledgerUriGenerator = ledgerUriGenerator;
    }

    public Charge getPaymentTransaction(Account account, String paymentId) {
        Response response = client
                .target(ledgerUriGenerator.transactionURI(account, paymentId, PAYMENT_TRANSACTION_TYPE))
                .request()
                .get();

        if (response.getStatus() == SC_OK) {
            TransactionResponse transactionResponse = response.readEntity(TransactionResponse.class);
            return Charge.from(transactionResponse);
        }

        throw new GetChargeException(response);
    }

    public RefundTransactionFromLedger getRefundTransaction(Account account, String transactionId, String parentExternalId) {
        Response response = client
                .target(ledgerUriGenerator.transactionURI(account, transactionId, REFUND_TRANSACTION_TYPE, parentExternalId))
                .request()
                .get();

        if (response.getStatus() == SC_OK) {
            return response.readEntity(RefundTransactionFromLedger.class);
        }

        throw new GetTransactionException(response);
    }

    public TransactionEvents getTransactionEvents(Account account, String paymentId) {
        Response response = client
                .target(ledgerUriGenerator.transactionEventsURI(account, paymentId))
                .request()
                .get();

        if (response.getStatus() == SC_OK) {
            return response.readEntity(TransactionEvents.class);
        }
        throw new GetEventsException(response);
    }

    public RefundsFromLedger getPaymentRefunds(String accountId, String paymentId) {
        Response response = client
                .target(ledgerUriGenerator.transactionsForTransactionURI(accountId, paymentId))
                .request()
                .get();

        if (response.getStatus() == SC_OK) {
            return response.readEntity(RefundsFromLedger.class);
        }

        throw new GetRefundsException(response);
    }

    public SearchRefundsResponseFromLedger searchRefunds(Account account, Map<String, String> paramsAsMap) {

        paramsAsMap.put(PARAM_ACCOUNT_ID, account.getAccountId());
        paramsAsMap.put(PARAM_TRANSACTION_TYPE, REFUND_TRANSACTION_TYPE);

        Response response = client
                .target(ledgerUriGenerator.transactionsURIWithParams(paramsAsMap))
                .request()
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .get();

        if (response.getStatus() == SC_OK) {
            try {
                return response.readEntity(SearchRefundsResponseFromLedger.class);
            } catch (ProcessingException exception) {
                throw new SearchRefundsException(exception);
            }
        }

        throw new SearchRefundsException(response);
    }

    public PaymentSearchResponse<TransactionResponse> searchPayments(Account account, Map<String, String> paramsAsMap) {

        paramsAsMap.put(PARAM_ACCOUNT_ID, account.getAccountId());
        paramsAsMap.put(PARAM_TRANSACTION_TYPE, PAYMENT_TRANSACTION_TYPE);
        paramsAsMap.put(PARAM_EXACT_REFERENCE_MATCH, "true");

        Response response = client
                .target(ledgerUriGenerator.transactionsURIWithParams(paramsAsMap))
                .request()
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .get();

        if (response.getStatus() == SC_OK) {
            try {
                return response.readEntity(new GenericType<PaymentSearchResponse<TransactionResponse>>() {
                });
            } catch (ProcessingException ex) {
                throw new SearchPaymentsException(ex);
            }
        }

        throw new SearchPaymentsException(response);
    }
}
