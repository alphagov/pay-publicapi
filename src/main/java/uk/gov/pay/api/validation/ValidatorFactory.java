package uk.gov.pay.api.validation;

import org.glassfish.hk2.api.Factory;

import jakarta.inject.Inject;
import jakarta.validation.ConstraintValidatorFactory;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

public class ValidatorFactory implements Factory<Validator> {

    private final ConstraintValidatorFactory constraintValidatorFactory;

    @Inject
    public ValidatorFactory(ConstraintValidatorFactory constraintValidatorFactory) {
        this.constraintValidatorFactory = constraintValidatorFactory;
    }
    
    @Override
    public Validator provide() {
        return Validation.byDefaultProvider().configure().constraintValidatorFactory(constraintValidatorFactory)
                .buildValidatorFactory().getValidator();
    }

    @Override
    public void dispose(Validator validator) {
    }
}
