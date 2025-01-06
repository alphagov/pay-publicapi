package uk.gov.pay.api.service;

import uk.gov.pay.api.auth.Account;

import jakarta.inject.Inject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Response;

public class CapturePaymentService {

    private final Client client;
    private final ConnectorUriGenerator connectorUriGenerator;

    @Inject
    public CapturePaymentService(Client client, ConnectorUriGenerator connectorUriGenerator) {
        this.client = client;
        this.connectorUriGenerator = connectorUriGenerator;
    }

    public Response capture(Account account, String chargeId) {
        return client
                .target(connectorUriGenerator.captureURI(account, chargeId))
                .request()
                .post(Entity.json("{}"));
    }

}
