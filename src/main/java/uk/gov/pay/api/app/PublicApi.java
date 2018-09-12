package uk.gov.pay.api.app;

import com.bendb.dropwizard.redis.JedisBundle;
import com.bendb.dropwizard.redis.JedisFactory;
import com.codahale.metrics.graphite.GraphiteReporter;
import com.codahale.metrics.graphite.GraphiteSender;
import com.codahale.metrics.graphite.GraphiteUDP;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.dropwizard.Application;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.auth.CachingAuthenticator;
import io.dropwizard.auth.oauth.OAuthCredentialAuthFilter;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.glassfish.jersey.CommonProperties;
import uk.gov.pay.api.app.config.PublicApiConfig;
import uk.gov.pay.api.app.config.PublicApiModule;
import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.auth.AccountAuthenticator;
import uk.gov.pay.api.exception.mapper.BadRequestExceptionMapper;
import uk.gov.pay.api.exception.mapper.CancelChargeExceptionMapper;
import uk.gov.pay.api.exception.mapper.CreateAgreementExceptionMapper;
import uk.gov.pay.api.exception.mapper.CreateChargeExceptionMapper;
import uk.gov.pay.api.exception.mapper.CreateRefundExceptionMapper;
import uk.gov.pay.api.exception.mapper.GetAgreementExceptionMapper;
import uk.gov.pay.api.exception.mapper.GetChargeExceptionMapper;
import uk.gov.pay.api.exception.mapper.GetEventsExceptionMapper;
import uk.gov.pay.api.exception.mapper.GetRefundExceptionMapper;
import uk.gov.pay.api.exception.mapper.GetRefundsExceptionMapper;
import uk.gov.pay.api.exception.mapper.SearchChargesExceptionMapper;
import uk.gov.pay.api.exception.mapper.SearchRefundsExceptionMapper;
import uk.gov.pay.api.exception.mapper.ValidationExceptionMapper;
import uk.gov.pay.api.filter.AuthorizationValidationFilter;
import uk.gov.pay.api.filter.LoggingFilter;
import uk.gov.pay.api.filter.RateLimiterFilter;
import uk.gov.pay.api.healthcheck.Ping;
import uk.gov.pay.api.resources.AgreementsResource;
import uk.gov.pay.api.resources.DirectDebitEventsResource;
import uk.gov.pay.api.resources.HealthCheckResource;
import uk.gov.pay.api.resources.PaymentRefundsResource;
import uk.gov.pay.api.resources.PaymentsResource;
import uk.gov.pay.api.resources.SearchRefundsResource;
import uk.gov.pay.api.resources.RequestDeniedResource;

import javax.net.ssl.HttpsURLConnection;
import java.util.concurrent.TimeUnit;

import static java.util.EnumSet.of;
import static javax.servlet.DispatcherType.REQUEST;

public class PublicApi extends Application<PublicApiConfig> {

    private static final String SERVICE_METRICS_NODE = "publicapi";
    private static final int GRAPHITE_SENDING_PERIOD_SECONDS = 10;

    @Override
    public void initialize(Bootstrap<PublicApiConfig> bootstrap) {
        bootstrap.setConfigurationSourceProvider(
                new SubstitutingSourceProvider(
                        bootstrap.getConfigurationSourceProvider(),
                        new EnvironmentVariableSubstitutor(false)
                )
        );
        bootstrap.addBundle(new JedisBundle<PublicApiConfig>() {
            @Override
            public JedisFactory getJedisFactory(PublicApiConfig configuration) {
                return configuration.getJedisFactory();
            };
        });
    }

    @Override
    public void run(PublicApiConfig configuration, Environment environment) {
        initialiseSSLSocketFactory();

        final Injector injector = Guice.createInjector(new PublicApiModule(configuration, environment));

        environment.healthChecks().register("ping", new Ping());

        environment.jersey().register(injector.getInstance(HealthCheckResource.class));
        environment.jersey().register(injector.getInstance(PaymentsResource.class));
        environment.jersey().register(injector.getInstance(DirectDebitEventsResource.class));
        environment.jersey().register(injector.getInstance(PaymentRefundsResource.class));
        environment.jersey().register(injector.getInstance(RequestDeniedResource.class));
        environment.jersey().register(injector.getInstance(AgreementsResource.class));
        environment.jersey().register(injector.getInstance(SearchRefundsResource.class));

        environment.servlets().addFilter("AuthorizationValidationFilter", injector.getInstance(AuthorizationValidationFilter.class))
                .addMappingForUrlPatterns(of(REQUEST), true, "/v1/*");

        environment.servlets().addFilter("RateLimiterFilter", injector.getInstance(RateLimiterFilter.class))
                .addMappingForUrlPatterns(of(REQUEST), true, "/v1/*");

        environment.servlets().addFilter("LoggingFilter", injector.getInstance(LoggingFilter.class))
                .addMappingForUrlPatterns(of(REQUEST), true, "/v1/*");
        /*
           Turn off 'FilteringJacksonJaxbJsonProvider' which overrides dropwizard JacksonMessageBodyProvider.
           Fails on Integration tests if not disabled. 
               - https://github.com/dropwizard/dropwizard/issues/1341
        */
        environment.jersey().property(CommonProperties.FEATURE_AUTO_DISCOVERY_DISABLE, Boolean.TRUE);

        CachingAuthenticator<String, Account> cachingAuthenticator = new CachingAuthenticator(
                environment.metrics(),
                injector.getInstance(AccountAuthenticator.class),
                configuration.getAuthenticationCachePolicy());

        environment.jersey().register(new AuthDynamicFeature(
                new OAuthCredentialAuthFilter.Builder<Account>()
                        .setAuthenticator(cachingAuthenticator)
                        .setPrefix("Bearer")
                        .buildAuthFilter()));
        environment.jersey().register(new AuthValueFactoryProvider.Binder<>(Account.class));

        attachExceptionMappersTo(environment.jersey());

        initialiseMetrics(configuration, environment);

        //health check removed as redis is not a mandatory dependency
        environment.healthChecks().unregister("redis");
    }

    /**
     * Adding a call to initialise SSL socket factory at startup until we find a resolution for the following jersey client bug (JERSEY-3124).
     *
     * @see <a href="https://jersey.github.io/release-notes/2.24.html">https://jersey.github.io/release-notes/2.24.html</a>
     */
    private void initialiseSSLSocketFactory() {
        HttpsURLConnection.getDefaultSSLSocketFactory();
    }

    private void attachExceptionMappersTo(JerseyEnvironment jersey) {
        jersey.register(CreateChargeExceptionMapper.class);
        jersey.register(GetChargeExceptionMapper.class);
        jersey.register(GetEventsExceptionMapper.class);
        jersey.register(SearchChargesExceptionMapper.class);
        jersey.register(SearchRefundsExceptionMapper.class);
        jersey.register(CancelChargeExceptionMapper.class);
        jersey.register(ValidationExceptionMapper.class);
        jersey.register(BadRequestExceptionMapper.class);
        jersey.register(CreateRefundExceptionMapper.class);
        jersey.register(GetRefundExceptionMapper.class);
        jersey.register(GetRefundsExceptionMapper.class);
        jersey.register(CreateAgreementExceptionMapper.class);
        jersey.register(GetAgreementExceptionMapper.class);
    }

    private void initialiseMetrics(PublicApiConfig configuration, Environment environment) {
        GraphiteSender graphiteUDP = new GraphiteUDP(configuration.getGraphiteHost(), Integer.valueOf(configuration.getGraphitePort()));
        GraphiteReporter.forRegistry(environment.metrics())
                .prefixedWith(SERVICE_METRICS_NODE)
                .build(graphiteUDP)
                .start(GRAPHITE_SENDING_PERIOD_SECONDS, TimeUnit.SECONDS);
    }

    public static void main(String[] args) throws Exception {
        new PublicApi().run(args);
    }
}
