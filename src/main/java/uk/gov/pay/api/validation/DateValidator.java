package uk.gov.pay.api.validation;

import uk.gov.pay.api.utils.DateTimeUtils;

import static org.eclipse.jetty.util.StringUtil.isBlank;

public class DateValidator {

    public static boolean validate(String value) {
        return isBlank(value) || DateTimeUtils.toUTCZonedDateTime(value).isPresent();
    }
}
