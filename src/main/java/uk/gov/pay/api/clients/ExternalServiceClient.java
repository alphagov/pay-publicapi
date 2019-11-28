package uk.gov.pay.api.clients;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import uk.gov.pay.api.resources.PaymentRefundsResource;

import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

public class ExternalServiceClient {

    private static final Logger logger = LoggerFactory.getLogger(ExternalServiceClient.class);

    private final Client client;

    @Inject
    public ExternalServiceClient(Client client) {
        this.client = client;
    }

    public Response get(String url) {
        return client.target(url).request().headers(getHeaders()).accept(MediaType.APPLICATION_JSON).get();
    }

    private MultivaluedMap<String, Object> getHeaders() {
        StringBuilder log = new StringBuilder();
        log.append("x_request_id in MDC: " + MDC.get("x_request_id") + "\n");
        
        MultivaluedHashMap<String, Object> headers = Optional.ofNullable(MDC.get("x_request_id"))
                .filter(s -> !s.isBlank())
                .map(s -> new MultivaluedHashMap<String, Object>(Map.of("X-Request-Id", s)))
                .orElse(new MultivaluedHashMap<>());
        
        log.append("Headers:\n");
        headers.forEach((s, objects) -> {
            log.append("Header name: " + s + "\n");
            objects.forEach(o -> log.append("Header value: " + o + "\n"));
        });
        
        logger.info(log.toString());
        
        return headers;
    }

    public Response post(String url) {
        return post(url, null);
    }

    public Response post(String url, Entity entity) {
        return client.target(url).request().headers(getHeaders()).accept(MediaType.APPLICATION_JSON).post(entity);
    }
}
