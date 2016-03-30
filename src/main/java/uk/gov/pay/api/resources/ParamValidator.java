package uk.gov.pay.api.resources;

import uk.gov.pay.api.model.ExternalChargeStatus;
import uk.gov.pay.api.utils.DateTimeUtils;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.eclipse.jetty.util.StringUtil.isBlank;

public class ParamValidator {
    public static boolean validateDate(String value) {
        return isBlank(value) || DateTimeUtils.toUTCZonedDateTime(value).isPresent();
    }

    public static boolean validateStatus(String value) {
        return isBlank(value) || ExternalChargeStatus.mapFromStatus(value).isPresent();
    }
}
