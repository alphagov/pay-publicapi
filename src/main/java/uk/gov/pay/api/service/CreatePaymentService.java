package uk.gov.pay.api.service;

import org.apache.http.HttpStatus;
import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.exception.CreateChargeException;
import uk.gov.pay.api.model.card.ChargeFromResponse;
import uk.gov.pay.api.model.request.CreatePaymentRequest;
import uk.gov.pay.api.model.response.PaymentWithAllLinks;

import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static javax.ws.rs.client.Entity.json;

public class CreatePaymentService {

    private final Client client;
    private final PublicApiUriGenerator publicApiUriGenerator;
    private final ConnectorUriGenerator connectorUriGenerator;

    @Inject
    public CreatePaymentService(Client client, PublicApiUriGenerator publicApiUriGenerator, ConnectorUriGenerator connectorUriGenerator) {
        this.client = client;
        this.publicApiUriGenerator = publicApiUriGenerator;
        this.connectorUriGenerator = connectorUriGenerator;
    }

    public PaymentWithAllLinks create(Account account, CreatePaymentRequest createPaymentRequest) {
        Response connectorResponse = createCharge(account, createPaymentRequest);

        if (!createdSuccessfully(connectorResponse)) {
            throw new CreateChargeException(connectorResponse);
        }

        ChargeFromResponse chargeFromResponse = connectorResponse.readEntity(ChargeFromResponse.class);
        return buildResponseModel(account, chargeFromResponse);
    }

    private PaymentWithAllLinks buildResponseModel(Account account, ChargeFromResponse chargeFromResponse) {
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

    private Response createCharge(Account account, CreatePaymentRequest validCreatePaymentRequest) {
        return client
                .target(connectorUriGenerator.chargesURI(account))
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .post(buildChargeRequestPayload(validCreatePaymentRequest));
    }

    private Entity buildChargeRequestPayload(CreatePaymentRequest requestPayload) {
        return json(requestPayload.toConnectorPayload());
    }
}
