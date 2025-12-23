package uk.gov.pay.api.it.validation.PublicApiConfigIT;

import io.dropwizard.testing.junit5.DropwizardAppExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.gov.pay.api.app.PublicApi;
import uk.gov.pay.api.app.config.PublicApiConfig;
import uk.gov.pay.api.app.config.RateLimiterConfig;

import java.util.List;

import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@ExtendWith(DropwizardExtensionsSupport.class)
class PublicApiConfigIT {

    private static final DropwizardAppExtension<PublicApiConfig> app = new DropwizardAppExtension<>(
            PublicApi.class,
            resourceFilePath("config/test-config.yaml")
    );

    @Test
    void shouldParseConfiguration() {
        RateLimiterConfig rateLimiterConfig = app.getConfiguration().getRateLimiterConfig();
        assertThat(rateLimiterConfig.getNoOfReq(), is(1000));
        assertThat(rateLimiterConfig.getPerMillis(), is(1000));
        assertThat(rateLimiterConfig.getNoOfReqForPost(), is(1000));
        assertThat(rateLimiterConfig.getNoOfReqPerNode(), is(1));
        assertThat(rateLimiterConfig.getNoOfReqForPostPerNode(), is(1));
        assertThat(rateLimiterConfig.getNoOfReqForElevatedAccounts(), is(1000));
        assertThat(rateLimiterConfig.getNoOfPostReqForElevatedAccounts(), is(1000));
        assertThat(rateLimiterConfig.getElevatedAccounts(), is(List.of("1", "2", "3")));
    }
}
