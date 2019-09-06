package uk.gov.pay.api.service.telephone;

import uk.gov.pay.api.model.PaymentState;
import uk.gov.pay.api.model.telephone.CreateTelephonePaymentRequest;
import uk.gov.pay.api.model.telephone.TelephonePaymentResponse;

public class CreateTelephonePaymentService {
    
    public TelephonePaymentResponse create(CreateTelephonePaymentRequest createTelephonePaymentRequest){
        
        PaymentState state = new PaymentState(
                "success",
                true,
                "Created",
                "P0010"
        );
        
        return new TelephonePaymentResponse(
                createTelephonePaymentRequest.getAmount(),
                createTelephonePaymentRequest.getReference(),
                createTelephonePaymentRequest.getDescription(),
                createTelephonePaymentRequest.getCreatedDate().orElse(null),
                createTelephonePaymentRequest.getAuthorisedDate().orElse(null),
                createTelephonePaymentRequest.getProcessorId(),
                createTelephonePaymentRequest.getProviderId(),
                createTelephonePaymentRequest.getAuthCode().orElse(null),
                createTelephonePaymentRequest.getPaymentOutcome(),
                createTelephonePaymentRequest.getCardType(),
                createTelephonePaymentRequest.getNameOnCard().orElse(null),
                createTelephonePaymentRequest.getEmailAddress().orElse(null),
                createTelephonePaymentRequest.getCardExpiry(),
                createTelephonePaymentRequest.getLastFourDigits(),
                createTelephonePaymentRequest.getFirstSixDigits(),
                createTelephonePaymentRequest.getTelephoneNumber().orElse(null),
                "dummypaymentid123notpersisted",
                state
        );
    }
}
