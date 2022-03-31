package uk.gov.pay.api.service;

import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.pay.api.agreement.model.AgreementResponse;
import uk.gov.pay.api.agreement.model.CreateAgreementRequest;
import uk.gov.pay.api.agreement.model.builder.AgreementResponseBuilder;
import uk.gov.pay.api.agreement.service.AgreementService;
import uk.gov.pay.api.auth.Account;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class CreateAgreementServiceTest {
    private static final String REFERENCE_ID = "test";
    public static final String AGREEMENT_ID = "12345678901234567890123456";
    private AgreementService service;
    private  Client client;
    private WebTarget target;
    private Account account;
    private  ConnectorUriGenerator connectorUriGenerator;
    private   Invocation.Builder builder;
    private  Response connectorResponse;
    private AgreementResponse agreementResponse;

    @Before
    public void setUp() {
        client = mock(Client.class);
        target = mock(WebTarget.class);
        account = mock(Account.class);
        builder = mock(Invocation.Builder.class);
        connectorResponse = mock(Response.class);
        connectorUriGenerator = mock(ConnectorUriGenerator.class);
        agreementResponse = new AgreementResponseBuilder().withReference(REFERENCE_ID)
                .withAgreementId(AGREEMENT_ID).build();
        service = new AgreementService(client, connectorUriGenerator);
    }

    @Test
    public void shouldCreateAnAgreement() {
        when(connectorUriGenerator.getAgreementURI(account)).thenReturn("uri");
        when(connectorResponse.getStatus()).thenReturn(HttpStatus.SC_CREATED);
        when(target.request()).thenReturn(builder);
        when(builder.accept(MediaType.APPLICATION_JSON)).thenReturn(builder);
        when(builder.post(any())).thenReturn(connectorResponse);
        when(client.target(anyString())).thenReturn(target);
        when(connectorResponse.readEntity(AgreementResponse.class)).thenReturn(agreementResponse);
        CreateAgreementRequest agreementCreateRequest = new CreateAgreementRequest(REFERENCE_ID);
        AgreementResponse agreementResponseFromService = service.create(account, agreementCreateRequest);
        assertThat(agreementResponseFromService.getAgreementId(), is(AGREEMENT_ID));
        assertThat(agreementResponseFromService.getReference(), is(REFERENCE_ID));
    }
}

