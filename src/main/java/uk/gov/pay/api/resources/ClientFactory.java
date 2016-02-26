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
            String keyStoreDir = clientConfig.getKeyStoreDir().endsWith("/") ?
                    clientConfig.getKeyStoreDir() :
                    clientConfig.getKeyStoreDir().concat("/"); //safer file path
            String keyStoreFile = keyStoreDir.concat(clientConfig.getKeyStoreFile());
            SslConfigurator sslConfig = SslConfigurator.newInstance()
                    .keyStoreFile(keyStoreFile)
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
