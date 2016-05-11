package uk.gov.pay.api.filter;

import java.time.Duration;
import java.time.Instant;

import static java.time.temporal.ChronoUnit.MILLIS;

final class RateLimit {

    private final int rate;
    private final int perMillis;

    private Instant created;
    private int requestCount;

    RateLimit(int rate, int perMillis) {
        this.rate = rate;
        this.perMillis = perMillis;
        this.requestCount = 0;
        this.created = Instant.now().truncatedTo(MILLIS);
    }

    /**
     * This block needs to be synchronous. Each RateLimit object will be shared between requests
     * from the same source (Service), so is not shared across all the requests.
     * 
     * @throws RateLimitException
     */
    synchronized void updateAllowance() throws RateLimitException {
        requestCount += 1;
        Instant now = Instant.now().truncatedTo(MILLIS);
        if (Duration.between(created, now).toMillis() >= perMillis) {
            requestCount = 1;
            created = now;
        }
        if (requestCount > rate) {
            throw new RateLimitException();
        }
    }
}
