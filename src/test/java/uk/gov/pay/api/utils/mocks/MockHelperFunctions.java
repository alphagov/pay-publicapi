package uk.gov.pay.api.utils.mocks;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.google.gson.GsonBuilder;
import uk.gov.pay.api.it.fixtures.PaymentSingleResultBuilder;
import uk.gov.pay.api.model.Address;
import uk.gov.pay.api.model.CardDetailsFromResponse;
import uk.gov.pay.api.model.PaymentState;
import uk.gov.service.payments.commons.model.ErrorIdentifier;
import uk.gov.service.payments.commons.model.SupportedLanguage;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static javax.ws.rs.HttpMethod.GET;
import static javax.ws.rs.HttpMethod.POST;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.pay.api.it.fixtures.PaymentSingleResultBuilder.aSuccessfulSinglePayment;
import static uk.gov.pay.api.utils.mocks.ChargeResponseFromConnector.ChargeResponseFromConnectorBuilder.aCreateOrGetChargeResponseFromConnector;

public class MockHelperFunctions {

    private static final DateFormat SDF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    
    static ChargeResponseFromConnector fromCreateChargeRequestParams(String gatewayAccountId, CreateChargeRequestParams requestParams) {
        var responseFromConnector = aCreateOrGetChargeResponseFromConnector()
                .withAmount(requestParams.getAmount())
                .withChargeId("chargeId")
                .withState(new PaymentState("created", false, null, null))
                .withReturnUrl(requestParams.getReturnUrl())
                .withDescription(requestParams.getDescription())
                .withReference(requestParams.getReference())
                .withPaymentProvider("Sandbox")
                .withGatewayTransactionId("gatewayTransactionId")
                .withCreatedDate(SDF.format(new Date()))
                .withLanguage(SupportedLanguage.ENGLISH)
                .withDelayedCapture(false)
                .withCardDetails(new CardDetailsFromResponse("1234", "123456", "Mr. Payment", "12/19", null, "Mastercard", "debit"))
                .withLink(validGetLink(chargeLocation(gatewayAccountId, "chargeId"), "self"))
                .withLink(validGetLink(nextUrl("chargeTokenId"), "next_url"))
                .withLink(validPostLink(nextUrlPost(), "next_url_post", "application/x-www-form-urlencoded", getChargeIdTokenMap("chargeTokenId", false)));

        requestParams.getSetUpAgreement().ifPresent(responseFromConnector::withAgreementId);

        if (!requestParams.getMetadata().isEmpty())
            responseFromConnector.withMetadata(requestParams.getMetadata());

        if (requestParams.getEmail() != null) {
            responseFromConnector.withEmail(requestParams.getEmail());
        }

        if (requestParams.getCardholderName().isPresent() || requestParams.getAddressLine1().isPresent() ||
                requestParams.getAddressLine2().isPresent() || requestParams.getAddressPostcode().isPresent() ||
                requestParams.getAddressCity().isPresent() || requestParams.getAddressCountry().isPresent()) {
            Address billingAddress = new Address(requestParams.getAddressLine1().orElse(null), requestParams.getAddressLine2().orElse(null),
                    requestParams.getAddressPostcode().orElse(null), requestParams.getAddressCity().orElse(null), requestParams.getAddressCountry().orElse(null));
            CardDetailsFromResponse cardDetails = new CardDetailsFromResponse(null, null, requestParams.getCardholderName().orElse(null),
                    null, billingAddress, null, null);
            responseFromConnector.withCardDetails(cardDetails);
        }
        
        return responseFromConnector.build();
    }
    
    static Map<String, String> getChargeIdTokenMap(String chargeTokenId, boolean isMotoApi) {
        final Map<String, String> chargeTokenIdMap = new HashMap<>();
        chargeTokenIdMap.put(isMotoApi ? "one_time_token" : "chargeTokenId", chargeTokenId);
        return chargeTokenIdMap;
    }

    static Map<String, String> validGetLink(String href, String rel) {
        return Map.of(
                "href", href,
                "rel", rel,
                "method", GET);
    }

    static Map<String, Object> validPostLink(String href, String rel, String type, Map<String, String> params) {
        return Map.of(
                "href", href,
                "rel", rel,
                "type", type,
                "params", params,
                "method", POST);
    }
    
    static String chargeLocation(String accountId, String chargeId) {
        return format("/v1/api/accounts/%s/charges/%s", accountId, chargeId);
    }

    static String nextUrl(String tokenId) {
        return nextUrlPost() + tokenId;
    }
    
    static String nextUrlPost() {
        return "http://frontend_card/charge/";
    }
    
    static String buildChargeResponse(ChargeResponseFromConnector responseFromConnector) {
        PaymentSingleResultBuilder resultBuilder = aSuccessfulSinglePayment()
                .withChargeId(responseFromConnector.getChargeId())
                .withAmount(responseFromConnector.getAmount())
                .withMatchingReference(responseFromConnector.getReference())
                .withEmail(responseFromConnector.getEmail())
                .withDescription(responseFromConnector.getDescription())
                .withState(responseFromConnector.getState())
                .withReturnUrl(responseFromConnector.getReturnUrl())
                .withCreatedDate(responseFromConnector.getCreatedDate())
                .withLanguage(responseFromConnector.getLanguage())
                .withPaymentProvider(responseFromConnector.getPaymentProvider())
                .withDelayedCapture(responseFromConnector.isDelayedCapture())
                .withMoto(responseFromConnector.isMoto())
                .withAgreementId(responseFromConnector.getAgreementId())
                .withLinks(responseFromConnector.getLinks())
                .withSettlementSummary(responseFromConnector.getSettlementSummary())
                .withAuthorisationMode(responseFromConnector.getAuthorisationMode());

        ofNullable(responseFromConnector.getCardDetails()).ifPresent(resultBuilder::withCardDetails);
        ofNullable(responseFromConnector.getRefundSummary()).ifPresent(resultBuilder::withRefundSummary);
        ofNullable(responseFromConnector.getGatewayTransactionId()).ifPresent(resultBuilder::withGatewayTransactionId);
        ofNullable(responseFromConnector.getCorporateCardSurcharge()).ifPresent(resultBuilder::withCorporateCardSurcharge);
        ofNullable(responseFromConnector.getTotalAmount()).ifPresent(resultBuilder::withTotalAmount);
        ofNullable(responseFromConnector.getFee()).ifPresent(resultBuilder::withFee);
        ofNullable(responseFromConnector.getNetAmount()).ifPresent(resultBuilder::withNetAmount);
        ofNullable(responseFromConnector.getAuthorisationSummary()).ifPresent(resultBuilder::withAuthorisationSummary);
        ofNullable(responseFromConnector.getWalletType()).ifPresent(resultBuilder::withWalletType);
        ofNullable(responseFromConnector.getExemption()).ifPresent(resultBuilder::withExemption);
        responseFromConnector.getMetadata().ifPresent(resultBuilder::withMetadata);

        return resultBuilder.build();
    }
    
    static ResponseDefinitionBuilder buildErrorResponse(int statusCode, String errorMsg, ErrorIdentifier errorIdentifier, String reason) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("message", List.of(errorMsg));
        payload.put("error_identifier", errorIdentifier.toString());
        if (reason != null) {
            payload.put("reason", reason);
        }

        return aResponse()
                .withStatus(statusCode)
                .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                .withBody(new GsonBuilder().create().toJson(payload));
    }
}
