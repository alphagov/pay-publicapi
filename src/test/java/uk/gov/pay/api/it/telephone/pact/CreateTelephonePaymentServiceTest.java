package uk.gov.pay.api.it.telephone.pact;

import au.com.dius.pact.consumer.PactVerification;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.api.app.RestClientFactory;
import uk.gov.pay.api.app.config.PublicApiConfig;
import uk.gov.pay.api.app.config.RestClientConfig;
import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.exception.CreateChargeException;
import uk.gov.pay.api.model.Address;
import uk.gov.pay.api.model.CardPayment;
import uk.gov.pay.api.model.ChargeFromResponse;
import uk.gov.pay.api.model.CreateCardPaymentRequestBuilder;
import uk.gov.pay.api.model.PaymentState;
import uk.gov.pay.api.model.TokenPaymentType;
import uk.gov.pay.api.model.links.Link;
import uk.gov.pay.api.model.links.PaymentWithAllLinks;
import uk.gov.pay.api.model.links.PostLink;
import uk.gov.pay.api.model.telephone.CreateTelephonePaymentRequest;
import uk.gov.pay.api.model.telephone.PaymentOutcome;
import uk.gov.pay.api.model.telephone.Supplemental;
import uk.gov.pay.api.service.ConnectorUriGenerator;
import uk.gov.pay.api.service.telephone.CreateTelephonePaymentService;
import uk.gov.pay.commons.model.ErrorIdentifier;
import uk.gov.pay.commons.model.SupportedLanguage;
import uk.gov.pay.commons.model.charge.ExternalMetadata;
import uk.gov.pay.commons.testing.pact.consumers.PactProviderRule;
import uk.gov.pay.commons.testing.pact.consumers.Pacts;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CreateTelephonePaymentServiceTest {

    private CreateTelephonePaymentService createTelephonePaymentService;
    private static CreateTelephonePaymentRequest.Builder builder = new CreateTelephonePaymentRequest.Builder();

    @Rule
    public PactProviderRule connectorRule = new PactProviderRule("connector", this);

    @Mock
    private PublicApiConfig configuration;

    @Before
    public void setup() {
        when(configuration.getConnectorUrl()).thenReturn(connectorRule.getUrl()); // We will actually send real requests here, which will be intercepted by pact        

        ConnectorUriGenerator connectorUriGenerator = new ConnectorUriGenerator(configuration);
        Client client = RestClientFactory.buildClient(new RestClientConfig(false));

        createTelephonePaymentService = new CreateTelephonePaymentService(client, connectorUriGenerator);
    }

    @Test
    @PactVerification({"connector"})
    @Pacts(pacts = {"publicapi-connector-create-telephone-payment-notification"})
    public void testCreatePaymentWithMetadata() {
        Account account = new Account("123456", TokenPaymentType.CARD);
        var createTelephonePaymentNotification =builder
                .withAmount(12000)
                .withReference("MRPC12345")
                .withDescription("New passport application")
                .withCreatedDate("2018-02-21T16:04:25Z")
                .withAuthorisedDate("2018-02-21T16:05:33Z")
                .withProcessorId("183f2j8923j8")
                .withProviderId("17498-8412u9-1273891239")
                .withAuthCode("auth12345")
                .withPaymentOutcome(new PaymentOutcome("failed", "P0010", new Supplemental("ECKOH01234", "textual message describing error code")))
                .withCardType("master-card")
                .withNameOnCard("J Doe")
                .withEmailAddress("j.doe@example.com")
                .withCardExpiry("02/19")
                .withLastFourDigits("1234")
                .withFirstSixDigits("654321")
                .withTelephoneNumber("+447700900796")
                .build();
        Response connectorResponse = createTelephonePaymentService.getConnectorResponse(account, createTelephonePaymentNotification);
        ChargeFromResponse chargeFromResponse = connectorResponse.readEntity(ChargeFromResponse.class);

        assertThat(chargeFromResponse.getAmount(), is(12000L));
        assertThat(chargeFromResponse.getReference(), is("MRPC12345"));
        assertThat(chargeFromResponse.getDescription(), is("New passport application"));
        assertThat(chargeFromResponse.getCreatedDate(), is("2018-02-21T16:04:25.000Z"));
        assertThat(chargeFromResponse.getAuthorisedDate(), is("2018-02-21T16:05:33.000Z"));
        assertThat(chargeFromResponse.getProcessorId(), is("183f2j8923j8"));
        assertThat(chargeFromResponse.getProviderId(), is("17498-8412u9-1273891239"));
        assertThat(chargeFromResponse.getAuthCode(), is("auth12345"));
        assertThat(chargeFromResponse.getPaymentOutcome().getStatus(), is("failed"));
        assertThat(chargeFromResponse.getPaymentOutcome().getCode().get(), is("P0010"));
        assertThat(chargeFromResponse.getPaymentOutcome().getSupplemental().get().getErrorCode().get(), is("ECKOH01234"));
        assertThat(chargeFromResponse.getPaymentOutcome().getSupplemental().get().getErrorMessage().get(), is("textual message describing error code"));
        assertThat(chargeFromResponse.getCardDetails().getCardBrand(), is("master-card"));
        assertThat(chargeFromResponse.getCardDetails().getCardHolderName(), is("J Doe"));
        assertThat(chargeFromResponse.getCardDetails().getExpiryDate(), is("02/19"));
        assertThat(chargeFromResponse.getCardDetails().getLastDigitsCardNumber(), is("1234"));
        assertThat(chargeFromResponse.getCardDetails().getFirstDigitsCardNumber(), is("654321"));
        assertThat(chargeFromResponse.getCardDetails().getBillingAddress().isPresent(), is(false));
        assertThat(chargeFromResponse.getEmail(), is("j.doe@example.com"));
        assertThat(chargeFromResponse.getTelephoneNumber(), is("+447700900796"));
    }
}
