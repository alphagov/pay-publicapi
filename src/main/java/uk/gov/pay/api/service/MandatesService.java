package uk.gov.pay.api.service;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.exception.CreateMandateException;
import uk.gov.pay.api.exception.GetMandateException;
import uk.gov.pay.api.model.directdebit.mandates.CreateMandateRequest;
import uk.gov.pay.api.model.directdebit.mandates.MandateConnectorRequest;
import uk.gov.pay.api.model.directdebit.mandates.MandateConnectorResponse;
import uk.gov.pay.api.model.directdebit.mandates.MandateResponse;
import uk.gov.pay.api.service.directdebit.DirectDebitConnectorUriGenerator;

import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class MandatesService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MandatesService.class);

    private final Client client;
    private final PublicApiUriGenerator publicApiUriGenerator;
    private final DirectDebitConnectorUriGenerator directDebitConnectorUriGenerator;

    @Inject
    public MandatesService(Client client,
                           PublicApiUriGenerator publicApiUriGenerator,
                           DirectDebitConnectorUriGenerator directDebitConnectorUriGenerator) {
        this.client = client;
        this.publicApiUriGenerator = publicApiUriGenerator;
        this.directDebitConnectorUriGenerator = directDebitConnectorUriGenerator;
    }

    public MandateResponse create(Account account, CreateMandateRequest createMandateRequest) {
        MandateConnectorResponse mandate = createMandate(account, MandateConnectorRequest.from(createMandateRequest));
        MandateResponse createMandateResponse = new MandateResponse(mandate, publicApiUriGenerator);
        LOGGER.info("Mandate returned (created): [ {} ]", createMandateResponse);
        return createMandateResponse;
    }

    public MandateResponse get(Account account, String mandateId) {
        Response connectorResponse = getMandate(account, mandateId);
        if (isFound(connectorResponse)) {
            MandateConnectorResponse mandate = connectorResponse.readEntity(MandateConnectorResponse.class);
            MandateResponse getMandateResponse = new MandateResponse(mandate, publicApiUriGenerator);
            LOGGER.info("Mandate returned (get): [ {} ]", getMandateResponse);
            return getMandateResponse;
        }
        throw new GetMandateException(connectorResponse);
    }

    MandateConnectorResponse createMandate(Account account, MandateConnectorRequest mandateConnectorRequest) {
        Response response = client.target(directDebitConnectorUriGenerator.mandatesURI(account))
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .post(Entity.entity(mandateConnectorRequest, MediaType.APPLICATION_JSON));

        if (response.getStatus() == HttpStatus.SC_CREATED)
            return response.readEntity(MandateConnectorResponse.class);

        throw new CreateMandateException(response);
    }

    Response getMandate(Account account, String mandateExternalId) {
        String url = directDebitConnectorUriGenerator.singleMandateURI(account, mandateExternalId);
        return client.target(url).request().accept(MediaType.APPLICATION_JSON).get();
    }

    private boolean isFound(Response connectorResponse) {
        return connectorResponse.getStatus() == HttpStatus.SC_OK;
    }
}
