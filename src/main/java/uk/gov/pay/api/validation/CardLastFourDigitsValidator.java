package uk.gov.pay.api.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class CardLastFourDigitsValidator implements ConstraintValidator<ValidCardLastFourDigits, String> {

    private Pattern pattern = Pattern.compile("\\d{4}");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        
        if (value == null) {
            return true;
        }

        return pattern.matcher(value).matches();

    }
}
