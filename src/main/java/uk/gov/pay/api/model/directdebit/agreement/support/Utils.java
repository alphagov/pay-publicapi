package uk.gov.pay.api.model.directdebit.agreement.support;

import uk.gov.pay.api.model.directdebit.agreement.AgreementStatus;
import uk.gov.pay.api.model.directdebit.agreement.AgreementType;
import uk.gov.pay.api.model.directdebit.agreement.CreateAgreementResponse;
import uk.gov.pay.api.model.directdebit.agreement.connector.CreateMandateResponse;

public class Utils {

    public static CreateAgreementResponse createMandateResponse2CreateAgreementResponse(CreateMandateResponse createMandateResponse) {
        CreateAgreementResponse createAgreementResponse = new CreateAgreementResponse(
                createMandateResponse.getMandateId(),
                AgreementType.valueOf(createMandateResponse.getMandateType().toString().toUpperCase()),
                createMandateResponse.getReturnUrl(),
                createMandateResponse.getCreatedDate(),
                AgreementStatus.valueOf(createMandateResponse.getState().toString().toUpperCase())
        );

        return createAgreementResponse;
    }

}
