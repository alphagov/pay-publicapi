package uk.gov.pay.api.service;

import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.exception.GetChargeException;
import uk.gov.pay.api.model.Charge;
import uk.gov.pay.api.model.ChargeFromResponse;

import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;

import static org.apache.http.HttpStatus.SC_OK;

public class ConnectorService {
    private final Client client;
    private final ConnectorUriGenerator connectorUriGenerator;

    @Inject
    public ConnectorService(Client client, ConnectorUriGenerator connectorUriGenerator) {
        this.client = client;
        this.connectorUriGenerator = connectorUriGenerator;
    }

    public Charge getCharge(Account account, String paymentId) {
        Response response = client
                .target(connectorUriGenerator.chargeURI(account, paymentId))
                .request()
                .get();

        if (response.getStatus() == SC_OK) {
            ChargeFromResponse chargeFromResponse = response.readEntity(ChargeFromResponse.class);
            return Charge.from(chargeFromResponse);
        }

        throw new GetChargeException(response);
    }
}
