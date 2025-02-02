package rest.models;

import ddsl.enums.ResponseCertificates;
import utils.Generator;
import utils.Utils;

import java.util.List;

import static ddsl.enums.ResponseCertificates.SMP_DOMAIN_01;
import static ddsl.enums.ResponseCertificates.SMP_DOMAIN_02;

/**
 * Data model for Domain used for generating domains, mapping domain rest calls.
 */
public class DomainModel {

    private String smlSmpId;
    private String domainCode;
    private String domainId;
    private boolean smlRegistered;
    private String visibility;
    private String smlClientKeyAlias;
    private String signatureKeyAlias;
    private String smlSubdomain;
    private String smlParticipantIdentifierRegExp;
    private boolean smlClientCertAuth;
    private long adminMemberCount = -1;
    private Object actionMessage;
    private Object defaultResourceTypeIdentifier;
    private List<Object> groups;
    private Long index;
    private List<String> resourceDefinitions;
    private Long status;
    public DomainModel() {
    }
    public String getSmlSmpId() {
        return smlSmpId;
    }

    public String getDomainCode() {
        return domainCode;
    }

    public boolean isSmlRegistered() {
        return smlRegistered;
    }

    public String getVisibility() {
        return visibility.toUpperCase();
    }

    public String getSmlClientKeyAlias() {
        return smlClientKeyAlias;
    }

    public String getSignatureKeyAlias() {
        return signatureKeyAlias;
    }

    public String getSmlSubdomain() {
        return smlSubdomain;
    }

    public String getSmlParticipantIdentifierRegExp() {
        return smlParticipantIdentifierRegExp;
    }

    public boolean isSmlClientCertAuth() {
        return smlClientCertAuth;
    }

    public void setIndex(Long index) {
        this.index = index;
    }

    public void setStatus(Long status) {
        this.status = status;
    }

    public void setResourceDefinitions(List<String> resourceDefinitions) {
        this.resourceDefinitions = resourceDefinitions;
    }

    public long getAdminMemberCount() {
        return adminMemberCount;
    }

    public void setAdminMemberCount(long adminMemberCount) {
        this.adminMemberCount = adminMemberCount;
    }

    public void setSmlSmpId(String smlSmpId) {
        this.smlSmpId = smlSmpId;
    }

    public void setDomainCode(String domainCode) {
        this.domainCode = domainCode;
    }

    public String getDomainId() {
        return domainId;
    }

    public void setDomainId(String domainId) {
        this.domainId = domainId;
    }

    public void setSmlRegistered(boolean smlRegistered) {
        this.smlRegistered = smlRegistered;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public void setSmlClientKeyAlias(String smlClientKeyAlias) {
        this.smlClientKeyAlias = smlClientKeyAlias;
    }

    public void setSignatureKeyAlias(String signatureKeyAlias) {
        this.signatureKeyAlias = signatureKeyAlias;
    }

    public void setSmlSubdomain(String smlSubdomain) {
        this.smlSubdomain = smlSubdomain;
    }

    public void setSmlParticipantIdentifierRegExp(String smlParticipantIdentifierRegExp) {
        this.smlParticipantIdentifierRegExp = smlParticipantIdentifierRegExp;
    }

    public void setSmlClientCertAuth(boolean smlClientCertAuth) {
        this.smlClientCertAuth = smlClientCertAuth;
    }

    public void setActionMessage(Object actionMessage) {
        this.actionMessage = actionMessage;
    }

    public void setDefaultResourceTypeIdentifier(Object defaultResourceTypeIdentifier) {
        this.defaultResourceTypeIdentifier = defaultResourceTypeIdentifier;
    }

    public void setGroups(List<Object> groups) {
        this.groups = groups;
    }

    public static DomainModel generatePublicDomainModelWithSML() {
        DomainModel domainModel = new DomainModel();
        domainModel.domainCode = "AUTDom" + Generator.randomAlphaNumericValue(6);
        domainModel.signatureKeyAlias = Utils.randomEnum(new ResponseCertificates[]{SMP_DOMAIN_01, SMP_DOMAIN_02}).getAlias();
        domainModel.visibility = "PUBLIC";
        domainModel.smlClientCertAuth = true;
        domainModel.smlSubdomain = "AUTDomSML" + Generator.randomAlphaNumericValue(6);
        domainModel.smlSmpId = "AUTSMLSMP" + Generator.randomAlphaNumericValue(4);
        domainModel.smlClientKeyAlias = Utils.randomEnum(new ResponseCertificates[]{SMP_DOMAIN_01, SMP_DOMAIN_02}).getAlias();
        return domainModel;
    }

    public static DomainModel generatePublicDomainModelWithoutSML() {
        DomainModel domainModel = new DomainModel();
        domainModel.domainCode = "AUTDom" + Generator.randomAlphaNumericValue(6);
        domainModel.signatureKeyAlias = Utils.randomEnum(ResponseCertificates.values()).getAlias();
        domainModel.visibility = "PUBLIC";
        return domainModel;
    }
}


