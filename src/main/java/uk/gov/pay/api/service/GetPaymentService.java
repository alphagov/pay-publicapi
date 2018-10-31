package uk.gov.pay.api.service;

import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.exception.GetChargeException;
import uk.gov.pay.api.model.ChargeFromResponse;
import uk.gov.pay.api.model.links.PaymentWithAllLinks;

import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import java.net.URI;

import static org.apache.http.HttpStatus.SC_OK;

public class GetPaymentService {

    private final Client client;
    private final PublicApiUriGenerator publicApiUriGenerator;
    private final ConnectorUriGenerator connectorUriGenerator;

    @Inject
    public GetPaymentService(Client client, PublicApiUriGenerator publicApiUriGenerator, ConnectorUriGenerator connectorUriGenerator) {
        this.client = client;
        this.publicApiUriGenerator = publicApiUriGenerator;
        this.connectorUriGenerator = connectorUriGenerator;
    }

    public PaymentWithAllLinks getPayment(Account account, String paymentId) {
        Response connectorResponse = client
                .target(connectorUriGenerator.chargeURI(account, paymentId))
                .request()
                .get();

        if (connectorResponse.getStatus() == SC_OK) {
            ChargeFromResponse chargeFromResponse = connectorResponse.readEntity(ChargeFromResponse.class);
            URI paymentURI = publicApiUriGenerator.getPaymentURI(chargeFromResponse.getChargeId());

            PaymentWithAllLinks payment = PaymentWithAllLinks.getPaymentWithLinks(
                    account.getPaymentType(),
                    chargeFromResponse,
                    paymentURI,
                    publicApiUriGenerator.getPaymentEventsURI(chargeFromResponse.getChargeId()),
                    publicApiUriGenerator.getPaymentCancelURI(chargeFromResponse.getChargeId()),
                    publicApiUriGenerator.getPaymentRefundsURI(chargeFromResponse.getChargeId()),
                    publicApiUriGenerator.getPaymentCaptureURI(chargeFromResponse.getChargeId()));
            return payment;
        }
        throw new GetChargeException(connectorResponse);
    }
}
