package uk.gov.pay.api.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.jackson.Jackson;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

abstract class ApiClient {

    private static final ObjectMapper OBJECT_MAPPER = Jackson.newObjectMapper();

    private final HttpClient httpClient;


    ApiClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }


    <T> T deserialize(HttpResponse response, Class<T> valueType) {
        try {
            return OBJECT_MAPPER.readValue(EntityUtils.toString(response.getEntity()), valueType);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    HttpResponse request(HttpUriRequest request) {
        try {
            return httpClient.execute(request);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
