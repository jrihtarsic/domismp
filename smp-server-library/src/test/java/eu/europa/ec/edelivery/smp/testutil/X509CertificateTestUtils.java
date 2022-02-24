package eu.europa.ec.edelivery.smp.testutil;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.*;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import java.io.FileInputStream;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.stream.Collectors;

public class X509CertificateTestUtils {

    public static X509Certificate createX509CertificateForTest( String subject) throws Exception {
        Calendar from = Calendar.getInstance();
        Calendar to = Calendar.getInstance();
        to.add(Calendar.DAY_OF_YEAR, 1);
        from.add(Calendar.DAY_OF_YEAR, -1);
        return createX509CertificateForTest("1234321", subject, subject, from.getTime(), to.getTime(), Collections.emptyList());
    }

    public static X509Certificate createX509CertificateForTest(String serialNumber, String issuer, String subject, Date startDate, Date expiryDate, List<String> distributionList) throws Exception {

        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(1024);
        KeyPair key = keyGen.generateKeyPair();
        X509v3CertificateBuilder certBuilder = new X509v3CertificateBuilder(new X500Name(issuer), new BigInteger(serialNumber, 16), startDate, expiryDate, new X500Name(subject), SubjectPublicKeyInfo.getInstance(key.getPublic().getEncoded()));
        if (!distributionList.isEmpty()) {

            List<DistributionPoint> distributionPoints = distributionList.stream().map(url -> {
                DistributionPointName distPointOne = new DistributionPointName(new GeneralNames(
                        new GeneralName(GeneralName.uniformResourceIdentifier, url)));

                return new DistributionPoint(distPointOne, null, null);
            }).collect(Collectors.toList());

            certBuilder.addExtension(Extension.cRLDistributionPoints, false, new CRLDistPoint(distributionPoints.toArray(new DistributionPoint[]{})));
        }

        ContentSigner sigGen = new JcaContentSignerBuilder("SHA1WithRSAEncryption").setProvider("BC").build(key.getPrivate());
        return new JcaX509CertificateConverter().setProvider("BC").getCertificate(certBuilder.build(sigGen));
    }

    public static X509Certificate[] createCertificateChain(String[] subjects, Date startDate, Date expiryDate) throws Exception {

        String issuer = null;
        PrivateKey issuerKey = null;
        long iSerial = 10000;
        X509Certificate[] certs = new X509Certificate[subjects.length];

        int index = subjects.length;
        for (String sbj: subjects){
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(1024);
            KeyPair key = keyGen.generateKeyPair();

            X509v3CertificateBuilder certBuilder = new X509v3CertificateBuilder(new X500Name(issuer ==null? sbj:issuer),
                    BigInteger.valueOf(iSerial++), startDate, expiryDate, new X500Name(sbj),
                    SubjectPublicKeyInfo.getInstance(key.getPublic().getEncoded()));

            ContentSigner sigGen = new JcaContentSignerBuilder("SHA256WITHRSA")
                    .setProvider("BC").build(issuerKey ==null?key.getPrivate():issuerKey);

            certs[--index] = new JcaX509CertificateConverter().setProvider("BC").getCertificate(certBuilder.build(sigGen));
            issuer= sbj;
            issuerKey = key.getPrivate();

        }
        return certs;
    }

    /**
     *  Method generates certificate chain
     * @param subjects
     * @param certificatePoliciesOids
     * @param startDate
     * @param expiryDate
     * @return
     * @throws Exception
     */
    public static X509Certificate[] createCertificateChain(String[] subjects,  List<List<String>> certificatePoliciesOids,  Date startDate, Date expiryDate) throws Exception {

        String issuer = null;
        PrivateKey issuerKey = null;
        long iSerial = 10000;
        X509Certificate[] certs = new X509Certificate[subjects.length];

        int index = subjects.length;
        for (String sbj: subjects){
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(1024);
            KeyPair key = keyGen.generateKeyPair();

            X509v3CertificateBuilder certBuilder = new X509v3CertificateBuilder(new X500Name(issuer ==null? sbj:issuer),
                    BigInteger.valueOf(iSerial++), startDate, expiryDate, new X500Name(sbj),
                    SubjectPublicKeyInfo.getInstance(key.getPublic().getEncoded()));

            ContentSigner sigGen = new JcaContentSignerBuilder("SHA256WITHRSA")
                    .setProvider("BC").build(issuerKey ==null?key.getPrivate():issuerKey);

            certs[--index] = new JcaX509CertificateConverter().setProvider("BC").getCertificate(certBuilder.build(sigGen));
            issuer= sbj;
            issuerKey = key.getPrivate();

        }
        return certs;
    }
}
