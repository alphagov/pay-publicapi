package uk.gov.pay.api.app;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.dropwizard.Application;
import io.dropwizard.auth.AuthFactory;
import io.dropwizard.auth.oauth.OAuthFactory;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import uk.gov.pay.api.app.config.PublicApiConfig;
import uk.gov.pay.api.auth.AccountAuthenticator;
import uk.gov.pay.api.exception.mapper.*;
import uk.gov.pay.api.filter.RateLimiter;
import uk.gov.pay.api.filter.RateLimiterFilter;
import uk.gov.pay.api.healthcheck.Ping;
import uk.gov.pay.api.json.CreatePaymentRequestDeserializer;
import uk.gov.pay.api.model.CreatePaymentRequest;
import uk.gov.pay.api.resources.HealthCheckResource;
import uk.gov.pay.api.resources.PaymentsResource;
import uk.gov.pay.api.validation.PaymentRequestValidator;
import uk.gov.pay.api.validation.URLValidator;

import javax.servlet.DispatcherType;
import javax.ws.rs.client.Client;

import static java.util.EnumSet.of;
import static uk.gov.pay.api.validation.URLValidator.urlValidatorValueOf;

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
        final Client client = RestClientFactory.buildClient(config.getRestClientConfig());

        ObjectMapper objectMapper = environment.getObjectMapper();
        configureObjectMapper(config, objectMapper);

        environment.healthChecks().register("ping", new Ping());
        environment.jersey().register(new HealthCheckResource(environment));

        environment.jersey().register(new PaymentsResource(client, config.getConnectorUrl()));

        RateLimiter rateLimiter = new RateLimiter(config.getRateLimiterConfig().getRate(), config.getRateLimiterConfig().getPerMillis());
        environment.servlets().addFilter("RateLimiterFilter", new RateLimiterFilter(rateLimiter, objectMapper))
                .addMappingForUrlPatterns(of(DispatcherType.REQUEST), true, "/*");

        environment.jersey().register(AuthFactory.binder(new OAuthFactory<>(new AccountAuthenticator(client, config.getPublicAuthUrl()), "", String.class)));

        environment.jersey().register(CreateChargeExceptionMapper.class);
        environment.jersey().register(GetChargeExceptionMapper.class);
        environment.jersey().register(GetEventsExceptionMapper.class);
        environment.jersey().register(SearchChargesExceptionMapper.class);
        environment.jersey().register(CancelPaymentExceptionMapper.class);
        environment.jersey().register(ValidationExceptionMapper.class);
        environment.jersey().register(BadRequestExceptionMapper.class);
    }

    private void configureObjectMapper(PublicApiConfig config, ObjectMapper objectMapper) {

        URLValidator urlValidator = urlValidatorValueOf(config.getRestClientConfig().isDisabledSecureConnection());
        CreatePaymentRequestDeserializer paymentRequestDeserializer = new CreatePaymentRequestDeserializer(new PaymentRequestValidator(urlValidator));

        SimpleModule publicApiDeserializationModule = new SimpleModule("publicApiDeserializationModule");
        publicApiDeserializationModule.addDeserializer(CreatePaymentRequest.class, paymentRequestDeserializer);

        objectMapper.configure(DeserializationFeature.ACCEPT_FLOAT_AS_INT, false);
        objectMapper.registerModule(publicApiDeserializationModule);
    }

    public static void main(String[] args) throws Exception {
        new PublicApi().run(args);
    }
}
