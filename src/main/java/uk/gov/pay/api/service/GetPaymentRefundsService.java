package uk.gov.pay.api.service;

import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.model.RefundsResponse;

import javax.inject.Inject;

public class GetPaymentRefundsService {

    private final ConnectorService connectorService;

    @Inject
    public GetPaymentRefundsService(ConnectorService connectorService) {
        this.connectorService = connectorService;
    }

    public RefundsResponse getConnectorPaymentRefunds(Account account, String paymentId) {
        return connectorService.getPaymentRefunds(account, paymentId);
    }
}
