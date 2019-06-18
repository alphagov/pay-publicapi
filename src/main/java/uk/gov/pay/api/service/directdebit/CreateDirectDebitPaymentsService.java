package uk.gov.pay.api.service.directdebit;

import org.apache.http.HttpStatus;
import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.exception.CreateChargeException;
import uk.gov.pay.api.model.CreateDirectDebitPaymentRequest;
import uk.gov.pay.api.model.directdebit.agreement.DirectDebitPayment;
import uk.gov.pay.api.model.directdebit.DirectDebitConnectorCreatePaymentResponse;
import uk.gov.pay.api.service.PublicApiUriGenerator;

import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static uk.gov.pay.api.model.directdebit.agreement.DirectDebitPayment.DirectDebitPaymentBuilder.aDirectDebitPayment;
import static uk.gov.pay.api.model.directdebit.DirectDebitConnectorCreatePaymentRequest.DirectDebitConnectorCreatePaymentRequestBuilder.aDirectDebitConnectorCreatePaymentRequest;

public class CreateDirectDebitPaymentsService {
    private final Client client;
    private final PublicApiUriGenerator publicApiUriGenerator;
    private final DirectDebitConnectorUriGenerator directDebitConnectorUriGenerator;

    @Inject
    public CreateDirectDebitPaymentsService(Client client, PublicApiUriGenerator publicApiUriGenerator,
                                            DirectDebitConnectorUriGenerator directDebitConnectorUriGenerator) {
        this.client = client;
        this.publicApiUriGenerator = publicApiUriGenerator;
        this.directDebitConnectorUriGenerator = directDebitConnectorUriGenerator;
    }

    public DirectDebitPayment create(Account account, CreateDirectDebitPaymentRequest directDebitPaymentRequest) {
        var directDebitConnectorCreatePaymentRequest = aDirectDebitConnectorCreatePaymentRequest()
                .withMandateId(directDebitPaymentRequest.getMandateId())
                .withAmount(directDebitPaymentRequest.getAmount())
                .withDescription(directDebitPaymentRequest.getDescription())
                .withReference(directDebitPaymentRequest.getReference())
                .build();

        Response response = client
                .target(directDebitConnectorUriGenerator.chargesURI(account))
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .post(Entity.entity(directDebitConnectorCreatePaymentRequest, MediaType.APPLICATION_JSON_TYPE));

        if (response.getStatus() != HttpStatus.SC_CREATED) {
            throw new CreateChargeException(response);
        }
        var connectorResponse = response
                .readEntity(DirectDebitConnectorCreatePaymentResponse.class);
        
        return aDirectDebitPayment().withAmount(connectorResponse.getAmount())
                .withReference(connectorResponse.getReference())
                .withDescription(connectorResponse.getDescription())
                .withPaymentId(connectorResponse.getPaymentExternalId())
                .withPaymentProvider(connectorResponse.getPaymentProvider())
                .withState(connectorResponse.getState())
                .withCreatedDate(connectorResponse.getCreatedDate())
                .withSelfLink(publicApiUriGenerator.getDirectDebitPaymentURI(connectorResponse.getPaymentExternalId()))
                .withEventsLink(publicApiUriGenerator.getDirectDebitPaymentEventsURI(connectorResponse.getPaymentExternalId()))
                .withMandateLink(publicApiUriGenerator.getDirectDebitMandateURI(connectorResponse.getMandateId()))
                .build();
    }
}
