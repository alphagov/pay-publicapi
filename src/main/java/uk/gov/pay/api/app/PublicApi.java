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
import uk.gov.pay.api.exception.mapper.BadRequestExceptionMapper;
import uk.gov.pay.api.exception.mapper.CreateChargeExceptionMapper;
import uk.gov.pay.api.exception.mapper.SearchChargesExceptionMapper;
import uk.gov.pay.api.exception.mapper.ValidationExceptionMapper;
import uk.gov.pay.api.healthcheck.Ping;
import uk.gov.pay.api.json.CreatePaymentRequestDeserializer;
import uk.gov.pay.api.model.CreatePaymentRequest;
import uk.gov.pay.api.resources.PaymentsResource;
import uk.gov.pay.api.validation.PaymentRequestValidator;
import uk.gov.pay.api.validation.URLValidator;

import javax.ws.rs.client.Client;

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

        configureObjectMapper(config, environment.getObjectMapper());

        environment.healthChecks().register("ping", new Ping());

        environment.jersey().register(new PaymentsResource(client, config.getConnectorUrl()));
        environment.jersey().register(AuthFactory.binder(new OAuthFactory<>(new AccountAuthenticator(client, config.getPublicAuthUrl()), "", String.class)));
        environment.jersey().register(CreateChargeExceptionMapper.class);
        environment.jersey().register(SearchChargesExceptionMapper.class);
        environment.jersey().register(ValidationExceptionMapper.class);
        environment.jersey().register(BadRequestExceptionMapper.class);
    }

    private void configureObjectMapper(PublicApiConfig config, ObjectMapper objectMapper) {

        URLValidator urlValidator = urlValidatorValueOf(config.getRestClientConfig().isDisabledSecureConnection());
        CreatePaymentRequestDeserializer paymentRequestDeserializer = new CreatePaymentRequestDeserializer(new PaymentRequestValidator(urlValidator));

        SimpleModule customDeserializationModule = new SimpleModule("customDeserializationModule");
        customDeserializationModule.addDeserializer(CreatePaymentRequest.class, paymentRequestDeserializer);

        objectMapper.configure(DeserializationFeature.ACCEPT_FLOAT_AS_INT, false);
        objectMapper.registerModule(customDeserializationModule);
    }

    public static void main(String[] args) throws Exception {
        new PublicApi().run(args);
    }
}
