package uk.gov.pay.api.resources;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.api.app.config.PublicApiConfig;

import java.util.List;

public abstract class LedgerOrConnectorStrategyTemplate<T> {

    private static final Logger logger = LoggerFactory.getLogger(LedgerOrConnectorStrategyTemplate.class);
    private String strategy;
    private List<String> VALID_STRATEGIES = ImmutableList.of("ledger-only", "future-behaviour");

    public LedgerOrConnectorStrategyTemplate(PublicApiConfig configuration, String strategy) {
        this.strategy = strategy;
    }

    private void validate() {
        if (!StringUtils.isBlank(strategy) && !VALID_STRATEGIES.contains(strategy)) {
            logger.warn("Not valid strategy (valid values are \"ledger-only\", \"future-behaviour\" or empty); using the default strategy");
            strategy = null;
        }
    }

    public T validateAndExecute() {
        validate();

        if ("future-behaviour".equals(strategy)) {
            return executeFutureBehaviourStrategy();
        } else if ("ledger-only".equals(strategy)) {
            return executeLedgerOnlyStrategy();
        }

        return executeDefaultStrategy();
    }

    protected abstract T executeLedgerOnlyStrategy();

    protected abstract T executeFutureBehaviourStrategy();

    protected abstract T executeDefaultStrategy();
}


