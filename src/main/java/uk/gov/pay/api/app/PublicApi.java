package uk.gov.pay.api.app;

import io.dropwizard.Application;
import io.dropwizard.auth.AuthFactory;
import io.dropwizard.auth.oauth.OAuthFactory;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import uk.gov.pay.api.auth.AccountAuthenticator;
import uk.gov.pay.api.config.PublicApiConfig;
import uk.gov.pay.api.healthcheck.Ping;
import uk.gov.pay.api.resources.ClientFactory;
import uk.gov.pay.api.resources.PaymentsResource;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

public class PublicApi extends Application<PublicApiConfig> {

    @Override
    public void initialize(Bootstrap<PublicApiConfig> bootstrap) {
        bootstrap.setConfigurationSourceProvider(
                new SubstitutingSourceProvider(
                        bootstrap.getConfigurationSourceProvider(),
                        new EnvironmentVariableSubstitutor(false)
                )
        );
    }

    @Override
    public void run(PublicApiConfig config, Environment environment) throws Exception {
        final Client client = ClientFactory.from(config.getJerseyClientConfig()).getInstance();

        environment.healthChecks().register("ping", new Ping());

        environment.jersey().register(new PaymentsResource(client, config.getConnectorUrl()));

        environment.jersey().register(AuthFactory.binder(new OAuthFactory<>(new AccountAuthenticator(client, config.getPublicAuthUrl()), "", String.class)));
    }

    public static void main(String[] args) throws Exception {
        new PublicApi().run(args);
    }
}
