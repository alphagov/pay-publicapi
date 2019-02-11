package uk.gov.pay.api.app;

import com.google.common.io.Files;
import com.google.common.io.Resources;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import uk.gov.pay.api.app.config.RestClientConfig;

import javax.net.ssl.SSLContext;
import javax.ws.rs.client.Client;
import java.io.*;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNot.not;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RestClientFactoryTest {

    private File keyStoreDir;

    @Before
    public void before() throws Exception {
        keyStoreDir = Files.createTempDir();
        KeyStoreUtil.createKeyStoreWithCerts(keyStoreDir, "tempKeystore.jks", "password".toCharArray());
    }

    @After
    public void after() throws Exception {
        FileUtils.deleteDirectory(keyStoreDir);
    }

    @Test
    public void jerseyClient_shouldUseSSLWhenSecureInternalCommunicationIsOn() {
        //given
        RestClientConfig clientConfiguration = mock(RestClientConfig.class);
        when(clientConfiguration.isDisabledSecureConnection()).thenReturn(false);

        //when
        Client client = RestClientFactory.buildClient(clientConfiguration);

        //then
        SSLContext sslContext = client.getSslContext();
        assertThat(sslContext.getProtocol(), is("TLSv1.2"));

    }

    @Test
    public void jerseyClient_shouldNotUseSSLWhenSecureInternalCommunicationIsOff() {
        //given
        RestClientConfig clientConfiguration = mock(RestClientConfig.class);
        when(clientConfiguration.isDisabledSecureConnection()).thenReturn(true);

        //when
        Client client = RestClientFactory.buildClient(clientConfiguration);

        //then
        assertThat(client.getSslContext().getProtocol(), is(not("TLSv1.2")));
    }


    static class KeyStoreUtil {

        static final String CERT_FILE = "gds-test.pem";

        static void createKeyStoreWithCerts(File keyStoreDir, String keyStoreName, char[] keyStorePassword) throws Exception {

            File keyStore = new File(keyStoreDir, keyStoreName);
            FileOutputStream os = new FileOutputStream(keyStore);

            KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
            keystore.load(null, keyStorePassword);
            keystore.store(os, keyStorePassword);

            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            Certificate certs = certFactory.generateCertificate(streamedCertificate(Resources.getResource(CERT_FILE).getFile()));

            // Load the keystore contents
            FileInputStream keyStoreRead = new FileInputStream(keyStore);
            keystore.load(keyStoreRead, keyStorePassword);
            keyStoreRead.close();

            // Add the certificate
            keystore.setCertificateEntry("root", certs);

            // Save the new keystore contents
            FileOutputStream keyStoreWrite = new FileOutputStream(keyStore);
            keystore.store(keyStoreWrite, keyStorePassword);
            keyStoreWrite.close();
        }

        private static InputStream streamedCertificate(String certFile) throws IOException {
            DataInputStream dis = new DataInputStream(new FileInputStream(certFile));
            byte[] bytes = new byte[dis.available()];
            dis.readFully(bytes);
            return new ByteArrayInputStream(bytes);
        }
    }
}
