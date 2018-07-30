package uk.gov.pay.api.it;

import io.dropwizard.setup.Environment;
import uk.gov.pay.api.app.config.PublicApiConfig;
import uk.gov.pay.api.app.config.PublicApiModule;

import javax.ws.rs.client.Client;

import static org.mockito.Mockito.mock;

public class TestPublicApiModule extends PublicApiModule {

    public static Client client = mock(Client.class);
    
    public TestPublicApiModule(PublicApiConfig configuration, Environment environment) {
        super(configuration, environment);
    }

    @Override
    protected Client getClient() {
        return client;
    }
}
