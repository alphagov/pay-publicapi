package uk.gov.pay.api.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
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
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class TrustStoreLoader {

    private static final Logger logger = LoggerFactory.getLogger(TrustStoreLoader.class);

    private static final String CERTS_PATH = "CERTS_PATH";
    private static final String TRUST_STORE_PASSWORD = "";
    public static final String KEY_STORE_FILE_LOCATION = "/tmp/cacerts";

    private static KeyStore TRUST_STORE;


    public static KeyStore initialiseTrustStore() {

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
            keyStore.store(out, "changeit".toCharArray());
        } catch (CertificateException | NoSuchAlgorithmException | KeyStoreException | IOException e) {
            throw new RuntimeException("Could't save trust store to file.", e);
        }
        return keyStore;
    }

    private static KeyStore loadTrustStore() {

        String trustStoreDefaultLocation = KEY_STORE_FILE_LOCATION;

        KeyStore newKeyStore;

        try {

            FileInputStream myKeys = new FileInputStream(trustStoreDefaultLocation);

            newKeyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            newKeyStore.load(myKeys, "changeit".toCharArray());

            myKeys.close();

        } catch (IOException | CertificateException | NoSuchAlgorithmException | KeyStoreException e) {
            throw new RuntimeException("Could not load trust store from '" + trustStoreDefaultLocation + "'", e);
        }

        return newKeyStore;
    }

    public static KeyStore getTrustStore() {
        if (TRUST_STORE == null) {
            synchronized (TrustStoreLoader.class) {
                if (TRUST_STORE == null) {
                    TRUST_STORE = loadTrustStore();
                }
            }
        }
        checkTrustStore(TRUST_STORE);
        return TRUST_STORE;
    }

    public static String getTrustStorePassword() {
        return TRUST_STORE_PASSWORD;
    }

    public static void checkTrustStore(final KeyStore keyStore) {
        final String CERTS_PATH = System.getenv("CERTS_PATH");

        if (CERTS_PATH != null) {
            try {

                Files.walk(Paths.get(CERTS_PATH)).forEach(certPath -> {
                    if (Files.isRegularFile(certPath)) {
                        try {
                            String certificateAlias = certPath.getFileName().toString();
                            int time = 0;
                            while (true) {
                                Certificate certificate = keyStore.getCertificate(certificateAlias);

                                if (certificate instanceof X509Certificate) {
                                    try {
                                        ((X509Certificate) certificate).checkValidity();
                                        logger.info("Certificate '{}' is active for current date", certificateAlias);
                                    } catch (CertificateExpiredException cee) {
                                        logger.error("Certificate '{}' is not valid.", certificateAlias);
                                    }
                                    break;
                                }
                                try {
                                    if (time > 90) break;
                                    Thread.sleep(1000);
                                    time++;
                                    logger.info("Retrying validation for certificate '{}'", certificateAlias);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        } catch (SecurityException | KeyStoreException | CertificateException e) {
                            logger.error("Could not verify certificate '" + certPath + "'", e);
                        }
                    }
                });
            } catch (NoSuchFileException nsfe) {
                logger.warn("Did not find any certificates to verify");
            } catch (IOException ioe) {
                logger.error("Error walking certs directory", ioe);
            }
        }
        logger.info("Finished Trust Store verification.");
    }
}
