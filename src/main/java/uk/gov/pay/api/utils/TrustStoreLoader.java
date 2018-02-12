package uk.gov.pay.api.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

public class TrustStoreLoader {
    private static final Logger logger = LoggerFactory.getLogger(TrustStoreLoader.class);

    private static final String CERTS_PATH;
    private static final String TRUST_STORE_PASSWORD = "changeit";
    private static final String KEY_STORE_FILE_LOCATION = "/tmp/cacerts";

    private static final KeyStore TRUST_STORE;

    static {
        CERTS_PATH = System.getenv("CERTS_PATH");

        try {
            TRUST_STORE = KeyStore.getInstance(KeyStore.getDefaultType());
            TRUST_STORE.load(null, TRUST_STORE_PASSWORD.toCharArray());
        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException e) {
            throw new RuntimeException("Could not create a keystore", e);
        }

        if (CERTS_PATH != null) {
            try {
                Files.walk(Paths.get(CERTS_PATH)).forEach(certPath -> {
                    if (Files.isRegularFile(certPath)) {
                        try {
                            CertificateFactory cf = CertificateFactory.getInstance("X.509");
                            Certificate cert = cf.generateCertificate(new ByteArrayInputStream(Files.readAllBytes(certPath)));
                            TRUST_STORE.setCertificateEntry(certPath.getFileName().toString(), cert);
                            logger.info("Loaded cert " + certPath);
                        } catch (SecurityException | KeyStoreException | CertificateException | IOException e) {
                            logger.error("Could not load " + certPath, e);
                        }
                    }
                });
            } catch (NoSuchFileException nsfe) {
                logger.warn("Did not find any certificates to load");
            } catch (IOException ioe) {
                logger.error("Error walking certs directory", ioe);
            }
        }
    }

    public static void initialiseTrustStore() {

        logger.info("Initialising Trust Store.");

        String CERTS_PATH = System.getenv(TrustStoreLoader.CERTS_PATH);

        KeyStore keyStore;

        try {
            keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null, TRUST_STORE_PASSWORD.toCharArray());
        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException e) {
            throw new RuntimeException("Could not create a keystore", e);
        }

        if (CERTS_PATH != null) {
            try {
                Files.walk(Paths.get(CERTS_PATH)).forEach(certPath -> {
                    if (Files.isRegularFile(certPath)) {
                        try {
                            CertificateFactory cf = CertificateFactory.getInstance("X.509");
                            Certificate cert = cf.generateCertificate(new ByteArrayInputStream(Files.readAllBytes(certPath)));
                            keyStore.setCertificateEntry(certPath.getFileName().toString(), cert);
                            logger.info("Loaded cert " + certPath);
                        } catch (SecurityException | KeyStoreException | CertificateException | IOException e) {
                            logger.error("Could not load " + certPath, e);
                        }
                    }
                });
            } catch (NoSuchFileException nsfe) {
                logger.warn("Did not find any certificates to load");
            } catch (IOException ioe) {
                logger.error("Error walking certs directory", ioe);
            }
        }
        logger.info("Finished Trust Store initialisation.");
        File keyStoreFile = new File(KEY_STORE_FILE_LOCATION);
        try {
            keyStoreFile.createNewFile();
            FileOutputStream out = new FileOutputStream(keyStoreFile);
            keyStore.store(out, TRUST_STORE_PASSWORD.toCharArray());
        } catch (CertificateException | NoSuchAlgorithmException | KeyStoreException | IOException e) {
            throw new RuntimeException("Could't save trust store to file.", e);
        }
    }

    public static KeyStore getTrustStore() {
        return TRUST_STORE;
    }

    public static String getTrustStorePassword() {
        return new String(TRUST_STORE_PASSWORD);
    }
}
