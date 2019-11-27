package uk.gov.pay.api.service;

import org.apache.http.HttpStatus;
import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.clients.ExternalServiceClient;
import uk.gov.pay.api.exception.CreateChargeException;
import uk.gov.pay.api.model.Charge;
import uk.gov.pay.api.model.ChargeFromResponse;
import uk.gov.pay.api.model.CreateCardPaymentRequest;
import uk.gov.pay.api.model.links.PaymentWithAllLinks;

import javax.inject.Inject;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import static javax.ws.rs.client.Entity.json;

public class CreatePaymentService {

    private final ExternalServiceClient client;
    private final PublicApiUriGenerator publicApiUriGenerator;
    private final ConnectorUriGenerator connectorUriGenerator;

    @Inject
    public CreatePaymentService(ExternalServiceClient client, PublicApiUriGenerator publicApiUriGenerator, ConnectorUriGenerator connectorUriGenerator) {
        this.client = client;
        this.publicApiUriGenerator = publicApiUriGenerator;
        this.connectorUriGenerator = connectorUriGenerator;
    }

    public PaymentWithAllLinks create(Account account, CreateCardPaymentRequest createCardPaymentRequest) {
        Response connectorResponse = createCharge(account, createCardPaymentRequest);

        if (!createdSuccessfully(connectorResponse)) {
            throw new CreateChargeException(connectorResponse);
        }

        ChargeFromResponse chargeFromResponse = connectorResponse.readEntity(ChargeFromResponse.class);
        return buildResponseModel(account, Charge.from(chargeFromResponse));
    }

    private PaymentWithAllLinks buildResponseModel(Account account, Charge chargeFromResponse) {
        return PaymentWithAllLinks.getPaymentWithLinks(
                account.getPaymentType(),
                chargeFromResponse,
                publicApiUriGenerator.getPaymentURI(chargeFromResponse.getChargeId()),
                publicApiUriGenerator.getPaymentEventsURI(chargeFromResponse.getChargeId()),
                publicApiUriGenerator.getPaymentCancelURI(chargeFromResponse.getChargeId()),
                publicApiUriGenerator.getPaymentRefundsURI(chargeFromResponse.getChargeId()),
                publicApiUriGenerator.getPaymentCaptureURI(chargeFromResponse.getChargeId()));
    }

    private boolean createdSuccessfully(Response connectorResponse) {
        return connectorResponse.getStatus() == HttpStatus.SC_CREATED;
    }

    private Response createCharge(Account account, CreateCardPaymentRequest createCardPaymentRequest) {
        return client.post(connectorUriGenerator.chargesURI(account), buildChargeRequestPayload(createCardPaymentRequest));
    }

    private Entity buildChargeRequestPayload(CreateCardPaymentRequest requestPayload) {
        return json(requestPayload.toConnectorPayload());
    }
}
