package uk.gov.pay.api.service;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.pay.api.agreement.model.AgreementResponse;
import uk.gov.pay.api.agreement.model.CreateAgreementRequest;
import uk.gov.pay.api.agreement.model.builder.AgreementResponseBuilder;
import uk.gov.pay.api.agreement.service.AgreementService;
import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.model.CreateAgreementRequestBuilder;
import uk.gov.pay.api.utils.mocks.CreateAgreementRequestParams;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static javax.ws.rs.client.Entity.json;
import static javax.ws.rs.core.Response.Status.NO_CONTENT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;
import static uk.gov.pay.api.it.CreateAgreementIT.agreementPayload;
import static uk.gov.pay.api.utils.mocks.CreateAgreementRequestParams.CreateAgreementRequestParamsBuilder.aCreateAgreementRequestParams;

@ExtendWith(MockitoExtension.class)
class AgreementServiceTest {

    private static final String REFERENCE_ID = "test";
    private static final String AGREEMENT_ID = "12345678901234567890123456";
    private static final String AGREEMENTS_CONNECTOR_URI = "/v1/api/accounts/GATEWAY_ACCOUNT_ID/agreements"; // pragma: allowlist secret
    private static final String AGREEMENT_CONNECTOR_CANCEL_URI = AGREEMENTS_CONNECTOR_URI + '/' + AGREEMENT_ID + "/cancel";

    private AgreementService service;
    @Mock
    private Client client;
    @Mock
    private WebTarget target;
    @Mock
    private Account account;
    @Mock
    private ConnectorUriGenerator connectorUriGenerator;
    @Mock
    private Invocation.Builder builder;
    @Mock
    private Response connectorResponse;

    @BeforeEach
    void setUp() {

        service = new AgreementService(client, connectorUriGenerator);
    }

    @Test
    void shouldCreateAgreement() {
        CreateAgreementRequestParams createAgreementRequestParams = aCreateAgreementRequestParams()
                .withReference(REFERENCE_ID)
                .build();
        String payloadToConnector = agreementPayload(createAgreementRequestParams);
        Entity<String> payloadEntity = json(payloadToConnector);
        var agreementResponse = new AgreementResponseBuilder()
                .withReference(REFERENCE_ID)
                .withAgreementId(AGREEMENT_ID).build();
        when(connectorUriGenerator.getAgreementURI(account)).thenReturn(AGREEMENTS_CONNECTOR_URI);
        when(connectorResponse.getStatus()).thenReturn(HttpStatus.SC_CREATED);
        when(target.request()).thenReturn(builder);
        when(builder.accept(MediaType.APPLICATION_JSON)).thenReturn(builder);
        when(builder.post(payloadEntity)).thenReturn(connectorResponse);
        when(client.target(AGREEMENTS_CONNECTOR_URI)).thenReturn(target);
        when(connectorResponse.readEntity(AgreementResponse.class)).thenReturn(agreementResponse);
        CreateAgreementRequest agreementCreateRequest = new CreateAgreementRequest(CreateAgreementRequestBuilder
                .builder().reference(REFERENCE_ID));

        AgreementResponse agreementResponseFromService = service.create(account, agreementCreateRequest);

        assertThat(agreementResponseFromService.getAgreementId(), is(AGREEMENT_ID));
        assertThat(agreementResponseFromService.getReference(), is(REFERENCE_ID));
    }

    @Test
    void shouldCancelAgreement() {
        when(connectorUriGenerator.cancelAgreementURI(account, AGREEMENT_ID)).thenReturn(AGREEMENT_CONNECTOR_CANCEL_URI);
        when(connectorResponse.getStatus()).thenReturn(HttpStatus.SC_NO_CONTENT);
        when(target.request()).thenReturn(builder);
        when(builder.post(null)).thenReturn(connectorResponse);
        when(client.target(AGREEMENT_CONNECTOR_CANCEL_URI)).thenReturn(target);

        Response response = service.cancel(account, AGREEMENT_ID);

        assertThat(response.getStatus(), is(NO_CONTENT.getStatusCode()));
    }

}
