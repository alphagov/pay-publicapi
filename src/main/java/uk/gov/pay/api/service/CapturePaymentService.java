package uk.gov.pay.api.service;

import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.clients.ExternalServiceClient;

import javax.inject.Inject;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

public class CapturePaymentService {

    private final ExternalServiceClient client;
    private final ConnectorUriGenerator connectorUriGenerator;

    @Inject
    public CapturePaymentService(ExternalServiceClient client, ConnectorUriGenerator connectorUriGenerator) {
        this.client = client;
        this.connectorUriGenerator = connectorUriGenerator;
    }

    public Response capture(Account account, String chargeId) {
        return client.post(connectorUriGenerator.captureURI(account, chargeId), Entity.json("{}"));
    }

}
