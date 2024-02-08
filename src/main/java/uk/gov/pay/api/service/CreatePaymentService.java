package uk.gov.pay.api.service;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.api.app.config.PublicApiConfig;
import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.exception.CreateChargeException;
import uk.gov.pay.api.model.*;
import uk.gov.pay.api.model.links.PaymentWithAllLinks;

import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.*;
import java.util.Optional;

import static javax.ws.rs.client.Entity.json;
import static uk.gov.pay.api.model.CreatedPaymentWithAllLinks.WhenCreated.BRAND_NEW;
import static uk.gov.pay.api.model.CreatedPaymentWithAllLinks.WhenCreated.EXISTING;

public class CreatePaymentService {

    private static final Logger logger = LoggerFactory.getLogger(CreatePaymentService.class);
    private final Client client;
    private final PublicApiUriGenerator publicApiUriGenerator;
    private final ConnectorUriGenerator connectorUriGenerator;

    private final PublicApiConfig publicApiConfig;

    @Inject
    public CreatePaymentService(Client client, PublicApiUriGenerator publicApiUriGenerator, ConnectorUriGenerator connectorUriGenerator, PublicApiConfig publicApiConfig) {
        this.client = client;
        this.publicApiUriGenerator = publicApiUriGenerator;
        this.connectorUriGenerator = connectorUriGenerator;
        this.publicApiConfig = publicApiConfig;
    }

    public CreatedPaymentWithAllLinks create(Account account, CreateCardPaymentRequest createCardPaymentRequest, String idempotencyKey) {
        // TODO: at this point, we might want to check which payment methods are available for the payment by querying
        // if card/pay-by-bank is enabled for the service. We can store this on the payment created in the payments
        // microservice. Potentially we could continue creating the charge directly in connector at first if only card 
        // is enabled. For the sake of prototyping, always create in payments microservice.
        boolean createInPaymentsMicroservice = true;
        if (createInPaymentsMicroservice) {
            logger.info("Creating payment in payments app");
            Response paymentResponse = createPaymentInPaymentsApp(account, createCardPaymentRequest, idempotencyKey);
            var paymentServiceResponse = paymentResponse.readEntity(PaymentServicePaymentResponse.class);
            return CreatedPaymentWithAllLinks.of(buildResponseModel(Charge.from(paymentServiceResponse)), BRAND_NEW);
        } else {
            logger.info("Creating payment in connector app");
            Response connectorResponse = createCharge(account, createCardPaymentRequest, idempotencyKey);

            if (connectorCreatedNewPayment(connectorResponse)) {
                ChargeFromResponse chargeFromResponse = connectorResponse.readEntity(ChargeFromResponse.class);
                return CreatedPaymentWithAllLinks.of(buildResponseModel(Charge.from(chargeFromResponse)), BRAND_NEW);
            }

            if (connectorReturnedExistingPayment(connectorResponse)) {
                ChargeFromResponse chargeFromResponse = connectorResponse.readEntity(ChargeFromResponse.class);
                return CreatedPaymentWithAllLinks.of(buildResponseModel(Charge.from(chargeFromResponse)), EXISTING);
            }

            throw new CreateChargeException(connectorResponse);
        }
    }

    private PaymentWithAllLinks buildResponseModel(Charge chargeFromResponse) {
        return PaymentWithAllLinks.getPaymentWithLinks(
                chargeFromResponse,
                publicApiUriGenerator.getPaymentURI(chargeFromResponse.getChargeId()),
                publicApiUriGenerator.getPaymentEventsURI(chargeFromResponse.getChargeId()),
                publicApiUriGenerator.getPaymentCancelURI(chargeFromResponse.getChargeId()),
                publicApiUriGenerator.getPaymentRefundsURI(chargeFromResponse.getChargeId()),
                publicApiUriGenerator.getPaymentCaptureURI(chargeFromResponse.getChargeId()),
                publicApiUriGenerator.getPaymentAuthorisationURI());
    }

    private boolean connectorCreatedNewPayment(Response connectorResponse) {
        return connectorResponse.getStatus() == HttpStatus.SC_CREATED;
    }

    private boolean connectorReturnedExistingPayment(Response connectorResponse) {
        return connectorResponse.getStatus() == HttpStatus.SC_OK;
    }


    private Response createCharge(Account account, CreateCardPaymentRequest createCardPaymentRequest, String idempotencyKey) {
        MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();

        Optional.ofNullable(idempotencyKey)
                .ifPresent(key -> headers.add("Idempotency-Key", idempotencyKey));

        return client
                .target(connectorUriGenerator.chargesURI(account))
                .request()
                .headers(headers)
                .accept(MediaType.APPLICATION_JSON)
                .post(buildChargeRequestPayload(createCardPaymentRequest));
    }

    private Entity buildChargeRequestPayload(CreateCardPaymentRequest requestPayload) {
        return json(requestPayload.toConnectorPayload());
    }

    public Response createPaymentInPaymentsApp(Account account, CreateCardPaymentRequest createCardPaymentRequest, String idempotencyKey) {
        MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();

        Optional.ofNullable(idempotencyKey)
                .ifPresent(key -> headers.add("Idempotency-Key", idempotencyKey));

        var path = String.format("/v1/account/%s/payment", account.getAccountId());
        String url = UriBuilder.fromPath(publicApiConfig.getPaymentsUrl()).path(path).toString();

        return client
                .target(url)
                .request()
                .headers(headers)
                .accept(MediaType.APPLICATION_JSON)
                .post(json(createCardPaymentRequest.toPaymentsPayload()));
    }
}
