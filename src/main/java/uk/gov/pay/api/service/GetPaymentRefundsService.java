package uk.gov.pay.api.service;

import uk.gov.pay.api.app.config.PublicApiConfig;
import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.model.Refund;
import uk.gov.pay.api.model.RefundResponse;
import uk.gov.pay.api.model.RefundsResponse;

import javax.inject.Inject;
import javax.ws.rs.core.UriBuilder;
import java.util.List;
import java.util.stream.Collectors;

import static uk.gov.pay.api.resources.PaymentRefundsResource.PAYMENT_BY_ID_PATH;
import static uk.gov.pay.api.resources.PaymentRefundsResource.PAYMENT_REFUNDS_PATH;
import static uk.gov.pay.api.resources.PaymentRefundsResource.PAYMENT_REFUND_BY_ID_PATH;

public class GetPaymentRefundsService {

    private final ConnectorService connectorService;
    private final String baseUrl;

    @Inject
    public GetPaymentRefundsService(ConnectorService connectorService,
                                    PublicApiConfig publicApiConfig) {
        this.connectorService = connectorService;
        this.baseUrl = publicApiConfig.getBaseUrl();
    }

    public RefundsResponse getConnectorPaymentRefunds(Account account, String paymentId) {
        List<Refund> refundsFromConnector = connectorService.getPaymentRefunds(account.getAccountId(), paymentId);

        List<RefundResponse> refundResponses = refundsFromConnector
                .stream()
                .map(refundFromConnector -> RefundResponse.from(refundFromConnector,
                        getRefundSelfLink(paymentId, refundFromConnector.getRefundId()),
                        getPaymentLink(paymentId))
                )
                .collect(Collectors.toList());

        return RefundsResponse.from(paymentId, refundResponses, getSelfLink(paymentId), getPaymentLink(paymentId));
    }

    private String getSelfLink(String paymentId) {
        return UriBuilder.fromUri(baseUrl)
                .path(PAYMENT_REFUNDS_PATH)
                .build(paymentId)
                .toString();
    }

    private String getPaymentLink(String paymentId) {
        return UriBuilder.fromUri(baseUrl)
                .path(PAYMENT_BY_ID_PATH)
                .build(paymentId)
                .toString();
    }

    private String getRefundSelfLink(String paymentId, String refundId) {
        return UriBuilder.fromUri(baseUrl)
                .path(PAYMENT_REFUND_BY_ID_PATH)
                .build(paymentId, refundId)
                .toString();
    }
}
