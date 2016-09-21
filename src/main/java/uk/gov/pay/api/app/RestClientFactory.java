package uk.gov.pay.api.app;

import org.glassfish.jersey.SslConfigurator;
import uk.gov.pay.api.app.config.RestClientConfig;
import uk.gov.pay.api.filter.RestClientLoggingFilter;

import javax.net.ssl.SSLContext;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

import static uk.gov.pay.api.utils.TrustStoreLoader.getTrustStore;
import static uk.gov.pay.api.utils.TrustStoreLoader.getTrustStorePassword;

public class RestClientFactory {
    public static final String TLSV1_2 = "TLSv1.2";

    public static Client buildClient(RestClientConfig clientConfig) {
        Client client;
        if (clientConfig.isDisabledSecureConnection()) {
            client = ClientBuilder.newBuilder().build();
        } else {
            SslConfigurator sslConfig = SslConfigurator.newInstance()
                    .trustStore(getTrustStore())
                    .trustStorePassword(getTrustStorePassword())
                    .securityProtocol(TLSV1_2);

            SSLContext sslContext = sslConfig.createSSLContext();
            client = ClientBuilder.newBuilder().sslContext(sslContext).build();
        }
        client.register(RestClientLoggingFilter.class);
        return client;
    }

    private RestClientFactory() {
    }
}
