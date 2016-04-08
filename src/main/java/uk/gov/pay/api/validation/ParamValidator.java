package uk.gov.pay.api.validation;

import uk.gov.pay.api.model.ExternalChargeStatus;
import uk.gov.pay.api.utils.DateTimeUtils;

import static org.eclipse.jetty.util.StringUtil.isBlank;

public class ParamValidator {
    public static boolean validateDate(String value) {
        return isBlank(value) || DateTimeUtils.toUTCZonedDateTime(value).isPresent();
    }

    public static boolean validateStatus(String value) {
        return isBlank(value) || ExternalChargeStatus.mapFromStatus(value).isPresent();
    }
}
