package uk.gov.pay.api.resources;

import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.api.app.config.PublicApiConfig;
import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.model.CreatePaymentRequest;
import uk.gov.pay.api.model.PaymentWithAllLinksCreator;
import uk.gov.pay.api.model.TokenPaymentType;
import uk.gov.pay.api.model.generated.Address;
import uk.gov.pay.api.model.generated.CardDetails;
import uk.gov.pay.api.model.generated.PaymentState;
import uk.gov.pay.api.model.generated.PaymentWithAllLinks;
import uk.gov.pay.api.model.generated.RefundSummary;
import uk.gov.pay.api.model.generated.SettlementSummary;
import uk.gov.pay.api.model.ValidCreatePaymentRequest;
import uk.gov.pay.api.service.CancelPaymentService;
import uk.gov.pay.api.service.CapturePaymentService;
import uk.gov.pay.api.service.ConnectorUriGenerator;
import uk.gov.pay.api.service.CreatePaymentService;
import uk.gov.pay.api.service.GetPaymentService;
import uk.gov.pay.api.service.PaymentSearchService;
import uk.gov.pay.api.service.PublicApiUriGenerator;
import uk.gov.pay.commons.model.SupportedLanguage;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PaymentsResourceCreatePaymentTest {

    private PaymentsResource paymentsResource;

    @Mock
    private Client client;

    @Mock
    private CreatePaymentService createPaymentService;

    @Mock
    private PaymentSearchService paymentSearchService;

    @Mock
    private PublicApiUriGenerator publicApiUriGenerator;

    @Mock
    private ConnectorUriGenerator connectorUriGenerator;

    @Mock
    private GetPaymentService getPaymentService;

    @Mock
    private CapturePaymentService capturePaymentService;
    
    @Mock
    private PublicApiConfig publicApiConfig;
    
    @Mock
    private CancelPaymentService cancelPaymentService;

    private final String paymentUri = "https://my.link/v1/payments/abc123";

    @Before
    public void setup() {
        paymentsResource = new PaymentsResource(client,
                createPaymentService,
                paymentSearchService,
                publicApiUriGenerator,
                connectorUriGenerator,
                getPaymentService,
                capturePaymentService,
                cancelPaymentService,
                publicApiConfig);
        when(publicApiUriGenerator.getPaymentURI(anyString())).thenReturn(URI.create(paymentUri));
    }

    @Test
    public void createNewPayment_withCardPayment_invokesCreatePaymentService() throws Exception {
        final Account account = new Account("foo", TokenPaymentType.CARD);
        final ValidCreatePaymentRequest createPaymentRequest = new ValidCreatePaymentRequest(CreatePaymentRequest.builder()
                .amount(100)
                .returnUrl("https://somewhere.test")
                .reference("my_ref")
                .description("New Passport")
                .build());
        PaymentWithAllLinks injectedResponse = aSuccessfullyCreatedPayment();

        when(createPaymentService.create(account, createPaymentRequest)).thenReturn(injectedResponse);

        final Response newPayment = paymentsResource.createNewPayment(createPaymentRequest, account);

        assertThat(newPayment.getStatus(), is(201));
        assertThat(newPayment.getLocation(), is(URI.create(paymentUri)));
        assertThat(newPayment.getEntity(), sameInstance(injectedResponse));
    }

    @NotNull
    private PaymentWithAllLinks aSuccessfullyCreatedPayment() {
        final Address cardholderAddress = new Address().line1("123 Acacia Ave").city("London").country("GB");
        return PaymentWithAllLinksCreator.valueOf(
                "abc123",
                100L,
                new PaymentState().status("created").finished(false),
                "https://somewhere.test",
                "New Passport",
                "my_ref",
                "made.up@example.com",
                "sandbox",
                "2018-01-01T11:12:13Z",
                SupportedLanguage.ENGLISH,
                false,
                new RefundSummary(),
                new SettlementSummary(),
                new CardDetails().lastDigitsCardNumber("9876").firstDigitsCardNumber("482393").cardholderName("Anne Onymous").expiryDate("12/20").billingAddress(cardholderAddress).cardBrand("visa"),
                Collections.emptyList(),
                URI.create(paymentUri),
                URI.create(paymentUri + "/events"),
                URI.create(paymentUri + "/cancel"),
                URI.create(paymentUri + "/refunds")
        );
    }
}
