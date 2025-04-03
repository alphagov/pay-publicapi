package uk.gov.pay.api.resources;

import com.codahale.metrics.annotation.Timed;
import com.codahale.metrics.health.HealthCheck;
import com.google.common.collect.ImmutableMap;
import io.dropwizard.core.setup.Environment;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.status;

@Path("/")
public class HealthCheckResource {
    public static final String HEALTHCHECK = "healthcheck";
    public static final String HEALTHY = "healthy";

    Environment environment;

    @Inject
    public HealthCheckResource(Environment environment) {
        this.environment = environment;
    }

    @GET
    @Timed
    @Path(HEALTHCHECK)
    @Produces(APPLICATION_JSON)
    public Response healthCheck() {
        SortedMap<String, HealthCheck.Result> results = environment.healthChecks().runHealthChecks();

        Map<String, Map<String, Boolean>> response = getResponse(results);

        boolean healthy = results.size() == results.values()
                .stream()
                .filter(HealthCheck.Result::isHealthy)
                .count();

        if(healthy) {
            return Response.ok().entity(response).build();
        }
        return status(503).entity(response).build();
    }

    private Map<String, Map<String, Boolean>> getResponse(SortedMap<String, HealthCheck.Result> results) {
        Map<String, Map<String, Boolean>> response = new HashMap<>();
        for (SortedMap.Entry<String, HealthCheck.Result> entry : results.entrySet() ) {
            response.put(entry.getKey(), ImmutableMap.of(HEALTHY, entry.getValue().isHealthy()));
        }
        return response;
    }
}
