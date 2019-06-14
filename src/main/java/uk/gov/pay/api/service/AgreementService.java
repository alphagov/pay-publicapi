package uk.gov.pay.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
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

import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import static java.lang.String.format;

public class AgreementService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AgreementService.class);

    private final String connectorDDUrl;
    private final Client client;
    private final PublicApiUriGenerator publicApiUriGenerator;

    @Inject
    public AgreementService(Client client,
                            PublicApiConfig configuration,
                            PublicApiUriGenerator publicApiUriGenerator) {
        this.connectorDDUrl = configuration.getConnectorDDUrl();
        this.client = client;
        this.publicApiUriGenerator = publicApiUriGenerator;
    }

    public CreateAgreementResponse create(Account account, CreateAgreementRequest createAgreementRequest) {
        MandateConnectorResponse mandate = createMandate(account, MandateConnectorRequest.from(createAgreementRequest));
        AgreementLinks agreementLinks = createLinksFromMandateResponse(mandate);
        CreateAgreementResponse createAgreementResponse = CreateAgreementResponse.from(mandate, agreementLinks);
        LOGGER.info("Agreement returned (created): [ {} ]", createAgreementResponse);
        return createAgreementResponse;
    }

    public GetAgreementResponse get(Account account, String agreementId) {
        Response connectorResponse = getMandate(account, agreementId);
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

    MandateConnectorResponse createMandate(Account account, MandateConnectorRequest mandateConnectorRequest) {
        Response response = client.target(getDDConnectorUrl(format("/v1/api/accounts/%s/mandates", account.getName())))
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .post(Entity.entity(mandateConnectorRequest, MediaType.APPLICATION_JSON));

        if (response.getStatus() == HttpStatus.SC_CREATED)
            return response.readEntity(MandateConnectorResponse.class);

        throw new CreateAgreementException(response);
    }

    Response getMandate(Account account, String mandateExternalId) {
        String url = getDDConnectorUrl(format("/v1/api/accounts/%s/mandates/%s", account.getName(), mandateExternalId));
        return client.target(url).request().accept(MediaType.APPLICATION_JSON).get();
    }

    private boolean isFound(Response connectorResponse) {
        return connectorResponse.getStatus() == HttpStatus.SC_OK;
    }
    
    private String getDDConnectorUrl(String urlPath) {
        return UriBuilder.fromPath(connectorDDUrl).path(urlPath).toString();
    }
}
