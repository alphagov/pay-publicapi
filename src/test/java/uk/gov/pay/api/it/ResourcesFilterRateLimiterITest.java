package uk.gov.pay.api.it;

import com.google.common.collect.ImmutableMap;
import io.restassured.response.ValidatableResponse;
import org.junit.Test;
import uk.gov.pay.api.it.fixtures.PaymentNavigationLinksFixture;
import uk.gov.pay.commons.model.SupportedLanguage;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.assertThat;
import static uk.gov.pay.api.it.fixtures.PaginatedPaymentSearchResultFixture.aPaginatedPaymentSearchResult;
import static uk.gov.pay.api.it.fixtures.PaymentSearchResultBuilder.aSuccessfulSearchPayment;
import static uk.gov.pay.api.utils.mocks.ChargeResponseFromConnector.ChargeResponseFromConnectorBuilder.aCreateOrGetChargeResponseFromConnector;

public class ResourcesFilterRateLimiterITest extends ResourcesFilterITestBase {

    @Test
    public void createPayment_whenRateLimitIsReached_shouldReturn429Response() throws Exception {

        connectorMock.respondOk_whenCreateCharge(CHARGE_TOKEN_ID, GATEWAY_ACCOUNT_ID, aCreateOrGetChargeResponseFromConnector()
                .withAmount(AMOUNT)
                .withChargeId(CHARGE_ID)
                .withState(CREATED)
                .withReturnUrl(RETURN_URL)
                .withDescription(DESCRIPTION)
                .withReference(REFERENCE)
                .withEmail(EMAIL)
                .withPaymentProvider(PAYMENT_PROVIDER)
                .withCreatedDate(CREATED_DATE)
                .withLanguage(SupportedLanguage.ENGLISH)
                .withDelayedCapture(false)
                .withRefundSummary(REFUND_SUMMARY)
                .withCardDetails(CARD_DETAILS)
                .withGatewayTransactionId("gatewayTxId")
                .build());

        List<Callable<ValidatableResponse>> tasks = Arrays.asList(
                () -> postPaymentResponse(API_KEY, PAYLOAD),
                () -> postPaymentResponse(API_KEY, PAYLOAD),
                () -> postPaymentResponse(API_KEY, PAYLOAD),
                () -> postPaymentResponse(API_KEY, PAYLOAD));

        List<ValidatableResponse> finishedTasks = invokeAll(tasks);

        assertThat(finishedTasks, hasItem(aResponse(201)));
        assertThat(finishedTasks, hasItem(anErrorResponse(429, "P0900", "Too many requests")));
    }

    @Test
    public void getPayment_whenRateLimitIsReached_shouldReturn429Response() throws Exception {

        connectorMock.respondWithChargeFound(CHARGE_TOKEN_ID, GATEWAY_ACCOUNT_ID,
                aCreateOrGetChargeResponseFromConnector()
                        .withAmount(AMOUNT)
                        .withChargeId(CHARGE_ID)
                        .withState(CREATED)
                        .withReturnUrl(RETURN_URL)
                        .withDescription(DESCRIPTION)
                        .withReference(REFERENCE)
                        .withEmail(EMAIL)
                        .withPaymentProvider(PAYMENT_PROVIDER)
                        .withCreatedDate(CREATED_DATE)
                        .withLanguage(SupportedLanguage.ENGLISH)
                        .withDelayedCapture(false)
                        .withRefundSummary(REFUND_SUMMARY)
                        .withCardDetails(CARD_DETAILS)
                        .build());

        List<Callable<ValidatableResponse>> tasks = Arrays.asList(
                () -> getPaymentResponse(API_KEY, CHARGE_ID),
                () -> getPaymentResponse(API_KEY, CHARGE_ID),
                () -> getPaymentResponse(API_KEY, CHARGE_ID),
                () -> getPaymentResponse(API_KEY, CHARGE_ID)
        );

        List<ValidatableResponse> finishedTasks = invokeAll(tasks);

        assertThat(finishedTasks, hasItem(aResponse(200)));
        assertThat(finishedTasks, hasItem(anErrorResponse(429, "P0900", "Too many requests")));
    }

    @Test
    public void getPaymentEvents_whenRateLimitIsReached_shouldReturn429Response() throws Exception {

        connectorMock.respondWithChargeEventsFound(GATEWAY_ACCOUNT_ID, CHARGE_ID, EVENTS);

        List<Callable<ValidatableResponse>> tasks = Arrays.asList(
                () -> getPaymentEventsResponse(API_KEY, CHARGE_ID),
                () -> getPaymentEventsResponse(API_KEY, CHARGE_ID),
                () -> getPaymentEventsResponse(API_KEY, CHARGE_ID),
                () -> getPaymentEventsResponse(API_KEY, CHARGE_ID)
        );

        List<ValidatableResponse> finishedTasks = invokeAll(tasks);

        assertThat(finishedTasks, hasItem(aResponse(200)));
        assertThat(finishedTasks, hasItem(anErrorResponse(429, "P0900", "Too many requests")));
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

        connectorMock.respondOk_whenSearchCharges(GATEWAY_ACCOUNT_ID, REFERENCE, null, null, null, null, null, null, null, null, payments);

        List<Callable<ValidatableResponse>> tasks = Arrays.asList(
                () -> searchPayments(API_KEY, ImmutableMap.of("reference", REFERENCE)),
                () -> searchPayments(API_KEY, ImmutableMap.of("reference", REFERENCE)),
                () -> searchPayments(API_KEY, ImmutableMap.of("reference", REFERENCE))
        );

        List<ValidatableResponse> finishedTasks = invokeAll(tasks);

        assertThat(finishedTasks, hasItem(aResponse(200)));
        assertThat(finishedTasks, hasItem(anErrorResponse(429, "P0900", "Too many requests")));
    }

    @Test
    public void cancelPayment_whenRateLimitIsReached_shouldReturn429Response() throws Exception {

        connectorMock.respondOk_whenCancelCharge(CHARGE_ID, GATEWAY_ACCOUNT_ID);

        List<Callable<ValidatableResponse>> tasks = Arrays.asList(
                () -> postCancelPaymentResponse(API_KEY, CHARGE_ID),
                () -> postCancelPaymentResponse(API_KEY, CHARGE_ID),
                () -> postCancelPaymentResponse(API_KEY, CHARGE_ID)
        );

        List<ValidatableResponse> finishedTasks = invokeAll(tasks);

        assertThat(finishedTasks, hasItem(aResponse(204)));
        assertThat(finishedTasks, hasItem(anErrorResponse(429, "P0900", "Too many requests")));
    }
}
