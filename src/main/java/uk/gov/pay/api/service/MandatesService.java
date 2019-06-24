package uk.gov.pay.api.service;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.api.app.config.PublicApiConfig;
import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.exception.CreateMandateException;
import uk.gov.pay.api.exception.GetMandateException;
import uk.gov.pay.api.model.directdebit.mandates.CreateMandateRequest;
import uk.gov.pay.api.model.directdebit.mandates.MandateConnectorRequest;
import uk.gov.pay.api.model.directdebit.mandates.MandateConnectorResponse;
import uk.gov.pay.api.model.directdebit.mandates.MandateResponse;
import uk.gov.pay.api.model.links.directdebit.MandateLinks;

import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import static java.lang.String.format;
import static uk.gov.pay.api.model.links.directdebit.MandateLinks.MandateLinksBuilder.aMandateLinks;

public class MandatesService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MandatesService.class);

    private final String connectorDDUrl;
    private final Client client;
    private final PublicApiUriGenerator publicApiUriGenerator;

    @Inject
    public MandatesService(Client client,
                           PublicApiConfig configuration,
                           PublicApiUriGenerator publicApiUriGenerator) {
        this.connectorDDUrl = configuration.getConnectorDDUrl();
        this.client = client;
        this.publicApiUriGenerator = publicApiUriGenerator;
    }

    public MandateResponse create(Account account, CreateMandateRequest createMandateRequest) {
        MandateConnectorResponse mandate = createMandate(account, MandateConnectorRequest.from(createMandateRequest));
        MandateLinks mandateLinks = createLinksFromMandateResponse(mandate);
        MandateResponse createMandateResponse = new MandateResponse(mandate, mandateLinks);
        LOGGER.info("Mandate returned (created): [ {} ]", createMandateResponse);
        return createMandateResponse;
    }

    public MandateResponse get(Account account, String mandateId) {
        Response connectorResponse = getMandate(account, mandateId);
        if (isFound(connectorResponse)) {
            MandateConnectorResponse mandate = connectorResponse.readEntity(MandateConnectorResponse.class);
            MandateLinks mandateLinks = createLinksFromMandateResponse(mandate);
            MandateResponse getMandateResponse = new MandateResponse(mandate, mandateLinks);
            LOGGER.info("Mandate returned (get): [ {} ]", getMandateResponse);
            return getMandateResponse;
        }
        throw new GetMandateException(connectorResponse);
    }

    private MandateLinks createLinksFromMandateResponse(MandateConnectorResponse mandate) {
        return aMandateLinks()
                .withSelf(publicApiUriGenerator.getMandateURI(mandate.getMandateId()).toString())
                .withPayments(publicApiUriGenerator.getMandatePaymentsURI(mandate.getMandateId()).toString())
                .withNextUrl(mandate.getLinks())
                .withNextUrlPost(mandate.getLinks())
                .withEvents(publicApiUriGenerator.getMandateEventsURI(mandate.getMandateId()))
                .build();
    }

    MandateConnectorResponse createMandate(Account account, MandateConnectorRequest mandateConnectorRequest) {
        Response response = client.target(getDDConnectorUrl(format("/v1/api/accounts/%s/mandates", account.getName())))
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .post(Entity.entity(mandateConnectorRequest, MediaType.APPLICATION_JSON));

        if (response.getStatus() == HttpStatus.SC_CREATED)
            return response.readEntity(MandateConnectorResponse.class);

        throw new CreateMandateException(response);
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
