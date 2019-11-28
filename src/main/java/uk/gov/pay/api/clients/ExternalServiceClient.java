package uk.gov.pay.api.clients;

import org.slf4j.MDC;

import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Optional;

public class ExternalServiceClient {

    private final Client client;

    @Inject
    public ExternalServiceClient(Client client) {
        this.client = client;
    }

    public Response get(String url) {
        Invocation.Builder request = client.target(url).request();

        Optional.ofNullable(MDC.get("x_request_id"))
                .filter(s -> !s.isBlank())
                .ifPresent(xRequestId -> request.header("X-Request-Id", xRequestId));
        
        return request.accept(MediaType.APPLICATION_JSON).get();
    }
    
    public Response post(String url) {
        return post(url, null);
    }

    public Response post(String url, Entity entity) {
        Invocation.Builder request = client.target(url).request();

        Optional.ofNullable(MDC.get("x_request_id"))
                .filter(s -> !s.isBlank())
                .ifPresent(xRequestId -> request.header("X-Request-Id", xRequestId));
        
        return request.accept(MediaType.APPLICATION_JSON).post(entity);
    }
}
