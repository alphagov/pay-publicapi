package uk.gov.pay.api.resources;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.model.Address;
import uk.gov.pay.api.model.CardDetails;
import uk.gov.pay.api.model.CreateCardPaymentRequestBuilder;
import uk.gov.pay.api.model.PaymentSettlementSummary;
import uk.gov.pay.api.model.PaymentState;
import uk.gov.pay.api.model.RefundSummary;
import uk.gov.pay.api.model.TokenPaymentType;
import uk.gov.pay.api.model.links.PaymentWithAllLinks;
import uk.gov.pay.api.service.CancelPaymentService;
import uk.gov.pay.api.service.CapturePaymentService;
import uk.gov.pay.api.service.CreatePaymentService;
import uk.gov.pay.api.service.GetPaymentEventsService;
import uk.gov.pay.api.service.GetPaymentService;
import uk.gov.pay.api.service.PaymentSearchService;
import uk.gov.pay.api.service.PublicApiUriGenerator;
import uk.gov.service.payments.commons.model.AuthorisationMode;
import uk.gov.service.payments.commons.model.SupportedLanguage;

import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PaymentsResourceCreatePaymentTest {

    @InjectMocks
    private PaymentsResource paymentsResource;

    @Mock
    private CreatePaymentService createPaymentService;

    @Mock
    private PaymentSearchService paymentSearchService;

    @Mock
    private PublicApiUriGenerator publicApiUriGenerator;

    @Mock
    private GetPaymentService getPaymentService;

    @Mock
    private CapturePaymentService capturePaymentService;

    @Mock
    private CancelPaymentService cancelPaymentService;

    @Mock
    private GetPaymentEventsService getPaymentEventsService;

    private final String PAYMENT_URI = "https://my.link/v1/payments/abc123";

    @BeforeEach
    public void setup() {
        when(publicApiUriGenerator.getPaymentURI(anyString())).thenReturn(URI.create(PAYMENT_URI));
    }

    @Test
    void createNewPayment_withCardPayment_invokesCreatePaymentService() {
        Account account = new Account("foo", TokenPaymentType.CARD, "a-token-link");
        var createPaymentRequest = CreateCardPaymentRequestBuilder.builder()
                .amount(100)
                .returnUrl("https://somewhere.test")
                .reference("my_ref")
                .description("New passport")
                .build();

        PaymentWithAllLinks injectedResponse = aSuccessfullyCreatedPayment();

        when(createPaymentService.create(account, createPaymentRequest)).thenReturn(injectedResponse);

        Response newPayment = paymentsResource.createNewPayment(account, createPaymentRequest);

        assertThat(newPayment.getStatus(), is(201));
        assertThat(newPayment.getLocation(), is(URI.create(PAYMENT_URI)));
        assertThat(newPayment.getEntity(), sameInstance(injectedResponse));
    }

    @NotNull
    private PaymentWithAllLinks aSuccessfullyCreatedPayment() {
        final Address cardholderAddress = new Address("123 Acacia Ave", "", "", "London", "GB");
        return new PaymentWithAllLinks(
                "abc123",
                100L,
                new PaymentState("created", false),
                "https://somewhere.test",
                "New Passport",
                "my_ref",
                "made.up@example.com",
                "sandbox",
                "2018-01-01T11:12:13Z",
                SupportedLanguage.ENGLISH,
                false,
                false,
                new RefundSummary(),
                new PaymentSettlementSummary(),
                new CardDetails("9876", "482393", "Anne Onymous", "12/20", cardholderAddress, "visa", null),
                Collections.emptyList(),
                URI.create(PAYMENT_URI),
                URI.create(PAYMENT_URI + "/events"),
                URI.create(PAYMENT_URI + "/cancel"),
                URI.create(PAYMENT_URI + "/refunds"),
                URI.create(PAYMENT_URI + "/capture"),
                null,
                null,
                "providerId",
                null,
                null,
                null,
                null,
                null,
                AuthorisationMode.WEB);
    }
}
