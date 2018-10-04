package uk.gov.pay.api.app.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.dropwizard.setup.Environment;
import uk.gov.pay.api.app.RestClientFactory;
import uk.gov.pay.api.filter.ratelimit.LocalRateLimiter;
import uk.gov.pay.api.filter.ratelimit.RateLimiter;
import uk.gov.pay.api.filter.ratelimit.RedisRateLimiter;
import uk.gov.pay.api.json.CreatePaymentRefundRequestDeserializer;
import uk.gov.pay.api.json.CreatePaymentRequestDeserializer;
import uk.gov.pay.api.model.generated.CreatePaymentRefundRequest;
import uk.gov.pay.api.model.ValidCreatePaymentRequest;
import uk.gov.pay.api.validation.PaymentRefundRequestValidator;
import uk.gov.pay.api.validation.PaymentRequestValidator;
import uk.gov.pay.api.validation.URLValidator;

import javax.ws.rs.client.Client;

import static uk.gov.pay.api.validation.URLValidator.urlValidatorValueOf;

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

    @Provides
    @Singleton
    public Client provideClient() {
        return RestClientFactory.buildClient(configuration.getRestClientConfig());
    }

    @Provides
    @Singleton
    public ObjectMapper provideObjectMapper() {
        ObjectMapper objectMapper = environment.getObjectMapper();

        URLValidator urlValidator = urlValidatorValueOf(configuration.getAllowHttpForReturnUrl());
        CreatePaymentRequestDeserializer paymentRequestDeserializer = new CreatePaymentRequestDeserializer(new PaymentRequestValidator(urlValidator));
        CreatePaymentRefundRequestDeserializer paymentRefundRequestDeserializer = new CreatePaymentRefundRequestDeserializer(new PaymentRefundRequestValidator());

        SimpleModule publicApiDeserializationModule = new SimpleModule("publicApiDeserializationModule");
        publicApiDeserializationModule.addDeserializer(ValidCreatePaymentRequest.class, paymentRequestDeserializer);
        publicApiDeserializationModule.addDeserializer(CreatePaymentRefundRequest.class, paymentRefundRequestDeserializer);

        objectMapper.configure(DeserializationFeature.ACCEPT_FLOAT_AS_INT, false);
        objectMapper.registerModule(publicApiDeserializationModule);

        return objectMapper;
    }

    @Provides
    public RateLimiter provideRateLimiter() {

        LocalRateLimiter localRateLimiter = getLocalRateLimiter();
        RedisRateLimiter redisRateLimiter = getRedisRateLimiter();

        return new RateLimiter(localRateLimiter, redisRateLimiter);
    }

    private LocalRateLimiter getLocalRateLimiter() {
        return new LocalRateLimiter(
                configuration.getRateLimiterConfig().getNoOfReqPerNode(),
                configuration.getRateLimiterConfig().getNoOfReqForPostPerNode(),
                configuration.getRateLimiterConfig().getPerMillis()
        );
    }

    private RedisRateLimiter getRedisRateLimiter() {
        return new RedisRateLimiter(configuration.getRateLimiterConfig().getNoOfReq(),
                configuration.getRateLimiterConfig().getNoOfReqForPost(),
                configuration.getRateLimiterConfig().getPerMillis(),
                configuration.getJedisFactory().build(environment));
    }


}
