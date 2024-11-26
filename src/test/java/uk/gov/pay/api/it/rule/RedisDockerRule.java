package uk.gov.pay.api.it.rule;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import static uk.gov.pay.api.it.rule.RedisContainer.getConnectionUrl;
import static uk.gov.pay.api.it.rule.RedisContainer.getOrCreateRedisContainer;
import static uk.gov.pay.api.it.rule.RedisContainer.clearRedisCache;

public class RedisDockerRule implements TestRule, BeforeAllCallback {

    public RedisDockerRule() {
        getOrCreateRedisContainer();
    }

    @Override
    public Statement apply(Statement statement, Description description) {
        return statement;
    }

    public String getRedisUrl() {
        return getConnectionUrl();
    }

    public void clearCache() {
        clearRedisCache();
    }

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        getOrCreateRedisContainer();
        clearRedisCache();
    }
}
