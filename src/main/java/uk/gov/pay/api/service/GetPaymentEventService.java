package uk.gov.pay.api.service;

import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.exception.GetEventsException;
import uk.gov.pay.api.model.PaymentEvents;
import uk.gov.pay.api.model.PaymentEventsResponse;

import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;

import static org.apache.http.HttpStatus.SC_OK;

public class GetPaymentEventService {

    private final Client client;
    private final ConnectorUriGenerator connectorUriGenerator;
    private final PublicApiUriGenerator publicApiUriGenerator;

    @Inject
    public GetPaymentEventService(Client client,
                                  ConnectorUriGenerator connectorUriGenerator,
                                  PublicApiUriGenerator publicApiUriGenerator) {
        this.client = client;
        this.connectorUriGenerator = connectorUriGenerator;
        this.publicApiUriGenerator = publicApiUriGenerator;
    }

    public PaymentEventsResponse getPaymentEvent(Account account, String paymentId) {
        Response connectorResponse = client
                .target(connectorUriGenerator.chargeEventsURI(account, paymentId))
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .get();
        if (connectorResponse.getStatus() == SC_OK) {
            URI paymentEventsLink = publicApiUriGenerator.getPaymentEventsURI(paymentId);
            URI paymentLink = publicApiUriGenerator.getPaymentURI(paymentId);
            PaymentEvents response = connectorResponse.readEntity(PaymentEvents.class);

            return PaymentEventsResponse.from(response, paymentLink, paymentEventsLink);
        }
        throw new GetEventsException(connectorResponse);
    }
}
