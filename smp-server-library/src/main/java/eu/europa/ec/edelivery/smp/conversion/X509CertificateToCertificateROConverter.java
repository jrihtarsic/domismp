package eu.europa.ec.edelivery.smp.conversion;

import eu.europa.ec.edelivery.security.PreAuthenticatedCertificatePrincipal;
import eu.europa.ec.edelivery.smp.data.ui.CertificateRO;
import eu.europa.ec.edelivery.smp.exceptions.ErrorCode;
import eu.europa.ec.edelivery.smp.exceptions.SMPRuntimeException;
import eu.europa.ec.edelivery.smp.logging.SMPLogger;
import eu.europa.ec.edelivery.smp.logging.SMPLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Base64;

/**
 * @author Joze Rihtarsic
 */
@Component
public class X509CertificateToCertificateROConverter implements Converter<X509Certificate, CertificateRO> {

    private static final SMPLogger LOG = SMPLoggerFactory.getLogger(CertificateROToDBCertificateConverter.class);

    private static final String S_BLUECOAT_DATEFORMAT = "MMM dd HH:mm:ss yyyy";

    @Override
    public CertificateRO convert(X509Certificate cert) {

        String subject = cert.getSubjectDN().getName();
        String issuer = cert.getIssuerDN().getName();
        String hash = cert.getIssuerDN().getName();
        BigInteger serial = cert.getSerialNumber();
        String certId = getCertificateIdFromCertificate(subject, issuer, serial);
        CertificateRO cro = new CertificateRO();
        cro.setCertificateId(certId);
        cro.setSubject(subject);
        cro.setIssuer(issuer);
        // set serial as HEX
        cro.setSerialNumber(serial.toString(16));
        cro.setValidFrom(cert.getNotBefore());
        cro.setValidTo(cert.getNotAfter());
        try {
            cro.setEncodedValue(Base64.getMimeEncoder().encodeToString(cert.getEncoded()));
        } catch (CertificateEncodingException cex) {
            throw new SMPRuntimeException(ErrorCode.CERTIFICATE_ERROR, cex,
                    "Error occured while decoding certificate " + subject, cex.getMessage(), cex);

        }
        // generate bluecoat header
        SimpleDateFormat sdf = new SimpleDateFormat(S_BLUECOAT_DATEFORMAT);
        StringWriter sw = new StringWriter();
        sw.write("sno=");
        sw.write(serial.toString(16));
        sw.write("&subject=");
        sw.write(urlEncodeString(subject));
        sw.write("&validfrom=");
        sw.write(urlEncodeString(sdf.format(cert.getNotBefore()) + " GMT"));
        sw.write("&validto=");
        sw.write(urlEncodeString(sdf.format(cert.getNotAfter()) + " GMT"));
        sw.write("&issuer=");
        sw.write(urlEncodeString(issuer));
        cro.setBlueCoatHeader(sw.toString());
        return cro;
    }

    public String getCertificateIdFromCertificate(String subject, String issuer, BigInteger serial) {
        return new PreAuthenticatedCertificatePrincipal(subject, issuer, serial).getName();
    }

    private String urlEncodeString(String val) {
        if (StringUtils.isBlank(val)) {
            return "";
        } else {
            try {
                return URLEncoder.encode(val, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                LOG.error("Error occurred while url encoding the certificate string:" + val, e);
            }
        }
        return "";
    }

}
