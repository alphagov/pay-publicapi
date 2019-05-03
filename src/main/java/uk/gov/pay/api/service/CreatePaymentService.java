package uk.gov.pay.api.service;

import org.apache.http.HttpStatus;
import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.exception.CreateChargeException;
import uk.gov.pay.api.model.Address;
import uk.gov.pay.api.model.ChargeFromResponse;
import uk.gov.pay.api.model.PrefilledCardholderDetails;
import uk.gov.pay.api.model.ValidCreatePaymentRequest;
import uk.gov.pay.api.model.links.PaymentWithAllLinks;
import uk.gov.pay.api.utils.JsonStringBuilder;

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
    private static final String PREFILLED_CARDHOLDER_DETAILS = "prefilled_cardholder_details";
    private static final String BILLING_ADDRESS = "billing_address";

    @Inject
    public CreatePaymentService(Client client, PublicApiUriGenerator publicApiUriGenerator, ConnectorUriGenerator connectorUriGenerator) {
        this.client = client;
        this.publicApiUriGenerator = publicApiUriGenerator;
        this.connectorUriGenerator = connectorUriGenerator;
    }

    public PaymentWithAllLinks create(Account account, ValidCreatePaymentRequest validCreatePaymentRequest) {
        Response connectorResponse = createCharge(account, validCreatePaymentRequest);

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

    private Response createCharge(Account account, ValidCreatePaymentRequest validCreatePaymentRequest) {
        return client
                .target(connectorUriGenerator.chargesURI(account, validCreatePaymentRequest.getAgreementId().orElse(null)))
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .post(buildChargeRequestPayload(validCreatePaymentRequest));
    }

    private Entity buildChargeRequestPayload(ValidCreatePaymentRequest requestPayload) {
        int amount = requestPayload.getAmount();
        String reference = requestPayload.getReference();
        String description = requestPayload.getDescription();
        JsonStringBuilder request = new JsonStringBuilder()
                .add("amount", amount)
                .add("reference", reference)
                .add("description", description);
        requestPayload.getLanguage().ifPresent(language -> request.add("language", language.toString()));
        requestPayload.getReturnUrl().ifPresent(returnUrl -> request.add("return_url", returnUrl));
        requestPayload.getAgreementId().ifPresent(agreementId -> request.add("agreement_id", agreementId));
        requestPayload.getDelayedCapture().ifPresent(delayedCapture -> request.add("delayed_capture", delayedCapture));
        requestPayload.getMetadata().ifPresent(metadata -> request.add("metadata", metadata.getMetadata()));
        requestPayload.getEmail().ifPresent(email -> request.add("email", email));
        requestPayload.getPrefilledCardholderDetails().ifPresent(prefilledCardholderDetails -> buildPrefilledCardHolderDetails(prefilledCardholderDetails, request));
        return json(request.build());
    }
    
    private void buildPrefilledCardHolderDetails(PrefilledCardholderDetails prefilledCardholderDetails, JsonStringBuilder request) {
        prefilledCardholderDetails.getCardholderName()
                .ifPresent(name -> request.addToMap(PREFILLED_CARDHOLDER_DETAILS, "cardholder_name", name));
        prefilledCardholderDetails.getBillingAddress().ifPresent(address -> {
            request.addToNestedMap("line1", address.getLine1(), PREFILLED_CARDHOLDER_DETAILS, BILLING_ADDRESS);
            request.addToNestedMap("line2", address.getLine2(), PREFILLED_CARDHOLDER_DETAILS, BILLING_ADDRESS);
            request.addToNestedMap("postcode", address.getPostcode(), PREFILLED_CARDHOLDER_DETAILS, BILLING_ADDRESS);
            request.addToNestedMap("city", address.getCity(), PREFILLED_CARDHOLDER_DETAILS, BILLING_ADDRESS);
            request.addToNestedMap("country", address.getCountry(), PREFILLED_CARDHOLDER_DETAILS, BILLING_ADDRESS);
        });
    }
}
