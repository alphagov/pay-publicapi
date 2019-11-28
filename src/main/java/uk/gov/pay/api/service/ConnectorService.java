package uk.gov.pay.api.service;

import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.clients.ExternalServiceClient;
import uk.gov.pay.api.exception.GetChargeException;
import uk.gov.pay.api.exception.GetEventsException;
import uk.gov.pay.api.exception.GetRefundException;
import uk.gov.pay.api.exception.GetRefundsException;
import uk.gov.pay.api.exception.SearchPaymentsException;
import uk.gov.pay.api.exception.SearchRefundsException;
import uk.gov.pay.api.model.Charge;
import uk.gov.pay.api.model.ChargeFromResponse;
import uk.gov.pay.api.model.PaymentEvents;
import uk.gov.pay.api.model.RefundFromConnector;
import uk.gov.pay.api.model.RefundsFromConnector;
import uk.gov.pay.api.model.search.card.PaymentSearchResponse;
import uk.gov.pay.api.model.search.card.SearchRefundsResponseFromConnector;

import javax.inject.Inject;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.util.Map;

import static org.apache.http.HttpStatus.SC_OK;

public class ConnectorService {
    private final ExternalServiceClient client;
    private final ConnectorUriGenerator connectorUriGenerator;

    @Inject
    public ConnectorService(ExternalServiceClient client, ConnectorUriGenerator connectorUriGenerator) {
        this.client = client;
        this.connectorUriGenerator = connectorUriGenerator;
    }

    public Charge getCharge(Account account, String paymentId) {
        Response response = client.get(connectorUriGenerator.chargeURI(account, paymentId));

        if (response.getStatus() == SC_OK) {
            ChargeFromResponse chargeFromResponse = response.readEntity(ChargeFromResponse.class);
            return Charge.from(chargeFromResponse);
        }

        throw new GetChargeException(response);
    }

    public PaymentEvents getChargeEvents(Account account, String paymentId) {
        Response connectorResponse = client.get(connectorUriGenerator.chargeEventsURI(account, paymentId));

        if (connectorResponse.getStatus() == SC_OK) {
            return connectorResponse.readEntity(PaymentEvents.class);
        }

        throw new GetEventsException(connectorResponse);
    }

    public RefundsFromConnector getPaymentRefunds(String accountId, String paymentId) {
        Response connectorResponse = client.get(connectorUriGenerator.refundsForPaymentURI(accountId, paymentId));

        if (connectorResponse.getStatus() == SC_OK) {
            return connectorResponse.readEntity(RefundsFromConnector.class);
        }

        throw new GetRefundsException(connectorResponse);
    }

    public RefundFromConnector getPaymentRefund(String accountId, String paymentId, String refundId) {
        Response connectorResponse = client.get(connectorUriGenerator.refundForPaymentURI(accountId, paymentId, refundId));

        if (connectorResponse.getStatus() == SC_OK) {
            return connectorResponse.readEntity(RefundFromConnector.class);
        }

        throw new GetRefundException(connectorResponse);
    }

    public SearchRefundsResponseFromConnector searchRefunds(Account account, Map<String, String> queryParams) {
        Response connectorResponse = client.get(connectorUriGenerator.refundsURIWithParams(account, queryParams));

        if (connectorResponse.getStatus() == SC_OK) {
            try {
                return connectorResponse.readEntity(SearchRefundsResponseFromConnector.class);
            } catch (ProcessingException exception) {
                throw new SearchRefundsException(exception);
            }
        }
        throw new SearchRefundsException(connectorResponse);
    }

    public PaymentSearchResponse<ChargeFromResponse> searchPayments(Account account, Map<String, String> queryParams) {
        String url = connectorUriGenerator.chargesURIWithParams(account, queryParams);
        Response connectorResponse = client.get(url);

        if (connectorResponse.getStatus() == SC_OK) {
            try {
                return connectorResponse.readEntity(new GenericType<>() {
                });
            } catch (ProcessingException ex) {
                throw new SearchPaymentsException(ex);
            }
        }

        throw new SearchPaymentsException(connectorResponse);
    }
}
