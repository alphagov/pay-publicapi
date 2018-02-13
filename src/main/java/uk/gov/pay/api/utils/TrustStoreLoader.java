package uk.gov.pay.api.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

public class TrustStoreLoader {

    private static final Logger logger = LoggerFactory.getLogger(TrustStoreLoader.class);

    private static final String CERTS_PATH_VARIABLE = "CERTS_PATH";
    private static final String TRUST_STORE_PASSWORD = "changeit";
    private static final String KEY_STORE_FILE_LOCATION = "/tmp/cacerts";

    private static final KeyStore TRUST_STORE;


    /*
     * To be removed if we switch all http clients to apache http client
     */
    static {

        String CERTS_PATH = System.getenv(TrustStoreLoader.CERTS_PATH_VARIABLE);

        TRUST_STORE = initialiseEmptyKeyStore();

        loadCertificatesIntoKeyStore(CERTS_PATH, TRUST_STORE);
    }


    public static void initialiseTrustStore() {

        String CERTS_PATH = System.getenv(TrustStoreLoader.CERTS_PATH_VARIABLE);

        KeyStore keyStore = initialiseEmptyKeyStore();
        loadCertificatesIntoKeyStore(CERTS_PATH, keyStore);
        storeKeyStoreInFile(keyStore);
    }

    private static void loadCertificatesIntoKeyStore(String CERTS_PATH, KeyStore keyStore) {
        if (CERTS_PATH != null) {
            try {
                Files.walk(Paths.get(CERTS_PATH)).forEach(certificatePath -> {
                    if (Files.isRegularFile(certificatePath)) {
                        try {
                            includeCertificateIntoKeyStore(certificatePath, keyStore);
                        } catch (SecurityException | KeyStoreException | CertificateException | IOException e) {
                            logger.error("Could not load " + certificatePath, e);
                        }
                    }
                });
                logger.info("Finished Trust Store initialisation.");
            } catch (NoSuchFileException nsfe) {
                logger.warn("Did not find any certificates to load");
            } catch (IOException ioe) {
                logger.error("Error walking certs directory", ioe);
            }
        }
    }

    private static void includeCertificateIntoKeyStore(Path certPath, KeyStore keyStore) throws CertificateException, IOException, KeyStoreException {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        Certificate cert = cf.generateCertificate(new ByteArrayInputStream(Files.readAllBytes(certPath)));
        keyStore.setCertificateEntry(certPath.getFileName().toString(), cert);
        logger.info("Loaded cert " + certPath);
    }

    private static void storeKeyStoreInFile(KeyStore keyStore) {
        File keyStoreFile = new File(KEY_STORE_FILE_LOCATION);
        try {
            keyStoreFile.createNewFile();
            FileOutputStream out = new FileOutputStream(keyStoreFile);
            keyStore.store(out, TRUST_STORE_PASSWORD.toCharArray());
        } catch (CertificateException | NoSuchAlgorithmException | KeyStoreException | IOException e) {
            throw new RuntimeException("Could't save trust store to file.", e);
        }
    }

    private static KeyStore initialiseEmptyKeyStore() {

        logger.info("Initialising Trust Store.");

        KeyStore keyStore;

        try {
            keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null, TRUST_STORE_PASSWORD.toCharArray());
        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException e) {
            throw new RuntimeException("Could not create a keystore", e);
        }
        return keyStore;
    }

    public static KeyStore getTrustStore() {
        return TRUST_STORE;
    }

    public static String getTrustStorePassword() {
        return TRUST_STORE_PASSWORD;
    }
}
