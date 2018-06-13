package uk.gov.pay.api.app.config;

import com.google.inject.AbstractModule;
import io.dropwizard.setup.Environment;

public class PublicApiModule extends AbstractModule {

    private final PublicApiConfig configuration;
    private final Environment environment;

    public PublicApiModule(final PublicApiConfig configuration, final Environment environment) {
        this.configuration = configuration;
        this.environment = environment;
    }

    @Override
    protected void configure() {
        bind(PublicApiConfig.class).toInstance(configuration);
        bind(Environment.class).toInstance(environment);
    }
    
}
