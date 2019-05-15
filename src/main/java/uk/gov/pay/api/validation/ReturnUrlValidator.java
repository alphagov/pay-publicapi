package uk.gov.pay.api.validation;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ReturnUrlValidator implements ConstraintValidator<ValidReturnUrl, String> {

    private URLValidator urlValidator;

    @Inject
    public ReturnUrlValidator(URLValidator urlValidator) {
        this.urlValidator = urlValidator;
    }
    
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return urlValidator.isValid(value);
    }
}
