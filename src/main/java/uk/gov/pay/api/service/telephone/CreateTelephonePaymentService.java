package uk.gov.pay.api.service.telephone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.model.ChargeFromResponse;
import uk.gov.pay.api.model.telephone.CreateTelephonePaymentRequest;
import uk.gov.pay.api.model.telephone.TelephonePaymentResponse;
import uk.gov.pay.api.resources.RequestDeniedResource;
import uk.gov.pay.api.service.ConnectorUriGenerator;

import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static javax.ws.rs.client.Entity.json;

public class CreateTelephonePaymentService {
    
    private final Client client;
    private final ConnectorUriGenerator connectorUriGenerator;
    private static final Logger logger = LoggerFactory.getLogger(CreateTelephonePaymentService.class);

    @Inject
    public CreateTelephonePaymentService(Client client, ConnectorUriGenerator connectorUriGenerator) {
        this.client = client;
        this.connectorUriGenerator = connectorUriGenerator;
    }

    public TelephonePaymentResponse create(Account account, CreateTelephonePaymentRequest createTelephonePaymentRequest){
        Response connectorResponse = createTelephoneCharge(account, createTelephonePaymentRequest);
        
        if (!createdSuccessfully(connectorResponse)) {
            logger.info("Successful response");
        }
        
        ChargeFromResponse chargeFromResponse = connectorResponse.readEntity(ChargeFromResponse.class);
        
        return TelephonePaymentResponse.from(chargeFromResponse);
    }
    
    private boolean createdSuccessfully(Response connectorResponse) {
        int status = connectorResponse.getStatus();
        return status == 200 || status == 201;
    }
    
    private Response createTelephoneCharge(Account account, CreateTelephonePaymentRequest createTelephonePaymentRequest) {
        logger.info("JSON Posted: " + buildTelephoneChargeRequestPayload(createTelephonePaymentRequest));
        
        return client
                .target(connectorUriGenerator.telephoneChargesURI(account))
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .post(buildTelephoneChargeRequestPayload(createTelephonePaymentRequest));
    }
    
    private Entity buildTelephoneChargeRequestPayload(CreateTelephonePaymentRequest requestPayload) {
        return json(requestPayload.toConnectorPayload());
    }
}
