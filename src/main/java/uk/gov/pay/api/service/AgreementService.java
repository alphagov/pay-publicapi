package uk.gov.pay.api.service;

import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
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
import uk.gov.pay.api.model.directdebit.agreement.MandateConnectorRequest;
import uk.gov.pay.api.model.directdebit.agreement.MandateConnectorResponse;
import uk.gov.pay.api.model.links.directdebit.AgreementLinks;
import uk.gov.pay.api.utils.JsonStringBuilder;

import static java.lang.String.format;
import static javax.ws.rs.client.Entity.json;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class AgreementService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AgreementService.class);

    private final String connectorDDUrl;
    private final Client client;
    private final PublicApiUriGenerator publicApiUriGenerator;
    @Inject
    public AgreementService(Client client, PublicApiConfig configuration,
            PublicApiUriGenerator publicApiUriGenerator) {
        this.connectorDDUrl = configuration.getConnectorDDUrl();
        this.client = client;
        this.publicApiUriGenerator = publicApiUriGenerator;
    }

    public CreateAgreementResponse create(Account account, CreateAgreementRequest createAgreementRequest) {
        Response connectorResponse = createAgreement(account, MandateConnectorRequest.from(createAgreementRequest));
        if (isCreated(connectorResponse)) {
            MandateConnectorResponse mandate = connectorResponse.readEntity(MandateConnectorResponse.class);
            AgreementLinks agreementLinks = createLinksFromMandateResponse(mandate);
            CreateAgreementResponse createAgreementResponse = CreateAgreementResponse.from(mandate, agreementLinks);
            LOGGER.info("Agreement returned (created): [ {} ]", createAgreementResponse);
            return createAgreementResponse;
        }

        throw new CreateAgreementException(connectorResponse);
    }

    public GetAgreementResponse get(Account account, String agreementId) {
        Response connectorResponse = getAgreement(account, agreementId);
        if (isFound(connectorResponse)) {
            MandateConnectorResponse mandate = connectorResponse.readEntity(MandateConnectorResponse.class);
            AgreementLinks agreementLinks = createLinksFromMandateResponse(mandate);
            GetAgreementResponse createAgreementResponse = GetAgreementResponse.from(mandate, agreementLinks);
            LOGGER.info("Agreement returned (get): [ {} ]", createAgreementResponse);
            return createAgreementResponse;
        }
        throw new GetAgreementException(connectorResponse);
    }

    private AgreementLinks createLinksFromMandateResponse(MandateConnectorResponse mandate) {
        AgreementLinks agreementLinks = new AgreementLinks();
        agreementLinks.addSelf(publicApiUriGenerator.getAgreementURI(mandate.getMandateId()).toString());
        agreementLinks.addKnownLinksValueOf(mandate.getLinks());
        return agreementLinks;
    }

    Response createAgreement(Account account, MandateConnectorRequest mandateConnectorRequest) {
        return client
                .target(getDDConnectorUrl(format("/v1/api/accounts/%s/mandates", account.getName())))
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .post(buildMandateConnectorRequestPayload(mandateConnectorRequest));
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

    private Entity buildMandateConnectorRequestPayload(MandateConnectorRequest requestPayload) {
        JsonStringBuilder jsonStringBuilder = new JsonStringBuilder()
                .add(MandateConnectorRequest.RETURN_URL_FIELD_NAME, requestPayload.getReturnUrl())
                .add(MandateConnectorRequest.AGREEMENT_TYPE_FIELD_NAME, requestPayload.getAgreementType().toString());

        if (isNotBlank(requestPayload.getServiceReference())) {
            jsonStringBuilder.add(MandateConnectorRequest.SERVICE_REFERENCE_FIELD_NAME, requestPayload.getServiceReference());
        }

        return json(jsonStringBuilder.build());
    }

    private boolean isFound(Response connectorResponse) {
        return connectorResponse.getStatus() == HttpStatus.SC_OK;
    }

    private boolean isCreated(Response connectorResponse) {
        return connectorResponse.getStatus() == HttpStatus.SC_CREATED;
    }

    private String getDDConnectorUrl(String urlPath) {
        UriBuilder builder = UriBuilder
                .fromPath(connectorDDUrl)
                .path(urlPath);

        return builder.toString();
    }

}
