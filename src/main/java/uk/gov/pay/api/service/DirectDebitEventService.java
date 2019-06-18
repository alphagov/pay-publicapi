package uk.gov.pay.api.service;

import org.apache.http.HttpStatus;
import uk.gov.pay.api.exception.ConnectorResponseErrorException;
import uk.gov.pay.api.model.links.directdebit.DirectDebitEvent;
import uk.gov.pay.api.model.links.directdebit.DirectDebitEventsResponse;

import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class DirectDebitEventService {

    private final PublicApiUriGenerator publicApiUriGenerator;
    private final Client client;

    @Inject
    public DirectDebitEventService(PublicApiUriGenerator publicApiUriGenerator, Client client) {
        this.publicApiUriGenerator = publicApiUriGenerator;
        this.client = client;
    }

    public DirectDebitEventsResponse getResponse(String searchUri) {
        Response ddConnectorResponse = client.target(searchUri)
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .get();

        if (ddConnectorResponse.getStatus() == HttpStatus.SC_OK) {
            DirectDebitEventsResponse eventsResponse = ddConnectorResponse.readEntity(DirectDebitEventsResponse.class);
            updatePaginationLinks(eventsResponse);
            addAgreementAndPaymentLinks(eventsResponse);
            return eventsResponse;
        }
        throw new ConnectorResponseErrorException(ddConnectorResponse);
    }

    private void addAgreementAndPaymentLinks(DirectDebitEventsResponse response) {

        for (DirectDebitEvent event : response.getResults()) {
            String agreementLink = null;
            String paymentLink = null;

            if (event.getMandateExternalId() != null) {
                agreementLink = publicApiUriGenerator.getMandateURI(event.getMandateExternalId()).toString();
            }

            if (event.getPaymentExternalId() != null) {
                paymentLink = publicApiUriGenerator.getPaymentURI(event.getPaymentExternalId()).toString();
            }

            event.setLinks(new DirectDebitEvent.Links(agreementLink, paymentLink));
        }
    }

    private void updatePaginationLinks(DirectDebitEventsResponse response) {
        DirectDebitEventsResponse.DirectDebitEventsPagination links = response.getPaginationLinks();

        if (links.getSelfLink() != null) {
            response.getPaginationLinks().getFirstLink().setHref(updateHost(response.getPaginationLinks().getFirstLink().getHref()));
        }

        if (links.getPrevLink() != null) {
            response.getPaginationLinks().getPrevLink().setHref(updateHost(response.getPaginationLinks().getPrevLink().getHref()));
        }

        if (links.getNextLink() != null) {
            response.getPaginationLinks().getNextLink().setHref(updateHost(response.getPaginationLinks().getNextLink().getHref()));
        }

        if (links.getLastLink() != null) {
            response.getPaginationLinks().getLastLink().setHref(updateHost(response.getPaginationLinks().getLastLink().getHref()));
        }

        if (links.getFirstLink() != null) {
            response.getPaginationLinks().getSelfLink().setHref(updateHost(response.getPaginationLinks().getSelfLink().getHref()));
        }
    }

    private String updateHost(String url) {
        if (url != null) {
            return publicApiUriGenerator.convertHostToPublicAPI(url);
        }
        return null;
    }
}
