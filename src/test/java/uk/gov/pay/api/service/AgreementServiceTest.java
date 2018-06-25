package uk.gov.pay.api.service;

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
import uk.gov.pay.api.model.PaymentConnectorResponseLink;
import uk.gov.pay.api.model.TokenPaymentType;
import uk.gov.pay.api.model.directdebit.agreement.AgreementType;
import uk.gov.pay.api.model.directdebit.agreement.MandateConnectorRequest;
import uk.gov.pay.api.model.directdebit.agreement.MandateConnectorResponse;
import uk.gov.pay.api.model.directdebit.agreement.MandateState;
import uk.gov.pay.api.model.directdebit.agreement.MandateType;
import uk.gov.pay.commons.testing.pact.consumers.PactProviderRule;
import uk.gov.pay.commons.testing.pact.consumers.Pacts;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import java.util.Collections;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AgreementServiceTest {

    private static final String MANDATE_ID = "test_mandate_id_xyz";

    private AgreementService agreementService;

    @Rule
    public PactProviderRule ddConnectorRule = new PactProviderRule("direct-debit-connector", this);

    @Mock
    private PublicApiConfig mockConfiguration;
    @Mock
    private PublicApiUriGenerator mockPublicApiUriGenerator;

    @Before
    public void setup() {
        // We will actually send real requests here, which will be intercepted by pact
        when(mockConfiguration.getConnectorDDUrl()).thenReturn(ddConnectorRule.getUrl());
        Client client = RestClientFactory.buildClient(new RestClientConfig(false));
        agreementService = new AgreementService(client, mockConfiguration, mockPublicApiUriGenerator);
    }

    @Test
    @PactVerification({"direct-debit-connector"})
    @Pacts(pacts = {"publicapi-direct-debit-connector-create-agreement-without-reference"})
    public void shouldCreateAMandateSuccessfullyWithoutReference() {
        Account account = new Account("9ddfcc27-acf5-43f9-92d5-52247540714b", TokenPaymentType.DIRECT_DEBIT);
        MandateConnectorRequest mandateConnectorRequest = new MandateConnectorRequest(
                "https://example.com/return",
                AgreementType.ON_DEMAND,
                null
        );
        Response connectorResponse = agreementService.createMandate(account, mandateConnectorRequest);
        MandateConnectorResponse mandateConnectorResponse = connectorResponse.readEntity(MandateConnectorResponse.class);

        assertThat(mandateConnectorResponse.getMandateId(), is(MANDATE_ID));
        assertThat(mandateConnectorResponse.getMandateType(), is(MandateType.ON_DEMAND));
        assertThat(mandateConnectorResponse.getCreatedDate(), is("2016-01-01T12:00:00.000Z"));
        assertThat(mandateConnectorResponse.getReturnUrl(), is("https://example.com/return"));
        assertThat(mandateConnectorResponse.getState(), is(new MandateState("created", false)));
        assertThat(mandateConnectorResponse.getLinks().get(0), is(new PaymentConnectorResponseLink(
                "self",
                "http://localhost:1234/v1/api/accounts/9ddfcc27-acf5-43f9-92d5-52247540714b/mandates/" + MANDATE_ID,
                "GET",
                null,
                null
        )));
        assertThat(mandateConnectorResponse.getLinks().get(1), is(new PaymentConnectorResponseLink(
                "next_url",
                "http://frontend_direct_debit/secure/token_1234567asdf",
                "GET",
                null,
                null
        )));
        assertThat(mandateConnectorResponse.getLinks().get(2), is(new PaymentConnectorResponseLink(
                "next_url_post",
                "http://frontend_direct_debit/secure/",
                "POST",
                "application/x-www-form-urlencoded",
                Collections.singletonMap("chargeTokenId", "token_1234567asdf")
        )));
    }

    @Test
    @PactVerification({"direct-debit-connector"})
    @Pacts(pacts = {"publicapi-direct-debit-connector-create-agreement-with-reference"})
    public void shouldCreateAMandateSuccessfullyWithReference() {
        Account account = new Account("7959d395-e720-4081-9e8a-a534cf76460a", TokenPaymentType.DIRECT_DEBIT);
        MandateConnectorRequest mandateConnectorRequest = new MandateConnectorRequest(
                "https://example.com/return",
                AgreementType.ON_DEMAND,
                "serviceref123"
        );
        Response connectorResponse = agreementService.createMandate(account, mandateConnectorRequest);
        MandateConnectorResponse mandateConnectorResponse = connectorResponse.readEntity(MandateConnectorResponse.class);

        assertThat(mandateConnectorResponse.getMandateId(), is(MANDATE_ID));
        assertThat(mandateConnectorResponse.getMandateType(), is(MandateType.ON_DEMAND));
        assertThat(mandateConnectorResponse.getServiceReference(), is("serviceref123"));
        assertThat(mandateConnectorResponse.getCreatedDate(), is("2016-01-01T12:00:00.000Z"));
        assertThat(mandateConnectorResponse.getReturnUrl(), is("https://example.com/return"));
        assertThat(mandateConnectorResponse.getState(), is(new MandateState("created", false)));
        assertThat(mandateConnectorResponse.getLinks().get(0), is(new PaymentConnectorResponseLink(
                "self",
                "http://localhost:1234/v1/api/accounts/7959d395-e720-4081-9e8a-a534cf76460a/mandates/" + MANDATE_ID,
                "GET",
                null,
                null
        )));
        assertThat(mandateConnectorResponse.getLinks().get(1), is(new PaymentConnectorResponseLink(
                "next_url",
                "http://frontend_direct_debit/secure/token_1234567asdf",
                "GET",
                null,
                null
        )));
        assertThat(mandateConnectorResponse.getLinks().get(2), is(new PaymentConnectorResponseLink(
                "next_url_post",
                "http://frontend_direct_debit/secure/",
                "POST",
                "application/x-www-form-urlencoded",
                Collections.singletonMap("chargeTokenId", "token_1234567asdf")
        )));
    }

    @Test
    @PactVerification({"direct-debit-connector"})
    @Pacts(pacts = {"publicapi-direct-debit-connector-get-agreement"})
    public void shouldGetAMandateSuccessfully() {
        Account account = new Account("9ddfcc27-acf5-43f9-92d5-52247540714c", TokenPaymentType.DIRECT_DEBIT);
        Response connectorResponse = agreementService.getMandate(account, MANDATE_ID);
        MandateConnectorResponse mandateConnectorResponse = connectorResponse.readEntity(MandateConnectorResponse.class);

        assertThat(mandateConnectorResponse.getMandateId(), is(MANDATE_ID));
        assertThat(mandateConnectorResponse.getMandateType(), is(MandateType.ON_DEMAND));
        assertThat(mandateConnectorResponse.getReturnUrl(), is("https://example.com/return"));
        assertThat(mandateConnectorResponse.getState(), is(new MandateState("created", false)));
        assertThat(mandateConnectorResponse.getLinks().get(0), is(new PaymentConnectorResponseLink(
                "self",
                "http://localhost:1234/v1/api/accounts/9ddfcc27-acf5-43f9-92d5-52247540714c/mandates/" + MANDATE_ID,
                "GET",
                null,
                null
        )));
        assertThat(mandateConnectorResponse.getLinks().get(1), is(new PaymentConnectorResponseLink(
                "next_url",
                "http://frontend_direct_debit/secure/token_1234567asdf",
                "GET",
                null,
                null
        )));
        assertThat(mandateConnectorResponse.getLinks().get(2), is(new PaymentConnectorResponseLink(
                "next_url_post",
                "http://frontend_direct_debit/secure/",
                "POST",
                "application/x-www-form-urlencoded",
                Collections.singletonMap("chargeTokenId", "token_1234567asdf")
        )));
    }
}
