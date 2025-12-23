package uk.gov.pay.api.it.validation.PublicApiConfigIT;

import io.dropwizard.testing.junit5.DropwizardAppExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.gov.pay.api.app.PublicApi;
import uk.gov.pay.api.app.config.PublicApiConfig;
import uk.gov.pay.api.app.config.RateLimiterConfig;

import java.util.Collections;

import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@ExtendWith(DropwizardExtensionsSupport.class)
public class EmptyLowTrafficAccountsPublicApiConfigIT {

    private static final DropwizardAppExtension<PublicApiConfig> EXT = new DropwizardAppExtension<>(
            PublicApi.class,
            resourceFilePath("config/empty-low-traffic-accounts-test-config.yaml")
    );

    @Test
    void shouldParseRateLimitConfigurationForLowTrafficAccounts() {
        RateLimiterConfig rateLimiterConfig = EXT.getConfiguration().getRateLimiterConfig();
        assertThat(rateLimiterConfig.getNoOfReqForLowTrafficAccounts(), is(4500));
        assertThat(rateLimiterConfig.getNoOfPostReqForLowTrafficAccounts(), is(2));
        assertThat(rateLimiterConfig.getIntervalInMillisForLowTrafficAccounts(), is(60000));
        assertThat(rateLimiterConfig.getLowTrafficAccounts(), is(Collections.emptyList()));
    }
}
