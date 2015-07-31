package uk.gov.pay.api.app;

import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Environment;
import uk.gov.pay.api.healthcheck.Ping;

public class PublicApi extends Application<Configuration> {

    public static void main(String[] args) throws Exception {
        new PublicApi().run(args);
    }

    @Override
    public void run(Configuration configuration, Environment environment) throws Exception {

         environment.healthChecks().register("ping", new Ping());

    }
}
