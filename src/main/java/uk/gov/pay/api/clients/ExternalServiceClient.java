package uk.gov.pay.api.clients;

import org.slf4j.MDC;

import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.util.Map;

public class ExternalServiceClient {

    private final Client client;

    @Inject
    public ExternalServiceClient(Client client) {
        this.client = client;
    }

    public Response get(String url) {
        return client.target(url).request().headers(getHeaders()).accept(MediaType.APPLICATION_JSON).get();
    }

    private MultivaluedMap<String, Object> getHeaders() {
        return new MultivaluedHashMap<>(Map.of("X-Request-Id", MDC.get("x_request_id")));
    }

    public Response post(String url) {
        return post(url, null);
    }

    public Response post(String url, Entity entity) {
        return client.target(url).request().headers(getHeaders()).accept(MediaType.APPLICATION_JSON).post(entity);
    }
}
