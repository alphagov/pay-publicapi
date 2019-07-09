package uk.gov.pay.api.service;

import uk.gov.pay.api.app.config.PublicApiConfig;
import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.exception.GetChargeException;
import uk.gov.pay.api.ledger.service.LedgerUriGenerator;
import uk.gov.pay.api.model.Charge;
import uk.gov.pay.api.model.ChargeFromResponse;
import uk.gov.pay.api.model.TransactionResponse;
import uk.gov.pay.api.model.links.PaymentWithAllLinks;

import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import java.net.URI;

import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.apache.http.HttpStatus.SC_OK;

public class GetPaymentService {

    private final Client client;
    private final PublicApiUriGenerator publicApiUriGenerator;
    private final ConnectorUriGenerator connectorUriGenerator;
    private final LedgerUriGenerator ledgerUriGenerator;
    private final PublicApiConfig config;

    @Inject
    public GetPaymentService(Client client, PublicApiUriGenerator publicApiUriGenerator,
                             ConnectorUriGenerator connectorUriGenerator, LedgerUriGenerator ledgerUriGenerator,
                             PublicApiConfig config) {
        this.client = client;
        this.publicApiUriGenerator = publicApiUriGenerator;
        this.connectorUriGenerator = connectorUriGenerator;
        this.ledgerUriGenerator = ledgerUriGenerator;
        this.config = config;
    }

    public PaymentWithAllLinks getPayment(Account account, String paymentId) {
        Response response = client
                .target(connectorUriGenerator.chargeURI(account, paymentId))
                .request()
                .get();

        if (response.getStatus() == SC_OK) {
            ChargeFromResponse chargeFromResponse = response.readEntity(ChargeFromResponse.class);
            return getPaymentWithAllLinks(account, Charge.from(chargeFromResponse));
        }

        if(response.getStatus() == SC_NOT_FOUND && config.getUseLedgerForGetPayment()) {
            response = client
                    .target(ledgerUriGenerator.transactionURI(paymentId))
                    .request()
                    .get();

            if (response.getStatus() == SC_OK) {
                TransactionResponse transactionResponse = response.readEntity(TransactionResponse.class);
                return getPaymentWithAllLinks(account, Charge.from(transactionResponse));
            }
        }

        throw new GetChargeException(response);
    }

    private PaymentWithAllLinks getPaymentWithAllLinks(Account account, Charge chargeFromResponse) {
        URI paymentURI = publicApiUriGenerator.getPaymentURI(chargeFromResponse.getChargeId());

        return PaymentWithAllLinks.getPaymentWithLinks(
                account.getPaymentType(),
                chargeFromResponse,
                paymentURI,
                publicApiUriGenerator.getPaymentEventsURI(chargeFromResponse.getChargeId()),
                publicApiUriGenerator.getPaymentCancelURI(chargeFromResponse.getChargeId()),
                publicApiUriGenerator.getPaymentRefundsURI(chargeFromResponse.getChargeId()),
                publicApiUriGenerator.getPaymentCaptureURI(chargeFromResponse.getChargeId()));
    }
}
