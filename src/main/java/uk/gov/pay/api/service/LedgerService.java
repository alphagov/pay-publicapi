package uk.gov.pay.api.service;

import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.exception.GetChargeException;
import uk.gov.pay.api.exception.GetEventsException;
import uk.gov.pay.api.ledger.service.LedgerUriGenerator;
import uk.gov.pay.api.model.Charge;
import uk.gov.pay.api.model.TransactionEvents;
import uk.gov.pay.api.model.TransactionResponse;

import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;

import static org.apache.http.HttpStatus.SC_OK;

public class LedgerService {
    private final Client client;
    private final LedgerUriGenerator ledgerUriGenerator;

    @Inject
    public LedgerService(Client client, LedgerUriGenerator ledgerUriGenerator) {
        this.client = client;
        this.ledgerUriGenerator = ledgerUriGenerator;
    }

    public Charge getTransaction(Account account, String paymentId) {
        Response response = client
                .target(ledgerUriGenerator.transactionURI(account, paymentId))
                .request()
                .get();

        if (response.getStatus() == SC_OK) {
            TransactionResponse transactionResponse = response.readEntity(TransactionResponse.class);
            return Charge.from(transactionResponse);
        }
        
        throw new GetChargeException(response);
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
}
