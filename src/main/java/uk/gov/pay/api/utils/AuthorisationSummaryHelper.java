package uk.gov.pay.api.utils;

import uk.gov.pay.api.model.AuthorisationSummary;

public class AuthorisationSummaryHelper {
    public static AuthorisationSummary includeAuthorisationSummaryWhen3dsRequired(AuthorisationSummary authorisationSummary) {
        if (authorisationSummary != null &&
                authorisationSummary.getThreeDSecure() != null &&
                authorisationSummary.getThreeDSecure().isRequired()) {
            return authorisationSummary;
        }
        return null;
    }
}
