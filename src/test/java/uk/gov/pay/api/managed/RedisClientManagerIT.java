package uk.gov.pay.api.managed;

import com.redis.testcontainers.RedisContainer;
import io.dropwizard.testing.DropwizardTestSupport;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Timeout;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.pay.api.app.PublicApi;
import uk.gov.pay.api.app.config.PublicApiConfig;

import static io.dropwizard.testing.ConfigOverride.config;
import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;

@Testcontainers
class RedisClientManagerIT {

    private static DropwizardTestSupport<PublicApiConfig> app;

    @Container
    private static final RedisContainer redis = new RedisContainer(
            RedisContainer.DEFAULT_IMAGE_NAME.withTag(RedisContainer.DEFAULT_TAG)
    );

    @BeforeAll
    static void startApp() throws Exception {
        app = new DropwizardTestSupport<>(
                PublicApi.class,
                resourceFilePath("config/test-config.yaml"),
                config("redis.endpoint", redis.getRedisHost() + ":" + redis.getRedisPort())
        );
        app.before();
    }

    @AfterAll
    static void afterAll() {
        app.after();
    }

    @RepeatedTest(1000)
    @Timeout(3)
    void shouldShutDownGracefully() {
        var redisURI = redis.getRedisURI();
        var redisClient = RedisClient.create(redisURI);
        var redisClientManager = new RedisClientManager(redisClient);
        StatefulRedisConnection<String, String> redisConnection = redisClientManager.getRedisConnection();

        for (int i = 0; i < 100; i++) {
            redisConnection.sync().incr("foo");
            redisConnection.sync().decr("foo");
        }

        redisClientManager.stop();
    }
}
