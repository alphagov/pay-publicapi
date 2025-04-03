package uk.gov.pay.api.service;

import uk.gov.pay.api.exception.AuthorisationRequestException;
import uk.gov.pay.api.model.AuthorisationRequest;

import jakarta.inject.Inject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Response;

import static org.apache.http.HttpStatus.SC_NO_CONTENT;

public class AuthorisationService {

    private final Client client;
    private final ConnectorUriGenerator connectorUriGenerator;

    @Inject
    public AuthorisationService(Client client, ConnectorUriGenerator connectorUriGenerator) {
        this.client = client;
        this.connectorUriGenerator = connectorUriGenerator;
    }

    public Response authoriseRequest(AuthorisationRequest authorisationRequest) {
        Response response = client
                .target(connectorUriGenerator.authorisationURI())
                .request()
                .post(Entity.json(authorisationRequest));

        if (response.getStatus() != SC_NO_CONTENT) {
            throw new AuthorisationRequestException(response);
        }

        return Response.noContent().build();
    }
}
