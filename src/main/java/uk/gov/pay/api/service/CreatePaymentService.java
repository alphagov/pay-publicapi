package uk.gov.pay.api.service;

import org.apache.http.HttpStatus;
import uk.gov.pay.api.app.config.PublicApiConfig;
import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.exception.CreateChargeException;
import uk.gov.pay.api.model.ChargeFromResponse;
import uk.gov.pay.api.model.CreatePaymentRequest;
import uk.gov.pay.api.model.links.PaymentWithAllLinks;
import uk.gov.pay.api.utils.JsonStringBuilder;

import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static javax.ws.rs.client.Entity.json;

public class CreatePaymentService {

    private final String baseUrl;
    private final Client client;
    private final PublicApiUriGenerator publicApiUriGenerator;
    private final ConnectorUriGenerator connectorUriGenerator;

    @Inject
    public CreatePaymentService(Client client, PublicApiConfig configuration, PublicApiUriGenerator publicApiUriGenerator, ConnectorUriGenerator connectorUriGenerator) {
        this.client = client;
        this.baseUrl = configuration.getBaseUrl();
        this.publicApiUriGenerator = publicApiUriGenerator;
        this.connectorUriGenerator = connectorUriGenerator;
    }

    public PaymentWithAllLinks create(Account account, CreatePaymentRequest requestPayload) {
        Response connectorResponse = createCharge(account, requestPayload);

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
                    publicApiUriGenerator.getPaymentRefundsURI(chargeFromResponse.getChargeId()));
    }

    private boolean createdSuccessfully(Response connectorResponse) {
        return connectorResponse.getStatus() == HttpStatus.SC_CREATED;
    }

    private Response createCharge(Account account, CreatePaymentRequest requestPayload) {
        return client
                .target(connectorUriGenerator.chargesURI(account))
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .post(buildChargeRequestPayload(requestPayload));
    }

    private Entity buildChargeRequestPayload(CreatePaymentRequest requestPayload) {
        int amount = requestPayload.getAmount();
        String reference = requestPayload.getReference();
        String description = requestPayload.getDescription();
        String returnUrl = requestPayload.getReturnUrl();
        return json(new JsonStringBuilder()
                .add("amount", amount)
                .add("reference", reference)
                .add("description", description)
                .add("return_url", returnUrl)
                .build());
    }
}
