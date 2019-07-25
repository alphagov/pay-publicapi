package uk.gov.pay.api.service.telephone;

import uk.gov.pay.api.model.telephone.CreateTelephonePaymentRequest;
import uk.gov.pay.api.model.telephone.PaymentOutcome;
import uk.gov.pay.api.model.telephone.State;
import uk.gov.pay.api.model.telephone.Supplemental;
import uk.gov.pay.api.model.telephone.TelephonePaymentResponse;

public class CreateTelephonePaymentService {
    
    public TelephonePaymentResponse create(CreateTelephonePaymentRequest createTelephonePaymentRequest){

        boolean present = createTelephonePaymentRequest.getPaymentOutcome().getSupplemental().isPresent();


        System.out.println("Present" + present);


        Supplemental supplemental = createTelephonePaymentRequest.getPaymentOutcome().getSupplemental()
                .map(s -> new Supplemental(
                        s.getErrorCode(),
                        s.getErrorMessage()
                )).orElse(null);
        
        
        //Supplemental supplemental;
        
//        if (createTelephonePaymentRequest.getPaymentOutcome().getSupplemental() == null) {
//            supplemental = null;
//        } else {
//            supplemental = new Supplemental(
//                    createTelephonePaymentRequest.getPaymentOutcome().getSupplemental().getErrorCode(),
//                    createTelephonePaymentRequest.getPaymentOutcome().getSupplemental().getErrorMessage()
//            );
//        }
        
        PaymentOutcome paymentOutcome = new PaymentOutcome(
                createTelephonePaymentRequest.getPaymentOutcome().getStatus(),
                createTelephonePaymentRequest.getPaymentOutcome().getCode(),
                supplemental
        );
        
        State state = new State(
                "Success",
                true,
                "Hello, world!",
                "P0010"
        );
        
        TelephonePaymentResponse telephonePaymentResponse = new TelephonePaymentResponse(
                createTelephonePaymentRequest.getAmount(),
                createTelephonePaymentRequest.getReference(),
                createTelephonePaymentRequest.getDescription(),
                createTelephonePaymentRequest.getCreatedDate(),
                createTelephonePaymentRequest.getAuthorisedDate(),
                createTelephonePaymentRequest.getProcessorId(),
                createTelephonePaymentRequest.getProviderId(),
                createTelephonePaymentRequest.getAuthCode(),
                paymentOutcome,
                createTelephonePaymentRequest.getCardType(),
                createTelephonePaymentRequest.getNameOnCard(),
                createTelephonePaymentRequest.getEmailAddress(),
                createTelephonePaymentRequest.getCardExpiry(),
                createTelephonePaymentRequest.getLastFourDigits(),
                createTelephonePaymentRequest.getFirstSixDigits(),
                createTelephonePaymentRequest.getTelephoneNumber(),
                "DUMMY API - RECORD NOT PERSISTED",
                state
        );
        
        return telephonePaymentResponse;
    }
}
