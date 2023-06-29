package uk.gov.pay.api.service;

import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.exception.GetChargeException;
import uk.gov.pay.api.model.CardPayment;
import uk.gov.pay.api.model.Charge;

import javax.inject.Inject;
import java.net.URI;

public class GetPaymentService {

    private final PublicApiUriGenerator publicApiUriGenerator;
    private final ConnectorService connectorService;
    private final LedgerService ledgerService;

    @Inject
    public GetPaymentService(PublicApiUriGenerator publicApiUriGenerator,
                             ConnectorService connectorService, LedgerService ledgerService) {
        this.publicApiUriGenerator = publicApiUriGenerator;
        this.connectorService = connectorService;
        this.ledgerService = ledgerService;
    }

    public CardPayment getConnectorCharge(Account account, String paymentId) {
        Charge charge = connectorService.getCharge(account, paymentId);

        return getPaymentWithAllLinks(charge);
    }
    
    public CardPayment getLedgerTransaction(Account account, String paymentId) {
        Charge charge = ledgerService.getPaymentTransaction(account, paymentId);
        
        return getPaymentWithAllLinks(charge);
    }

    public CardPayment getPayment(Account account, String paymentId) {
        try {
            return getConnectorCharge(account, paymentId);
        } catch (GetChargeException ex) {
            return getLedgerTransaction(account, paymentId);
        }
    }

    private CardPayment getPaymentWithAllLinks(Charge chargeFromResponse) {
        URI paymentURI = publicApiUriGenerator.getPaymentURI(chargeFromResponse.getChargeId());

        return new CardPayment(chargeFromResponse,
                paymentURI,
                publicApiUriGenerator.getPaymentEventsURI(chargeFromResponse.getChargeId()),
                publicApiUriGenerator.getPaymentCancelURI(chargeFromResponse.getChargeId()),
                publicApiUriGenerator.getPaymentRefundsURI(chargeFromResponse.getChargeId()),
                publicApiUriGenerator.getPaymentCaptureURI(chargeFromResponse.getChargeId()),
                publicApiUriGenerator.getPaymentAuthorisationURI());
    }
}
