package uk.gov.pay.api.service;

import org.apache.http.HttpStatus;
import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.clients.ExternalServiceClient;
import uk.gov.pay.api.exception.CancelChargeException;

import javax.inject.Inject;
import javax.ws.rs.core.Response;

public class CancelPaymentService {

    private final ExternalServiceClient client;
    private final ConnectorUriGenerator connectorUriGenerator;

    @Inject
    public CancelPaymentService(ExternalServiceClient client, ConnectorUriGenerator connectorUriGenerator) {
        this.client = client;
        this.connectorUriGenerator = connectorUriGenerator;
    }

    public Response cancel(Account account, String chargeId) {
        Response connectorResponse = client.post(connectorUriGenerator.cancelURI(account, chargeId));

        if (connectorResponse.getStatus() == HttpStatus.SC_NO_CONTENT) {
            connectorResponse.close();
            return Response.noContent().build();
        }

        throw new CancelChargeException(connectorResponse);
    }

}
