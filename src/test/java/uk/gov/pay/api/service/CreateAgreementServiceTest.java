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
import uk.gov.pay.api.model.TokenPaymentType;
import uk.gov.pay.api.model.directdebit.agreement.AgreementStatus;
import uk.gov.pay.api.model.directdebit.agreement.AgreementType;
import uk.gov.pay.api.model.directdebit.agreement.CreateAgreementRequest;
import uk.gov.pay.api.model.directdebit.agreement.CreateAgreementResponse;
import uk.gov.pay.api.model.links.Link;
import uk.gov.pay.api.model.links.PostLink;
import uk.gov.pay.commons.testing.pact.consumers.PactProviderRule;
import uk.gov.pay.commons.testing.pact.consumers.Pacts;

import javax.ws.rs.client.Client;
import java.util.Collections;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CreateAgreementServiceTest {
    private CreateAgreementService createAgreementService;

    @Rule
    public PactProviderRule ddConnectorRule = new PactProviderRule("direct-debit-connector", this);

    @Mock
    private PublicApiConfig configuration;

    @Before
    public void setup() {
        // We will actually send real requests here, which will be intercepted by pact
        when(configuration.getConnectorDDUrl()).thenReturn(ddConnectorRule.getUrl());

        Client client = RestClientFactory.buildClient(new RestClientConfig(false));

        createAgreementService = new CreateAgreementService(client, configuration);
    }

    @Test
    @PactVerification({"direct-debit-connector"})
    @Pacts(pacts = {"publicapi-direct-debit-connector-create-agreement"})
    public void shouldCreateAMandateSuccessfully() {
        Account account = new Account("9ddfcc27-acf5-43f9-92d5-52247540714b", TokenPaymentType.DIRECT_DEBIT);
        CreateAgreementRequest requestPayload = new CreateAgreementRequest(
                "https://example.com/return", AgreementType.ON_DEMAND);
        CreateAgreementResponse agreementResponse = createAgreementService.create(account, requestPayload);

        assertThat(agreementResponse.getAgreementId(), is("test_mandate_id_xyz"));
        assertThat(agreementResponse.getAgreementType(), is(AgreementType.ON_DEMAND));
        assertThat(agreementResponse.getCreatedDate(), is("2016-01-01T12:00:00.000Z"));
        assertThat(agreementResponse.getReturnUrl(), is("https://example.com/return"));
        assertThat(agreementResponse.getState(), is(AgreementStatus.CREATED));
        assertThat(agreementResponse.getLinks().getSelf(), is(new Link("http://localhost:1234/v1/api/accounts/9ddfcc27-acf5-43f9-92d5-52247540714b/mandates/mandateId", "GET")));
        assertThat(agreementResponse.getLinks().getNextUrl(), is(new Link("http://frontend_direct_debit/secure/token_1234567asdf", "GET")));
        PostLink expectedLink = new PostLink("http://frontend_direct_debit/secure/", "POST", "application/x-www-form-urlencoded", Collections.singletonMap("chargeTokenId", "token_1234567asdf"));
        assertThat(agreementResponse.getLinks().getNextUrlPost(), is(expectedLink));
    }
}
