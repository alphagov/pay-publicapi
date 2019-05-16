package uk.gov.pay.api.validation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ReturnUrlValidator implements ConstraintValidator<ValidReturnUrl, String> {

    private URLValidator urlValidator;
    private final Logger logger = LoggerFactory.getLogger(ReturnUrlValidator.class);
    
    @Inject
    public ReturnUrlValidator(URLValidator urlValidator) {
        this.urlValidator = urlValidator;
    }
    
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        logger.info("Calling URL validator for URL " + value);
        return value == null || urlValidator.isValid(value);
    }
}
