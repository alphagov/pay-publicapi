package uk.gov.pay.api.resources;

import org.glassfish.jersey.SslConfigurator;
import uk.gov.pay.api.config.RestClientConfig;

import javax.net.ssl.SSLContext;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

public class RestClientFactory {
    public static final String TLSV1_2 = "TLSv1.2";

    private final RestClientConfig clientConfig;

    public RestClientFactory(RestClientConfig clientConfiguration) {
        this.clientConfig = clientConfiguration;
    }

    public Client getInstance() {
        if (clientConfig.isDisabledSecureConnection()) {
            return ClientBuilder.newBuilder().build();
        } else {
            String keyStoreFile = clientConfig.getKeyStoreDir() + clientConfig.getKeyStoreFile();
            SslConfigurator sslConfig = SslConfigurator.newInstance()
                    .keyStoreFile(keyStoreFile)
                    .keyPassword(clientConfig.getKeyStorePassword())
                    .securityProtocol(TLSV1_2);

            SSLContext sslContext = sslConfig.createSSLContext();
            return ClientBuilder.newBuilder().sslContext(sslContext).build();
        }
    }
}
