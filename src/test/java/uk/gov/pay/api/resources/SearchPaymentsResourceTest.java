package uk.gov.pay.api.resources;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.auth.oauth.OAuthCredentialAuthFilter;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;
import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.slf4j.LoggerFactory;
import uk.gov.pay.api.app.config.PublicApiConfig;
import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.auth.AccountAuthenticator;
import uk.gov.pay.api.exception.mapper.SearchChargesExceptionMapper;
import uk.gov.pay.api.model.publicauth.AuthResponse;
import uk.gov.pay.api.service.CancelPaymentService;
import uk.gov.pay.api.service.CapturePaymentService;
import uk.gov.pay.api.service.CreatePaymentService;
import uk.gov.pay.api.service.GetPaymentEventsService;
import uk.gov.pay.api.service.GetPaymentService;
import uk.gov.pay.api.service.PaymentSearchService;
import uk.gov.pay.api.service.PublicApiUriGenerator;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

import static java.lang.String.format;
import static jakarta.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(DropwizardExtensionsSupport.class)
class SearchPaymentsResourceTest {

    private static final String API_TOKEN = "topsecretapitoken";
    
    private final Client mockClient = mock(Client.class);
    private final WebTarget mockWebTarget = mock(WebTarget.class);
    private final Invocation.Builder mockBuilder = mock(Invocation.Builder.class);
    private final Response mockResponse = mock(Response.class);
    private final Account mockAccount = mock(Account.class);
    private final Appender<ILoggingEvent> mockLogAppender = mock(Appender.class);
    private final ArgumentCaptor<LoggingEvent> loggingEventArgumentCaptor = ArgumentCaptor.forClass(LoggingEvent.class);
    private final CreatePaymentService mockCreatePaymentService = mock(CreatePaymentService.class);
    private final PaymentSearchService mockPaymentSearchService = mock(PaymentSearchService.class);
    private final PublicApiUriGenerator mockPublicApiUriGenerator  = mock(PublicApiUriGenerator.class);
    private final GetPaymentService mockGetPaymentService = mock(GetPaymentService.class);
    private final CapturePaymentService mockCapturePaymentService = mock(CapturePaymentService.class);
    private final CancelPaymentService mockCancelPaymentService = mock(CancelPaymentService.class);
    private final GetPaymentEventsService mockGetPaymentEventsService = mock(GetPaymentEventsService.class);
    
    private final ResourceExtension paymentsResource = ResourceExtension.builder()
            .setRegisterDefaultExceptionMappers(false)
            .addProvider(SearchChargesExceptionMapper.class)
            .addProvider(new AuthDynamicFeature(
                    new OAuthCredentialAuthFilter.Builder<Account>()
                            .setAuthenticator(new AccountAuthenticator(mockClient, mock(PublicApiConfig.class)))
                            .setPrefix("Bearer")
                            .buildAuthFilter()))
            .addProvider(new AuthValueFactoryProvider.Binder<>(Account.class))
            .addProvider(RolesAllowedDynamicFeature.class)
            .addProvider(new AbstractBinder() {
                @Override
                protected void configure() {
                    bind(mockAccount).to(Account.class);
                }
            })
            .addResource(new PaymentsResource(
                    mockCreatePaymentService,
                    mockPaymentSearchService,
                    mockPublicApiUriGenerator,
                    mockGetPaymentService,
                    mockCapturePaymentService,
                    mockCancelPaymentService,
                    mockGetPaymentEventsService
            ))
            .build();
    
    @BeforeEach
    void setUp() {
        when(mockClient.target(nullable(String.class))).thenReturn(mockWebTarget);
        when(mockWebTarget.request()).thenReturn(mockBuilder);
        when(mockBuilder.header(AUTHORIZATION, format("Bearer %s", API_TOKEN))).thenReturn(mockBuilder);
        when(mockBuilder.accept(MediaType.APPLICATION_JSON)).thenReturn(mockBuilder);
        when(mockBuilder.get()).thenReturn(mockResponse);
        when(mockResponse.getStatus()).thenReturn(200);
        when(mockResponse.readEntity(AuthResponse.class)).thenReturn(mock(AuthResponse.class));
        Logger logger = (Logger) LoggerFactory.getLogger(PaymentsResource.class);
        logger.setLevel(Level.INFO);
        logger.addAppender(mockLogAppender);
    }
    
    @Test
    void emailAndCardholderName_areRedactedInLogs() {
        when(mockPaymentSearchService.searchLedgerPayments(any(), any())).thenReturn(Response.ok().build());
        try (Response response = paymentsResource
                .target("/v1/payments")
                .queryParam("email", "email@example.com")
                .queryParam("cardholder_name", "Mr. R E Dacted")
                .queryParam("reference", "ishouldbeinthelog123")
                .request()
                .header("Authorization", format("Bearer %s", API_TOKEN))
                .get()) {

            assertThat(response.getStatus(), is(200));
            verify(mockLogAppender).doAppend(loggingEventArgumentCaptor.capture());
            List<LoggingEvent> logStatement = loggingEventArgumentCaptor.getAllValues();
            String expectedLogMessage = "Payments search request - [ reference: ishouldbeinthelog123, email: REDACTED, status: null, card_brand null, fromDate: null, toDate: null, page: null, display_size: null, cardholder_name: REDACTED, first_digits_card_number: null, last_digits_card_number: null, from_settled_date: null, to_settled_date: null, agreement_id: null ]";
            assertThat(logStatement.getFirst().getFormattedMessage(), is(expectedLogMessage));
        }
    }
}
