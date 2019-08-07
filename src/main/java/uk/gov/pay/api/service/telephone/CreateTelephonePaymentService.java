package uk.gov.pay.api.service.telephone;

import uk.gov.pay.api.model.telephone.CreateTelephonePaymentRequest;
import uk.gov.pay.api.model.telephone.State;
import uk.gov.pay.api.model.telephone.TelephonePaymentResponse;

public class CreateTelephonePaymentService {
    
    public TelephonePaymentResponse create(CreateTelephonePaymentRequest createTelephonePaymentRequest){
        
        State state = new State(
                "success",
                true,
                "Created",
                "P0010"
        );
        
        return new TelephonePaymentResponse(
                createTelephonePaymentRequest.getAmount(),
                createTelephonePaymentRequest.getReference(),
                createTelephonePaymentRequest.getDescription(),
                createTelephonePaymentRequest.getCreatedDate(),
                createTelephonePaymentRequest.getAuthorisedDate(),
                createTelephonePaymentRequest.getProcessorId(),
                createTelephonePaymentRequest.getProviderId(),
                createTelephonePaymentRequest.getAuthCode(),
                createTelephonePaymentRequest.getPaymentOutcome(),
                createTelephonePaymentRequest.getCardType(),
                createTelephonePaymentRequest.getNameOnCard(),
                createTelephonePaymentRequest.getEmailAddress(),
                createTelephonePaymentRequest.getCardExpiry(),
                createTelephonePaymentRequest.getLastFourDigits(),
                createTelephonePaymentRequest.getFirstSixDigits(),
                createTelephonePaymentRequest.getTelephoneNumber(),
                "dummypaymentid123notpersisted",
                state
        );
    }
}
