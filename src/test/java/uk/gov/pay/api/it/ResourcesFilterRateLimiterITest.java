package uk.gov.pay.api.it;

import com.google.common.collect.ImmutableMap;
import io.restassured.response.ValidatableResponse;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import uk.gov.pay.api.it.fixtures.PaymentNavigationLinksFixture;
import uk.gov.pay.api.utils.mocks.ConnectorMockClient;
import uk.gov.pay.commons.model.SupportedLanguage;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertThat;
import static uk.gov.pay.api.it.fixtures.PaginatedPaymentSearchResultFixture.aPaginatedPaymentSearchResult;
import static uk.gov.pay.api.it.fixtures.PaymentSearchResultBuilder.aSuccessfulSearchPayment;
import static uk.gov.pay.api.utils.mocks.ChargeResponseFromConnector.ChargeResponseFromConnectorBuilder.aCreateOrGetChargeResponseFromConnector;

public class ResourcesFilterRateLimiterITest extends ResourcesFilterITestBase {

    private ConnectorMockClient connectorMockClient = new ConnectorMockClient(connectorMock);
    
    @Test
    public void createPayment_whenRateLimitIsReached_shouldReturn429Response() throws Exception {

        connectorMockClient.respondOk_whenCreateCharge("token_1234567asdf", GATEWAY_ACCOUNT_ID, aCreateOrGetChargeResponseFromConnector()
                .withAmount(AMOUNT)
                .withChargeId(CHARGE_ID)
                .withState(CREATED)
                .withReturnUrl(RETURN_URL)
                .withDescription(DESCRIPTION)
                .withReference(REFERENCE)
                .withPaymentProvider("Sandbox")
                .withCreatedDate(CREATED_DATE)
                .withLanguage(SupportedLanguage.ENGLISH)
                .withDelayedCapture(false)
                .withGatewayTransactionId("gatewayTxId")
                .build());

        List<Callable<ValidatableResponse>> tasks = Arrays.asList(
                () -> postPaymentResponse(API_KEY, PAYLOAD),
                () -> postPaymentResponse(API_KEY, PAYLOAD),
                () -> postPaymentResponse(API_KEY, PAYLOAD),
                () -> postPaymentResponse(API_KEY, PAYLOAD));

        List<ValidatableResponse> finishedTasks = invokeAll(tasks);
        List<Integer> statusCodes = extractStatusCodes(finishedTasks);

        assertThat(statusCodes, hasItems(201, 429));
        assertThat(finishedTasks, hasItem(anErrorResponse()));
    }

    @Test
    public void getPayment_whenRateLimitIsReached_shouldReturn429Response() throws Exception {

        connectorMockClient.respondWithChargeFound("token_1234567asdf", GATEWAY_ACCOUNT_ID,
                aCreateOrGetChargeResponseFromConnector()
                        .withAmount(AMOUNT)
                        .withChargeId(CHARGE_ID)
                        .withState(CREATED)
                        .withReturnUrl(RETURN_URL)
                        .withDescription(DESCRIPTION)
                        .withReference(REFERENCE)
                        .withPaymentProvider("Sandbox")
                        .withCreatedDate(CREATED_DATE)
                        .withLanguage(SupportedLanguage.ENGLISH)
                        .withDelayedCapture(false)
                        .build());

        List<Callable<ValidatableResponse>> tasks = Arrays.asList(
                () -> getPaymentResponse(API_KEY),
                () -> getPaymentResponse(API_KEY),
                () -> getPaymentResponse(API_KEY),
                () -> getPaymentResponse(API_KEY)
        );

        List<ValidatableResponse> finishedTasks = invokeAll(tasks);
        List<Integer> statusCodes = extractStatusCodes(finishedTasks);

        assertThat(statusCodes, hasItems(200, 429));
        assertThat(finishedTasks, hasItem(anErrorResponse()));
    }

    @Test
    public void getPaymentEvents_whenRateLimitIsReached_shouldReturn429Response() throws Exception {

        connectorMockClient.respondWithChargeEventsFound(GATEWAY_ACCOUNT_ID, CHARGE_ID, EVENTS);

        List<Callable<ValidatableResponse>> tasks = Arrays.asList(
                () -> getPaymentEventsResponse(API_KEY),
                () -> getPaymentEventsResponse(API_KEY),
                () -> getPaymentEventsResponse(API_KEY),
                () -> getPaymentEventsResponse(API_KEY)
        );

        List<ValidatableResponse> finishedTasks = invokeAll(tasks);
        List<Integer> statusCodes = extractStatusCodes(finishedTasks);

        assertThat(statusCodes, hasItems(200, 429));
        assertThat(finishedTasks, hasItem(anErrorResponse()));
    }

    @Test
    public void searchPayments_whenRateLimitIsReached_shouldReturn429Response() throws Exception {
        String payments = aPaginatedPaymentSearchResult()
                .withCount(10)
                .withPage(2)
                .withTotal(20)
                .withLinks(new PaymentNavigationLinksFixture().withSelfLink("/self"))
                .withPayments(aSuccessfulSearchPayment()
                        .withInProgressState("created")
                        .withReference(REFERENCE)
                        .withNumberOfResults(1).getResults())
                .build();

        connectorMockClient.respondOk_whenSearchCharges(GATEWAY_ACCOUNT_ID, payments);

        List<Callable<ValidatableResponse>> tasks = Arrays.asList(
                () -> searchPayments(API_KEY, ImmutableMap.of("reference", REFERENCE)),
                () -> searchPayments(API_KEY, ImmutableMap.of("reference", REFERENCE)),
                () -> searchPayments(API_KEY, ImmutableMap.of("reference", REFERENCE))
        );

        List<ValidatableResponse> finishedTasks = invokeAll(tasks);
        List<Integer> statusCodes = extractStatusCodes(finishedTasks);

        assertThat(statusCodes, hasItems(200, 429));
        assertThat(finishedTasks, hasItem(anErrorResponse()));
    }

    @Test
    public void cancelPayment_whenRateLimitIsReached_shouldReturn429Response() throws Exception {

        connectorMockClient.respondOk_whenCancelCharge(CHARGE_ID, GATEWAY_ACCOUNT_ID);

        List<Callable<ValidatableResponse>> tasks = Arrays.asList(
                () -> postCancelPaymentResponse(API_KEY),
                () -> postCancelPaymentResponse(API_KEY),
                () -> postCancelPaymentResponse(API_KEY)
        );

        List<ValidatableResponse> finishedTasks = invokeAll(tasks);
        List<Integer> statusCodes = extractStatusCodes(finishedTasks);

        assertThat(statusCodes, hasItems(204, 429));
        assertThat(finishedTasks, hasItem(anErrorResponse()));
    }

    @NotNull
    private List<Integer> extractStatusCodes(List<ValidatableResponse> validatableResponses) {
        return validatableResponses
                .stream()
                .map(validatableResponse -> validatableResponse.extract().statusCode())
                .collect(Collectors.toList());
    }
}
