package uk.gov.pay.api.service;

import uk.gov.pay.api.app.config.PublicApiConfig;
import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.exception.GetChargeException;
import uk.gov.pay.api.exception.GetRefundsException;
import uk.gov.pay.api.model.Charge;
import uk.gov.pay.api.model.ChargeFromResponse;
import uk.gov.pay.api.model.RefundsFromConnector;
import uk.gov.pay.api.model.RefundsResponse;

import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;

import static org.apache.http.HttpStatus.SC_OK;

public class ConnectorService {
    private final Client client;
    private final ConnectorUriGenerator connectorUriGenerator;

    private final String baseUrl;

    @Inject
    public ConnectorService(Client client, ConnectorUriGenerator connectorUriGenerator,
                            PublicApiConfig publicApiConfig) {
        this.client = client;
        this.connectorUriGenerator = connectorUriGenerator;
        this.baseUrl = publicApiConfig.getBaseUrl();
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

    public RefundsResponse getPaymentRefunds(Account account, String paymentId) {
        Response connectorResponse = client
                .target(connectorUriGenerator.refundsForPaymentURI(account.getAccountId(), paymentId))
                .request()
                .get();

        if (connectorResponse.getStatus() == SC_OK) {
            RefundsFromConnector refundsFromConnector = connectorResponse.readEntity(RefundsFromConnector.class);
            return RefundsResponse.valueOf(refundsFromConnector, baseUrl);
        }

        throw new GetRefundsException(connectorResponse);
    }
}
