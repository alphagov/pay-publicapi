package uk.gov.pay.api.it;

import au.com.dius.pact.consumer.PactVerification;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import uk.gov.pay.api.app.PublicApi;
import uk.gov.pay.api.app.config.PublicApiConfig;
import uk.gov.pay.api.pact.PactProviderRule;
import uk.gov.pay.api.pact.Pacts;
import uk.gov.pay.api.utils.ApiKeyGenerator;

import static io.dropwizard.testing.ConfigOverride.config;
import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;

public class DirectDebitPaymentTest {

    private static final String API_KEY = ApiKeyGenerator.apiKeyValueOf("TEST_BEARER_TOKEN", "qwer9yuhgf");
    private static final String GATEWAY_ACCOUNT_ID = "GATEWAY_ACCOUNT_ID";
    private static final String CHARGE_ID = "ch_ab2341da231434l";
    
    private PactProviderRule directDebitConnector = new PactProviderRule("direct-debit-connector", this);
    private PactProviderRule publicAuth = new PactProviderRule("publicauth", this);
    private DropwizardAppRule<PublicApiConfig> app = new DropwizardAppRule<>(
            PublicApi.class, 
            resourceFilePath("config/test-config.yaml"), 
            config("connectorUrl", "http://localhost"), 
            config("connectorDDUrl", directDebitConnector.getUrl()), 
            config("publicAuthUrl", publicAuth.getUrl()));

    @Rule
    public RuleChain chain = RuleChain.outerRule(directDebitConnector).around(publicAuth).around(app);

    @Test
    @PactVerification()
    @Pacts(pacts = {"publicapi-publicauth", "publicapi-direct-debit-connector"})
    public void createDirectDebitPayment() {
        
    }
}
