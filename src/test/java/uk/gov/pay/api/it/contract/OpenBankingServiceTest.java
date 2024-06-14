package uk.gov.pay.api.it.contract;

import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import uk.gov.pay.api.app.PublicApi;
import uk.gov.pay.api.app.config.PublicApiConfig;
import uk.gov.pay.api.service.OpenBankingService;

import javax.ws.rs.client.ClientBuilder;

import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;

public class OpenBankingServiceTest {

    @Rule
    public DropwizardAppRule<PublicApiConfig> app = new DropwizardAppRule<>(
            PublicApi.class,
            resourceFilePath("config/test-config.yaml"));
    
    private OpenBankingService openBankingService;
    
    @Before
    public void before() {
        openBankingService = new OpenBankingService(ClientBuilder.newBuilder().build(), 
                app.getConfiguration().getTinkConfiguration());
    }
    
    @Test
    public void testGetAccessToken() {
        System.out.println(openBankingService.getAccessToken());
    }
}
