package uk.gov.pay.api.validation;

import uk.gov.pay.api.utils.DateTimeUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import static org.eclipse.jetty.util.StringUtil.isBlank;

public class DateValidator implements ConstraintValidator<ValidDate, String> {
    
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return isValid(value);
    }

    public static boolean isValid(String value) {
        return isBlank(value) || DateTimeUtils.toUTCZonedDateTime(value).isPresent();
    }
}
