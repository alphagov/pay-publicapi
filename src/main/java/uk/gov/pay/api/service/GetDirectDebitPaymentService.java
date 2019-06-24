package uk.gov.pay.api.service;

import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.exception.GetChargeException;
import uk.gov.pay.api.model.directdebit.DirectDebitConnectorPaymentResponse;
import uk.gov.pay.api.model.directdebit.mandates.DirectDebitPayment;

import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;

import static org.apache.http.HttpStatus.SC_OK;

public class GetDirectDebitPaymentService {

    private final Client client;
    private final PublicApiUriGenerator publicApiUriGenerator;
    private final ConnectorUriGenerator connectorUriGenerator;

    @Inject
    public GetDirectDebitPaymentService(Client client, PublicApiUriGenerator publicApiUriGenerator, ConnectorUriGenerator connectorUriGenerator) {
        this.client = client;
        this.publicApiUriGenerator = publicApiUriGenerator;
        this.connectorUriGenerator = connectorUriGenerator;
    }

    public DirectDebitPayment getDirectDebitPayment(Account account, String paymentId) {
        Response connectorResponse = client
                .target(connectorUriGenerator.chargeURI(account, paymentId))
                .request()
                .get();
        if (connectorResponse.getStatus() != SC_OK) {
            throw new GetChargeException(connectorResponse);
        }
        DirectDebitConnectorPaymentResponse ddConnectorResponse = 
                connectorResponse.readEntity(DirectDebitConnectorPaymentResponse.class);
        
        return DirectDebitPayment.from(ddConnectorResponse, publicApiUriGenerator);
    }
}
