package uk.gov.pay.api.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ReturnUrlValidator implements ConstraintValidator<ValidReturnUrl, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return URLValidator.SECURITY_ENABLED.isValid(value);
    }
}
