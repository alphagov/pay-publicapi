package uk.gov.pay.api.app;

import com.codahale.metrics.graphite.GraphiteReporter;
import com.codahale.metrics.graphite.GraphiteSender;
import com.codahale.metrics.graphite.GraphiteUDP;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.dropwizard.Application;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.auth.oauth.OAuthCredentialAuthFilter;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.api.app.config.PublicApiConfig;
import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.auth.AccountAuthenticator;
import uk.gov.pay.api.exception.mapper.BadRequestExceptionMapper;
import uk.gov.pay.api.exception.mapper.CancelChargeExceptionMapper;
import uk.gov.pay.api.exception.mapper.CreateChargeExceptionMapper;
import uk.gov.pay.api.exception.mapper.CreateRefundExceptionMapper;
import uk.gov.pay.api.exception.mapper.GetChargeExceptionMapper;
import uk.gov.pay.api.exception.mapper.GetEventsExceptionMapper;
import uk.gov.pay.api.exception.mapper.GetRefundExceptionMapper;
import uk.gov.pay.api.exception.mapper.GetRefundsExceptionMapper;
import uk.gov.pay.api.exception.mapper.SearchChargesExceptionMapper;
import uk.gov.pay.api.exception.mapper.ValidationExceptionMapper;
import uk.gov.pay.api.filter.AuthorizationValidationFilter;
import uk.gov.pay.api.filter.LoggingFilter;
import uk.gov.pay.api.filter.RateLimiter;
import uk.gov.pay.api.filter.RateLimiterFilter;
import uk.gov.pay.api.healthcheck.Ping;
import uk.gov.pay.api.json.CreatePaymentRefundRequestDeserializer;
import uk.gov.pay.api.json.CreatePaymentRequestDeserializer;
import uk.gov.pay.api.model.CreatePaymentRefundRequest;
import uk.gov.pay.api.model.CreatePaymentRequest;
import uk.gov.pay.api.resources.HealthCheckResource;
import uk.gov.pay.api.resources.PaymentRefundsResource;
import uk.gov.pay.api.resources.PaymentsResource;
import uk.gov.pay.api.resources.RequestDeniedResource;
import uk.gov.pay.api.utils.BuildTrustStoreCommand;
import uk.gov.pay.api.validation.PaymentRefundRequestValidator;
import uk.gov.pay.api.validation.PaymentRequestValidator;
import uk.gov.pay.api.validation.URLValidator;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static java.util.EnumSet.of;
import static javax.servlet.DispatcherType.REQUEST;
import static uk.gov.pay.api.resources.PaymentsResource.API_VERSION_PATH;
import static uk.gov.pay.api.validation.URLValidator.urlValidatorValueOf;

public class PublicApi extends Application<PublicApiConfig> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PublicApi.class);

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
        bootstrap.addCommand(new BuildTrustStoreCommand());
    }

    @Override
    public void run(PublicApiConfig config, Environment environment) {

        final Client client = RestClientFactory.buildClient(config.getRestClientConfig());


        IntStream.of(1, 2, 3)
                .forEach((number -> {
                    try {
                        Response response = client.target(config.getConnectorUrl())
                                .path("/v1/api/accounts")
                                .request()
                                .get();
                        LOGGER.info(">>> Test connector client call retry " + number + " -> Response: " + response.getStatus());
                    } catch (Exception e) {

                    }
                }));



        ObjectMapper objectMapper = environment.getObjectMapper();
        configureObjectMapper(config, objectMapper);

        environment.healthChecks().register("ping", new Ping());
        environment.jersey().register(new HealthCheckResource(environment));
        environment.jersey().register(new PaymentsResource(config.getBaseUrl(), client, config.getConnectorUrl(), config.getConnectorDDUrl(), objectMapper));
        environment.jersey().register(new PaymentRefundsResource(config.getBaseUrl(), client, config.getConnectorUrl()));
        environment.jersey().register(new RequestDeniedResource());

        RateLimiter rateLimiter = new RateLimiter(config.getRateLimiterConfig().getRate(), config.getRateLimiterConfig().getPerMillis());

        environment.servlets().addFilter("AuthorizationValidationFilter", new AuthorizationValidationFilter(config.getApiKeyHmacSecret()))
                .addMappingForUrlPatterns(of(REQUEST), true, API_VERSION_PATH + "/*");

        environment.servlets().addFilter("RateLimiterFilter", new RateLimiterFilter(rateLimiter, objectMapper))
                .addMappingForUrlPatterns(of(REQUEST), true, API_VERSION_PATH + "/*");

        environment.servlets().addFilter("LoggingFilter", new LoggingFilter())
                .addMappingForUrlPatterns(of(REQUEST), true, API_VERSION_PATH + "/*");

        environment.jersey().register(new AuthDynamicFeature(
                new OAuthCredentialAuthFilter.Builder<Account>()
                        .setAuthenticator(new AccountAuthenticator(client, config.getPublicAuthUrl()))
                        .setPrefix("Bearer")
                        .buildAuthFilter()));
        environment.jersey().register(new AuthValueFactoryProvider.Binder<>(Account.class));

        attachExceptionMappersTo(environment.jersey());

        initialiseMetrics(config, environment);
    }

    private void attachExceptionMappersTo(JerseyEnvironment jersey) {
        jersey.register(CreateChargeExceptionMapper.class);
        jersey.register(GetChargeExceptionMapper.class);
        jersey.register(GetEventsExceptionMapper.class);
        jersey.register(SearchChargesExceptionMapper.class);
        jersey.register(CancelChargeExceptionMapper.class);
        jersey.register(ValidationExceptionMapper.class);
        jersey.register(BadRequestExceptionMapper.class);
        jersey.register(CreateRefundExceptionMapper.class);
        jersey.register(GetRefundExceptionMapper.class);
        jersey.register(GetRefundsExceptionMapper.class);
    }

    private void initialiseMetrics(PublicApiConfig configuration, Environment environment) {
        GraphiteSender graphiteUDP = new GraphiteUDP(configuration.getGraphiteHost(), Integer.valueOf(configuration.getGraphitePort()));
        GraphiteReporter.forRegistry(environment.metrics())
                .prefixedWith(SERVICE_METRICS_NODE)
                .build(graphiteUDP)
                .start(GRAPHITE_SENDING_PERIOD_SECONDS, TimeUnit.SECONDS);
    }

    private void configureObjectMapper(PublicApiConfig config, ObjectMapper objectMapper) {

        URLValidator urlValidator = urlValidatorValueOf(config.getAllowHttpForReturnUrl());
        CreatePaymentRequestDeserializer paymentRequestDeserializer = new CreatePaymentRequestDeserializer(new PaymentRequestValidator(urlValidator));
        CreatePaymentRefundRequestDeserializer paymentRefundRequestDeserializer = new CreatePaymentRefundRequestDeserializer(new PaymentRefundRequestValidator());

        SimpleModule publicApiDeserializationModule = new SimpleModule("publicApiDeserializationModule");
        publicApiDeserializationModule.addDeserializer(CreatePaymentRequest.class, paymentRequestDeserializer);
        publicApiDeserializationModule.addDeserializer(CreatePaymentRefundRequest.class, paymentRefundRequestDeserializer);

        objectMapper.configure(DeserializationFeature.ACCEPT_FLOAT_AS_INT, false);
        objectMapper.registerModule(publicApiDeserializationModule);
    }

    public static void main(String[] args) throws Exception {
        new PublicApi().run(args);
    }
}
