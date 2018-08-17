package uk.gov.pay.api.resources;

import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.model.Address;
import uk.gov.pay.api.model.CardDetails;
import uk.gov.pay.api.model.CreatePaymentRequest;
import uk.gov.pay.api.model.PaymentState;
import uk.gov.pay.api.model.RefundSummary;
import uk.gov.pay.api.model.SettlementSummary;
import uk.gov.pay.api.model.TokenPaymentType;
import uk.gov.pay.api.model.ValidCreatePaymentRequest;
import uk.gov.pay.api.model.links.PaymentWithAllLinks;
import uk.gov.pay.api.service.ConnectorUriGenerator;
import uk.gov.pay.api.service.CreatePaymentService;
import uk.gov.pay.api.service.PaymentSearchService;
import uk.gov.pay.api.service.PublicApiUriGenerator;

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

    private final String paymentUri = "https://my.link/v1/payments/abc123";

    @Before
    public void setup() {
        paymentsResource = new PaymentsResource(client,
                createPaymentService,
                paymentSearchService,
                publicApiUriGenerator,
                connectorUriGenerator);
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

        final Response newPayment = paymentsResource.createNewPayment(account, createPaymentRequest);

        assertThat(newPayment.getStatus(), is(201));
        assertThat(newPayment.getLocation(), is(URI.create(paymentUri)));
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
                new RefundSummary(),
                new SettlementSummary(),
                new CardDetails("9876", "Anne Onymous", "12/20", cardholderAddress, "visa"),
                Collections.emptyList(),
                URI.create(paymentUri),
                URI.create(paymentUri + "/events"),
                URI.create(paymentUri + "/cancel"),
                URI.create(paymentUri + "/refunds")
        );
    }
}
