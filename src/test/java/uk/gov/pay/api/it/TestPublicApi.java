package uk.gov.pay.api.it;

import com.google.inject.Guice;
import com.google.inject.Injector;
import io.dropwizard.setup.Environment;
import uk.gov.pay.api.app.PublicApi;
import uk.gov.pay.api.app.config.PublicApiConfig;

public class TestPublicApi extends PublicApi {

    @Override
    protected Injector getInjector(PublicApiConfig configuration, Environment environment) {
        return Guice.createInjector(new TestPublicApiModule(configuration, environment));
    }
}
