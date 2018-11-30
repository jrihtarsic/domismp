package eu.europa.ec.edelivery.smp.services.ui;

import eu.europa.ec.edelivery.smp.data.ui.CertificateRO;
import eu.europa.ec.edelivery.smp.exceptions.ErrorCode;
import eu.europa.ec.edelivery.smp.exceptions.SMPRuntimeException;
import eu.europa.ec.edelivery.smp.logging.SMPLogger;
import eu.europa.ec.edelivery.smp.logging.SMPLoggerFactory;
import eu.europa.ec.edelivery.smp.services.SecurityUtilsServices;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import java.io.*;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.list;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Service
public class UIKeystoreService {

    private static final SMPLogger LOG = SMPLoggerFactory.getLogger(UIKeystoreService.class);

    @Autowired
    SecurityUtilsServices securityUtilsServices;

    @Autowired
    private ConversionService conversionService;


    @Value("${smp.keystore.password}")
    private String smpKeyStorePasswordEncrypted;

    @Value("${smp.keystore.filename}")
    private String smpKeyStoreFilename;

    @Value("${configuration.dir}")
    private String configurationDir;

    @Value("${encryption.key.filename}")
    private String encryptionFilename;

    private String smpKeyStorePasswordDecrypted;

    private Map<String, Key> keystoreKeys;
    private Map<String, X509Certificate> keystoreCertificates;

    private KeyManager[] keyManagers;


    @PostConstruct
    public void init() {
        keystoreKeys = new HashMap();
        keystoreCertificates = new HashMap();
        setupJCEProvider();
        refreshData();
    }

    private void setupJCEProvider() {
        Provider[] providerList = Security.getProviders();

        if (providerList == null || providerList.length <= 0 || !(providerList[0] instanceof BouncyCastleProvider)) {
            Security.insertProviderAt(new org.bouncycastle.jce.provider.BouncyCastleProvider(), 1);
        }
    }

    public void refreshData() {


        LOG.info("initialize from configuration folder:{}, enc file: {}, keystore {}" , configurationDir, encryptionFilename, smpKeyStoreFilename);
        if (configurationDir == null || encryptionFilename == null) {
            LOG.warn("Configuration folder and/or encryption filename are not set in database!");
            return;
        }

        File file = new File(configurationDir + File.separator + encryptionFilename);
        File keystoreFilePath = new File(configurationDir + File.separator + smpKeyStoreFilename);
        if (!file.exists()) {
            LOG.error("Encryption key file '{}' does not exists!", file.getAbsolutePath());
            return;
        }
        if (!keystoreFilePath.exists()) {
            LOG.error("Keystore file '{}' does not exists!", keystoreFilePath.getAbsolutePath());
            return;
        }

        try {
            smpKeyStorePasswordDecrypted = securityUtilsServices.decrypt(file, smpKeyStorePasswordEncrypted);
        } catch (SMPRuntimeException exception) {
            LOG.error("Error occurred while using encryption key: " + file.getAbsolutePath() + " Error: " + ExceptionUtils.getRootCauseMessage(exception), exception);
            return;
        }
        // load keystore
        KeyStore keyStore = loadKeystore();
        if (keyStore == null) {
            return;
        }


        try {
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(keyStore, smpKeyStorePasswordDecrypted.toCharArray());
            keyManagers = kmf.getKeyManagers();
        } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException exception) {
            LOG.error("Error occurred while initialize  keyManagers : " + file.getAbsolutePath() + " Error: " + ExceptionUtils.getRootCauseMessage(exception), exception);
            return;
        }


        updateData(keyStore);
    }

    public KeyManager[] getKeyManagers() {
        return keyManagers;
    }

    private void updateData(KeyStore keyStore) {
        try {
            keystoreKeys.clear();
            keystoreCertificates.clear();
            for (String alias : list(keyStore.aliases())) {
                loadKeyAndCert(keyStore, alias);
            }
        } catch (Exception exception) {
            LOG.error("Could not load signing certificate amd private keys Error: " + ExceptionUtils.getRootCauseMessage(exception), exception);
        }
    }

    private KeyStore loadKeystore() {
        // Load the KeyStore and get the signing key and certificate.
        File keystoreFilePath = new File(configurationDir + File.separator + smpKeyStoreFilename);

        if (!keystoreFilePath.exists()) {
            LOG.error("Keystore file '{}' does not exists!", keystoreFilePath.getAbsolutePath());
            return null;
        }
        KeyStore keyStore = null;
        try (InputStream keystoreInputStream = new FileInputStream(keystoreFilePath)) {
            keyStore = KeyStore.getInstance("JKS");
            keyStore.load(keystoreInputStream, smpKeyStorePasswordDecrypted.toCharArray());
        } catch (Exception exception) {
            LOG.error("Could not load signing certificate with private key from keystore file:"
                    + keystoreFilePath + " Error: " + ExceptionUtils.getRootCauseMessage(exception), exception);
        }
        return keyStore;
    }

    private void loadKeyAndCert(KeyStore keyStore, String alias) throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException {
        Key key = keyStore.getKey(alias, smpKeyStorePasswordDecrypted.toCharArray());
        Certificate certificate = keyStore.getCertificate(alias);
        if (key == null || certificate == null || !(certificate instanceof X509Certificate)) {
            throw new IllegalStateException("Wrong entry type found in keystore, only certificates with keypair are accepted, entry alias: " + alias);
        }
        keystoreKeys.put(alias, key);
        keystoreCertificates.put(alias, (X509Certificate) certificate);
    }


    public List<CertificateRO> getKeystoreEntriesList() {

        List<CertificateRO> keystoreList = new ArrayList<>();
        keystoreCertificates.forEach((alias, crt) -> {
            CertificateRO cro = convertToRo(crt);
            cro.setAlias(alias);
            keystoreList.add(cro);
        });
        return keystoreList;
    }


    public CertificateRO convertToRo(X509Certificate d) {
        return conversionService.convert(d, CertificateRO.class);
    }

    public Key getKey(String keyAlias) {
        if (keystoreKeys.size() == 1) {
            // don't care about configured alias in single-domain setup
            return keystoreKeys.values().iterator().next();
        }
        if (isBlank(keyAlias) || !keystoreKeys.containsKey(keyAlias)) {
            throw new SMPRuntimeException(ErrorCode.CONFIGURATION_ERROR, "Wrong configuration, missing key pair from keystore or wrong alias: " + keyAlias);
        }
        return keystoreKeys.get(keyAlias);
    }

    public X509Certificate getCert(String certAlias) {
        if (keystoreCertificates.size() == 1) {
            // don't care about configured alias in single-domain setup
            return keystoreCertificates.values().iterator().next();
        }
        if (isBlank(certAlias) || !keystoreCertificates.containsKey(certAlias)) {
            throw new SMPRuntimeException(ErrorCode.CONFIGURATION_ERROR,  "Wrong configuration, missing key pair from keystore or wrong alias: " + certAlias);
        }
        return keystoreCertificates.get(certAlias);
    }

    /**
     * Import keys smp keystore
     *
     * @param newKeystore
     * @param password
     */
    public void importKeys(KeyStore newKeystore, String password) throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, IOException, CertificateException {

        KeyStore keyStore = loadKeystore();
        if (keyStore != null) {
            securityUtilsServices.mergeKeystore(keyStore, smpKeyStorePasswordDecrypted, newKeystore, password);
            // store keystore
            storeKeystore(keyStore);
            updateData(keyStore);
        }
    }

    /**
     * Delete keys smp keystore
     *
     * @param alias
     */
    public void deleteKey(String alias) throws NoSuchAlgorithmException, KeyStoreException, IOException, CertificateException {

        KeyStore keyStore = loadKeystore();
        if (keyStore != null) {
            keyStore.deleteEntry(alias);
            // store keystore
            storeKeystore(keyStore);
            updateData(keyStore);
        }
    }

    /**
     * Store keystore
     * @param keyStore to store
     * @throws IOException
     * @throws CertificateException
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException
     */
    private void storeKeystore(KeyStore keyStore) throws IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException {
        File keystoreFilePath = new File(configurationDir + File.separator + smpKeyStoreFilename);
        try (FileOutputStream fos = new FileOutputStream(keystoreFilePath)) {
            keyStore.store(fos, smpKeyStorePasswordDecrypted.toCharArray());
        }
    }
}
