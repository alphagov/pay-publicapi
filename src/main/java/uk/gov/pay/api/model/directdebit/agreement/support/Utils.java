package uk.gov.pay.api.model.directdebit.agreement.support;

import uk.gov.pay.api.model.directdebit.agreement.AgreementStatus;
import uk.gov.pay.api.model.directdebit.agreement.AgreementType;
import uk.gov.pay.api.model.directdebit.agreement.CreateAgreementResponse;

import java.util.Map;

public class Utils {

    public static CreateAgreementResponse map2CreateAgreementResponse(Map<String, String> response) {
        CreateAgreementResponse createAgreementResponse = new CreateAgreementResponse(
                response.get("mandate_id"),
                AgreementType.valueOf(response.get("mandate_type").toString()),
                response.get("return_url"),
                response.get("created_date"),
                AgreementStatus.valueOf(response.get("state").toString())
        );

        return createAgreementResponse;
    }

}
