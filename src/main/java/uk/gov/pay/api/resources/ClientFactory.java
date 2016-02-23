package uk.gov.pay.api.resources;

import org.glassfish.jersey.SslConfigurator;
import uk.gov.pay.api.config.JerseyClientConfig;

import javax.net.ssl.SSLContext;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

public class ClientFactory {
    public static final String TLSV1_2 = "TLSv1.2";
    private static JerseyClientConfig clientConfig;

    public Client getInstance() {
        if (clientConfig.isDisabledSecureConnection()) {
            return ClientBuilder.newBuilder().build();
        } else {
            SslConfigurator sslConfig = SslConfigurator.newInstance()
                    .trustStoreFile(clientConfig.getTrustStoreFlie())
                    .trustStorePassword(clientConfig.getTrustStorePassword())
                    .keyStoreFile(clientConfig.getKeyStoreFile())
                    .keyPassword(clientConfig.getKeyStorePassword())
                    .securityProtocol(TLSV1_2);

            SSLContext sslContext = sslConfig.createSSLContext();
            return ClientBuilder.newBuilder().sslContext(sslContext).build();
        }
    }

    public static ClientFactory from(JerseyClientConfig clientConfiguration) {
        return new ClientFactory(clientConfiguration);
    }

    private ClientFactory(JerseyClientConfig clientConfiguration) {
        this.clientConfig = clientConfiguration;
    }
}
