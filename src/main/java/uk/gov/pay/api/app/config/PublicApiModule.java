package uk.gov.pay.api.app.config;

import com.fasterxml.jackson.annotation.JsonInclude;
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
import uk.gov.pay.api.filter.ratelimit.RateLimitManager;
import uk.gov.pay.api.json.CreatePaymentRefundRequestDeserializer;
import uk.gov.pay.api.json.CreateCardPaymentRequestDeserializer;
import uk.gov.pay.api.json.StringDeserializer;
import uk.gov.pay.api.model.CreateCardPaymentRequest;
import uk.gov.pay.api.model.CreatePaymentRefundRequest;
import uk.gov.pay.api.validation.PaymentRefundRequestValidator;
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
        bind(URLValidator.class).toInstance(urlValidatorValueOf(configuration.getAllowHttpForReturnUrl()));
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

        CreateCardPaymentRequestDeserializer cardPaymentRequestDeserializer = new CreateCardPaymentRequestDeserializer();
        CreatePaymentRefundRequestDeserializer paymentRefundRequestDeserializer = new CreatePaymentRefundRequestDeserializer(new PaymentRefundRequestValidator());
        StringDeserializer stringDeserializer = new StringDeserializer(); 

        SimpleModule publicApiDeserializationModule = new SimpleModule("publicApiDeserializationModule");
        publicApiDeserializationModule.addDeserializer(CreateCardPaymentRequest.class, cardPaymentRequestDeserializer);
        publicApiDeserializationModule.addDeserializer(CreatePaymentRefundRequest.class, paymentRefundRequestDeserializer);
        publicApiDeserializationModule.addDeserializer(String.class, stringDeserializer);

        objectMapper.configure(DeserializationFeature.ACCEPT_FLOAT_AS_INT, false);
        objectMapper.registerModule(publicApiDeserializationModule);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

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
        var rateLimitManager = new RateLimitManager(configuration.getRateLimiterConfig());
        return new RedisRateLimiter(rateLimitManager,
                configuration.getRateLimiterConfig().getPerMillis(),
                configuration.getJedisFactory().build(environment));
    }


}
