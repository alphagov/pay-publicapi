package uk.gov.pay.api.agreement.service;

import org.apache.http.HttpStatus;
import uk.gov.pay.api.agreement.model.Agreement;
import uk.gov.pay.api.agreement.model.AgreementResponse;
import uk.gov.pay.api.agreement.model.CreateAgreementRequest;
import uk.gov.pay.api.agreement.model.builder.AgreementResponseBuilder;
import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.exception.CreateAgreementException;
import uk.gov.pay.api.service.ConnectorUriGenerator;
import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import static javax.ws.rs.client.Entity.json;

public class AgreementService {

    private final Client client;
    private final ConnectorUriGenerator connectorUriGenerator;

    @Inject
    public AgreementService(Client client, ConnectorUriGenerator connectorUriGenerator) {
        this.client = client;
        this.connectorUriGenerator = connectorUriGenerator;
    }

    public AgreementResponse create(Account account, CreateAgreementRequest createAgreementRequest) {
        Response connectorResponse = createAgreement(account, createAgreementRequest);

        if (!createdSuccessfully(connectorResponse)) {
            throw new CreateAgreementException(connectorResponse);
        }
        
        AgreementResponse agreementResponse = connectorResponse.readEntity(AgreementResponse.class);
        return buildResponseModel(Agreement.from(agreementResponse));
    }
    
    private AgreementResponse buildResponseModel(Agreement agreementFromConnector) {
        return new AgreementResponseBuilder().
                withAgreementId(agreementFromConnector.getAgreementId())
                .withReference(agreementFromConnector.getReference())
                .build();
    }

    private boolean createdSuccessfully(Response connectorResponse) {
        return connectorResponse.getStatus() == HttpStatus.SC_CREATED;
    }

    private Response createAgreement(Account account, CreateAgreementRequest agreementCreateRequest) {
        return client
                .target(connectorUriGenerator.getAgreementURI(account))
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .post(buildAgreementCreateRequestPayload(agreementCreateRequest));
    }

    private Entity buildAgreementCreateRequestPayload(CreateAgreementRequest requestPayload) {
        return json(requestPayload.toConnectorPayload());
    }
}


