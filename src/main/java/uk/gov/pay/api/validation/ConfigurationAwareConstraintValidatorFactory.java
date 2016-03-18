package uk.gov.pay.api.validation;

import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorFactoryImpl;
import uk.gov.pay.api.config.PublicApiConfig;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorFactory;

public class ConfigurationAwareConstraintValidatorFactory implements ConstraintValidatorFactory {

    private ConstraintValidatorFactory delegate = new ConstraintValidatorFactoryImpl();
    private PublicApiConfig config;

    public ConfigurationAwareConstraintValidatorFactory(PublicApiConfig config) {
        this.config = config;
    }

    @Override
    public <T extends ConstraintValidator<?, ?>> T getInstance(Class<T> key) {
        T instance = delegate.getInstance(key);
        if (instance instanceof ConfigurationAware) {
            ((ConfigurationAware) instance).setConfiguration(config);
        }
        return instance;
    }

    @Override
    public void releaseInstance(ConstraintValidator<?, ?> instance) {
        delegate.releaseInstance(instance);
    }
}
