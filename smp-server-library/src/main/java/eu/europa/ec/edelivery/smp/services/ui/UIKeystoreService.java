/*-
 * #%L
 * smp-server-library
 * %%
 * Copyright (C) 2017 - 2023 European Commission | eDelivery | DomiSMP
 * %%
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * [PROJECT_HOME]\license\eupl-1.2\license.txt or https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 * #L%
 */
package eu.europa.ec.edelivery.smp.services.ui;

import eu.europa.ec.edelivery.security.utils.KeystoreUtils;
import eu.europa.ec.edelivery.smp.data.ui.CertificateRO;
import eu.europa.ec.edelivery.smp.exceptions.ErrorCode;
import eu.europa.ec.edelivery.smp.exceptions.SMPRuntimeException;
import eu.europa.ec.edelivery.smp.logging.SMPLogger;
import eu.europa.ec.edelivery.smp.logging.SMPLoggerFactory;
import eu.europa.ec.edelivery.smp.services.ConfigurationService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import java.io.*;
import java.nio.file.Files;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.list;
import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * @author Joze Rihtarsic
 * @since 4.1
 */
@Service
public class UIKeystoreService extends BasicKeystoreService {

    private static final SMPLogger LOG = SMPLoggerFactory.getLogger(UIKeystoreService.class);

    private final ConversionService conversionService;
    private final ConfigurationService configurationService;

    public UIKeystoreService(ConversionService conversionService, ConfigurationService configurationService) {
        super(null);
        this.conversionService = conversionService;
        this.configurationService = configurationService;
    }

    private final Map<String, Key> keystoreKeys = new HashMap<>();
    private final Map<String, X509Certificate> keystoreCertificates = new HashMap<>();
    private final List<CertificateRO> certificateROList = new ArrayList<>();

    private KeyManager[] keyManagers;

    private long lastUpdateKeystoreFileTime = 0;
    private File lastUpdateKeystoreFile = null;

    /**
     * Method  validates the configuration properties and refresh the
     * cached data
     */
    public void refreshData() {

        String keystoreSecToken = configurationService.getKeystoreCredentialToken();

        // load keystore
        File keystoreFile = configurationService.getKeystoreFile();
        if (keystoreFile == null) {
            LOG.error("KeystoreFile: is null! Check the keystore and the configuration!");
            return;
        }

        KeyStore keyStore = loadKeystore(keystoreFile, keystoreSecToken);
        if (keyStore == null) {
            LOG.error("Keystore: [{}] is not loaded! Check the keystore and the configuration!", keystoreFile.getAbsolutePath());
            return;
        }
        // init key managers for TLS
        KeyManager[] keyManagersTemp;
        try {
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(keyStore, keystoreSecToken.toCharArray());
            keyManagersTemp = kmf.getKeyManagers();
        } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException exception) {
            LOG.error("Error occurred while initialize  keyManagers : "
                    + keystoreFile.getAbsolutePath() + " Error: " + ExceptionUtils.getRootCauseMessage(exception), exception);
            return;
        }

        // load keys for signature
        Map<String, Key> hmKeys = new HashMap<>();
        Map<String, X509Certificate> hmCertificates = new HashMap<>();
        try {
            List<String> aliases = list(keyStore.aliases());
            for (String alias : aliases) {
                loadKeyAndCert(keyStore, alias, keystoreSecToken, hmKeys, hmCertificates);
            }
        } catch (Exception exception) {
            LOG.error("Could not load signing certificate amd private keys Error: " + ExceptionUtils.getRootCauseMessage(exception), exception);
            return;
        }
        LOG.debug("Set keystore certificates:");
        hmCertificates.forEach((alias, cert) -> LOG.debug(" - {}, {}", alias, cert.getSubjectDN().toString()));
        // if got all data from keystore - update data
        keyManagers = keyManagersTemp;

        keystoreKeys.clear();
        keystoreCertificates.clear();

        keystoreKeys.putAll(hmKeys);
        keystoreCertificates.putAll(hmCertificates);
        // add last file date
        lastUpdateKeystoreFileTime = keystoreFile.lastModified();
        lastUpdateKeystoreFile = keystoreFile;
        // clear list to reload RO when required
        certificateROList.clear();
    }

    boolean isKeyStoreChanged() {
        File file = configurationService.getKeystoreFile();

        return file != null && (!Objects.equals(lastUpdateKeystoreFile, file) || file.lastModified() != lastUpdateKeystoreFileTime);
    }


    public KeyManager[] getKeyManagers() {
        // check if keystore is changes
        if (isKeyStoreChanged()) {
            refreshData();
        }
        return keyManagers;
    }

    private KeyStore loadKeystore(File keyStoreFile, String keystoreSecToken) {
        // Load the KeyStore.
        if (keyStoreFile == null) {
            LOG.error("Keystore file is not defined!");
            return null;
        }
        if (!keyStoreFile.exists()) {
            LOG.error("Keystore file '[{}]' does not exists!", keyStoreFile);
            return null;
        }

        KeyStore keyStore;
        try (InputStream keystoreInputStream = Files.newInputStream(keyStoreFile.toPath())) {
            String type = StringUtils.defaultIfEmpty(configurationService.getKeystoreType(), "JKS");
            LOG.info("Load keystore [{}] with type [{}].", keyStoreFile, type);
            keyStore = KeyStore.getInstance(type);
            keyStore.load(keystoreInputStream, keystoreSecToken.toCharArray());
        } catch (Exception exception) {
            LOG.error("Could not load signing certificate with private key from keystore file:"
                    + keyStoreFile + " Error: " + ExceptionUtils.getRootCauseMessage(exception), exception);
            keyStore = null;
        }
        return keyStore;
    }

    private void loadKeyAndCert(KeyStore keyStore, String alias, String keySecurityToken, Map<String, Key> hmKeys, Map<String, X509Certificate> hmCertificates) throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException {
        Key key = keyStore.getKey(alias, keySecurityToken.toCharArray());
        Certificate certificate = keyStore.getCertificate(alias);
        if (key == null || certificate == null || !(certificate instanceof X509Certificate)) {
            LOG.warn("Wrong entry type found in keystore, only certificates with keypair are accepted, entry alias: [{}]. Entry is ignored", alias);
            return;
        }
        // add to cache
        hmKeys.put(alias, key);
        hmCertificates.put(alias, (X509Certificate) certificate);
    }

    public List<CertificateRO> getKeystoreEntriesList() {

        if (isKeyStoreChanged()) {
            refreshData();
            // refresh also the list
            certificateROList.clear();
        }
        if (certificateROList.isEmpty() && !keystoreCertificates.isEmpty()) {
            keystoreCertificates.forEach((alias, cert) -> {
                CertificateRO certificateRO = convertToRo(cert);
                basicCertificateValidation(cert, certificateRO);
                certificateRO.setAlias(alias);
                certificateRO.setContainingKey(keystoreKeys.containsKey(alias));
                certificateROList.add(certificateRO);
            });
        }

        return certificateROList;
    }

    public CertificateRO convertToRo(X509Certificate d) {
        return conversionService.convert(d, CertificateRO.class);
    }

    public Key getKey(String keyAlias) {

        if (isKeyStoreChanged()) {
            refreshData();
        }

        if (keystoreKeys.isEmpty()) {
            throw new SMPRuntimeException(ErrorCode.CONFIGURATION_ERROR, "Could not retrieve key: [" + keyAlias + "] from empty keystore: [" + configurationService.getKeystoreFile() +"]!");
        }


        if (keystoreKeys.size() == 1) {
            // for backward compatibility...
            // don't care about configured alias in single-domain setup
            // and return the only key
            LOG.warn("Returning the only key in keystore regardless the configuration");
            return keystoreKeys.values().iterator().next();
        }

        if (isBlank(keyAlias) || !keystoreKeys.containsKey(keyAlias)) {
            throw new SMPRuntimeException(ErrorCode.CONFIGURATION_ERROR, "Wrong configuration, missing key pair from keystore or wrong alias: " + keyAlias);
        }

        return keystoreKeys.get(keyAlias);
    }

    public X509Certificate getCert(String certAlias) {

        if (isKeyStoreChanged()) {
            refreshData();
        }

        if (keystoreCertificates.size() == 1) {
            // for backward compatibility...
            // don't care about configured alias in single-domain setup
            // and return the only key
            LOG.warn("Returning the only certificate in keystore regardless the configuration");
            return keystoreCertificates.values().iterator().next();
        }
        if (isBlank(certAlias) || !keystoreCertificates.containsKey(certAlias)) {
            throw new SMPRuntimeException(ErrorCode.CONFIGURATION_ERROR, "Wrong configuration, missing key pair from keystore or wrong alias: " + certAlias);
        }
        return keystoreCertificates.get(certAlias);
    }

    /**
     * Import keys smp keystore
     *
     * @param newKeystore  new keystore file to import
     * @param password    password for new keystore file
     */
    public List<CertificateRO> importKeys(KeyStore newKeystore, String password) throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, IOException, CertificateException {

        String keystoreSecToken = configurationService.getKeystoreCredentialToken();
        KeyStore keyStore = loadKeystore(configurationService.getKeystoreFile(), keystoreSecToken);
        if (keyStore != null) {
            List<String> listAliases = KeystoreUtils.mergeKeystore(keyStore, keystoreSecToken, newKeystore, password);
            // store keystore
            storeKeystore(keyStore);
            // refresh and return added list of certificates
            List<CertificateRO> keystoreEntries = getKeystoreEntriesList();
            return keystoreEntries.stream().filter(cert -> listAliases.contains(cert.getAlias())).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    /**
     * Delete keys smp keystore
     *
     * @param alias alias of the key to delete from keystore
     */
    public X509Certificate deleteKey(String alias) throws NoSuchAlgorithmException, KeyStoreException, IOException, CertificateException {
        String keystoreSecToken = configurationService.getKeystoreCredentialToken();
        KeyStore keyStore = loadKeystore(configurationService.getKeystoreFile(), keystoreSecToken);

        if (keyStore == null || !keyStore.containsAlias(alias)) {
            return null;
        }
        X509Certificate certificate = (X509Certificate) keyStore.getCertificate(alias);
        keyStore.deleteEntry(alias);
        // store keystore
        storeKeystore(keyStore);
        refreshData();
        return certificate;
    }

    /**
     * Store keystore
     *
     * @param keyStore to store
     * @throws IOException if the keystore can not be persisted
     * @throws CertificateException if keystore cannot be stored
     * @throws NoSuchAlgorithmException if keystore type algorithm is not supported
     * @throws KeyStoreException if keystore cannot be stored
     */
    private void storeKeystore(KeyStore keyStore) throws IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException {
        File keystoreFilePath = configurationService.getKeystoreFile();
        String keystoreSecToken = configurationService.getKeystoreCredentialToken();
        try (FileOutputStream fos = new FileOutputStream(keystoreFilePath)) {
            keyStore.store(fos, keystoreSecToken.toCharArray());
        }
    }
}
