package uk.gov.pay.api.service;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.api.app.config.PublicApiConfig;
import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.exception.CreateAgreementException;
import uk.gov.pay.api.exception.GetAgreementException;
import uk.gov.pay.api.model.directdebit.agreement.CreateAgreementRequest;
import uk.gov.pay.api.model.directdebit.agreement.CreateAgreementResponse;
import uk.gov.pay.api.model.directdebit.agreement.GetAgreementResponse;
import uk.gov.pay.api.model.directdebit.agreement.MandateConnectorResponse;
import uk.gov.pay.api.utils.JsonStringBuilder;

import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import static java.lang.String.format;
import static javax.ws.rs.client.Entity.json;

public class AgreementService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AgreementService.class);

    private final String connectorDDUrl;
    private Client client;

    @Inject
    public AgreementService(Client client, PublicApiConfig configuration) {
        this.connectorDDUrl = configuration.getConnectorDDUrl();
        this.client = client;
    }

    public CreateAgreementResponse create(Account account, CreateAgreementRequest createAgreementRequest) {
        Response connectorResponse = createAgreement(account, createAgreementRequest);
        if (isCreated(connectorResponse)) {
            MandateConnectorResponse mandate = connectorResponse.readEntity(MandateConnectorResponse.class);
            CreateAgreementResponse createAgreementResponse = CreateAgreementResponse.from(mandate);
            LOGGER.info("Agreement returned (created): [ {} ]", createAgreementResponse);

            return createAgreementResponse;
        }

        throw new CreateAgreementException(connectorResponse);
    }

    public GetAgreementResponse get(Account account, String agreementId) {
        Response connectorResponse = getAgreement(account, agreementId);
        if (isFound(connectorResponse)) {
            MandateConnectorResponse mandate = connectorResponse.readEntity(MandateConnectorResponse.class);
            GetAgreementResponse createAgreementResponse = GetAgreementResponse.from(mandate);
            LOGGER.info("Agreement returned (created): [ {} ]", createAgreementResponse);
            return createAgreementResponse;
        }
        throw new GetAgreementException(connectorResponse);
    }

    private boolean isFound(Response connectorResponse) {
        return connectorResponse.getStatus() == HttpStatus.SC_OK;
    }
    
    private boolean isCreated(Response connectorResponse) {
        return connectorResponse.getStatus() == HttpStatus.SC_CREATED;
    }

    private Response createAgreement(Account account, CreateAgreementRequest createAgreementRequest) {
        return client
                .target(getDDConnectorUrl(format("/v1/api/accounts/%s/mandates", account.getName())))
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .post(buildAgreementRequestPayload(createAgreementRequest));
    }

    private Response getAgreement(Account account, String agreementId) {
        return client
                .target(getDDConnectorUrl(format("/v1/api/accounts/%s/mandates/%s",
                        account.getName(),
                        agreementId)))
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .get();
    }
    
    private String getDDConnectorUrl(String urlPath) {
        UriBuilder builder = UriBuilder
                .fromPath(connectorDDUrl)
                .path(urlPath);

        return builder.toString();
    }

    private Entity buildAgreementRequestPayload(CreateAgreementRequest requestPayload) {
        return json(new JsonStringBuilder()
                .add(CreateAgreementRequest.RETURN_URL_FIELD_NAME, requestPayload.getReturnUrl())
                .add(CreateAgreementRequest.AGREEMENT_TYPE_FIELD_NAME, requestPayload.getAgreementType().toString())
                .build());
    }

}
