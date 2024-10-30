package domiSMPTests.ui;

import com.fasterxml.jackson.core.JsonProcessingException;
import ddsl.DomiSMPPage;
import ddsl.enums.Pages;
import ddsl.enums.ResourceTypes;
import domiSMPTests.SeleniumTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.LoginPage;
import pages.administration.editResourcesPage.CreateSubresourceDetailsDialog;
import pages.administration.editResourcesPage.EditResourcePage;
import pages.administration.editResourcesPage.editResourceDocumentPage.EditResourceDocumentPage;
import pages.administration.editResourcesPage.editResourceDocumentPage.EditResourceDocumentWizardDialog;
import pages.administration.editResourcesPage.editResourceDocumentPage.EditSubresourceDocumentPage;
import pages.administration.editResourcesPage.editResourceDocumentPage.SubresourceWizardDialog;
import pages.search.ResourcesPage;
import rest.models.*;
import utils.FileUtils;
import utils.Generator;
import utils.TestRunData;
import utils.XMLUtils;

import javax.xml.parsers.ParserConfigurationException;
import java.util.Arrays;
import java.util.List;

public class EditResourcePgTests extends SeleniumTest {
    DomiSMPPage homePage;
    LoginPage loginPage;
    EditResourcePage editResourcePage;
    UserModel adminUser;
    DomainModel domainModel;
    GroupModel groupModel;
    ResourceModel resourceModel;
    SoftAssert soft;

    MemberModel adminMember;

    @BeforeMethod(alwaysRun = true)
    public void beforeTest() throws Exception {
        soft = new SoftAssert();
        domainModel = DomainModel.generatePublicDomainModelWithSML();
        adminUser = UserModel.generateUserWithADMINrole();
        groupModel = GroupModel.generatePublicGroup();
        resourceModel = ResourceModel.generatePublicResourceUnregisteredToSML();

        adminMember = new MemberModel() {
        };
        adminMember.setUsername(adminUser.getUsername());
        adminMember.setRoleType("ADMIN");
        adminMember.setHasPermissionReview(true);

        MemberModel superMember = new MemberModel();
        superMember.setUsername(TestRunData.getInstance().getAdminUsername());
        superMember.setRoleType("ADMIN");

        //create user
        rest.users().createUser(adminUser).getString("userId");

        //create domain
        domainModel = rest.domains().createDomain(domainModel);

        //add users to domain
        rest.domains().addMembersToDomain(domainModel, adminMember);
        rest.domains().addMembersToDomain(domainModel, superMember);

        //add resources to domain
        List<ResourceTypes> resourcesToBeAdded = Arrays.asList(ResourceTypes.OASIS1, ResourceTypes.OASIS3, ResourceTypes.OASIS2);
        domainModel = rest.domains().addResourcesToDomain(domainModel, resourcesToBeAdded);

        //create group for domain
        groupModel = rest.domains().createGroupForDomain(domainModel, groupModel);

        //add users to groups
        rest.groups().addMembersToGroup(domainModel, groupModel, adminMember);

        //add resource to group
        resourceModel = rest.resources().createResourceForGroup(domainModel, groupModel, resourceModel);
        rest.resources().addMembersToResource(domainModel, groupModel, resourceModel, adminMember);


        homePage = new DomiSMPPage(driver);
        loginPage = homePage.goToLoginPage();
        loginPage.login(adminUser.getUsername(), TestRunData.getInstance().getNewPassword());
        editResourcePage = homePage.getSidebar().navigateTo(Pages.ADMINISTRATION_EDIT_RESOURCES);
    }

    @Test(description = "EDTRES-01 Resource admins are able to invite/edit/remove resource members")
    public void resourceAdminsAreAbleToInviteEditRemoveMembers() {

        UserModel domainMember = UserModel.generateUserWithUSERrole();
        rest.users().createUser(domainMember);

        editResourcePage.selectDomain(domainModel, groupModel, resourceModel);
        editResourcePage.goToTab("Members");
        //Add user
        editResourcePage.getResourceMembersTab().getInviteMemberBtn().click();
        editResourcePage.getResourceMembersTab().getInviteMembersPopup().selectMember(domainMember.getUsername(), "VIEWER");
        soft.assertTrue(editResourcePage.getResourceMembersTab().getMembersGrid().isValuePresentInColumn("Username", domainMember.getUsername()));

        //Change role of user
        editResourcePage.getResourceMembersTab().changeRoleOfUser(domainMember.getUsername(), "ADMIN");
        String currentRoleTypeOfuser = editResourcePage.getResourceMembersTab().getMembersGrid().getColumnValueForSpecificRow("Username", domainMember.getUsername(), "Role type");
        soft.assertEquals(currentRoleTypeOfuser, "ADMIN");

        //Remove user
        editResourcePage.getResourceMembersTab().removeUser(domainMember.getUsername());
        soft.assertFalse(editResourcePage.getResourceMembersTab().getMembersGrid().isValuePresentInColumn("Username", domainMember.getUsername()));

        soft.assertAll();
    }

    @Test(description = "EDTRES-02 Resource admins are to view their resources", priority = 1)
    public void resourceAdminsAreAbleToViewTheirResources() {

        UserModel domainMember = UserModel.generateUserWithUSERrole();
        rest.users().createUser(domainMember);

        editResourcePage.selectDomain(domainModel, groupModel, resourceModel);
        editResourcePage.goToTab("Members");
        //Add user
        editResourcePage.getResourceMembersTab().getInviteMemberBtn().click();
        editResourcePage.getResourceMembersTab().getInviteMembersPopup().selectMember(domainMember.getUsername(), "VIEWER");
        soft.assertTrue(editResourcePage.getResourceMembersTab().getMembersGrid().isValuePresentInColumn("Username", domainMember.getUsername()));
    }

    @Test(description = "EDTRES-03 Resource admins are able to add generated document", priority = 1)
    public void resourceAdminsAreAbleToAddGeneratedDocument() throws Exception {

        //Generate resource Oasis 3
        ResourceModel resourceModelOasis3 = ResourceModel.generatePublicResourceUnregisteredToSML();
        resourceModelOasis3.setResourceTypeIdentifier(ResourceTypes.OASIS3.getName());
        resourceModelOasis3 = rest.resources().createResourceForGroup(domainModel, groupModel, resourceModelOasis3);
        rest.resources().addMembersToResource(domainModel, groupModel, resourceModelOasis3, adminMember);

        editResourcePage.refreshPage();
        editResourcePage.selectDomain(domainModel, groupModel, resourceModelOasis3);
        editResourcePage.goToTab("Resource details");
        EditResourceDocumentPage editResourceDocumentPage = editResourcePage.getResourceDetailsTab().clickOnEditDocument();
        editResourceDocumentPage.clickOnNewVersion();
        editResourceDocumentPage.clickOnGenerate();
        editResourceDocumentPage.clickOnSave();
        editResourceDocumentPage.getAlertArea().closeAlert();

        String currentGeneratedValue = editResourceDocumentPage.getDocumentValue();

        editResourceDocumentPage.clickOnValidate();
        soft.assertEquals(editResourceDocumentPage.getAlertArea().getAlertMessage(), "Document is Valid.", "Generated document is not valid");

        soft.assertNotNull(currentGeneratedValue, "Document is empty");
        XMLUtils documentXML = new XMLUtils(currentGeneratedValue);
        soft.assertTrue(documentXML.isNodePresent("CPP"), "Node is not present in generated document");

        editResourcePage = homePage.getSidebar().navigateTo(Pages.ADMINISTRATION_EDIT_RESOURCES);

        //Generate resource Oasis 2
        ResourceModel resourceModelOasis2 = ResourceModel.generatePublicResourceUnregisteredToSML();
        resourceModelOasis2.setResourceTypeIdentifier(ResourceTypes.OASIS2.getName());
        resourceModelOasis2 = rest.resources().createResourceForGroup(domainModel, groupModel, resourceModelOasis2);
        rest.resources().addMembersToResource(domainModel, groupModel, resourceModelOasis2, adminMember);

        editResourcePage.refreshPage();
        editResourcePage.selectDomain(domainModel, groupModel, resourceModelOasis2);
        editResourcePage.goToTab("Resource details");
        editResourceDocumentPage = editResourcePage.getResourceDetailsTab().clickOnEditDocument();
        editResourceDocumentPage.clickOnNewVersion();
        editResourceDocumentPage.clickOnGenerate();
        editResourceDocumentPage.clickOnSave();
        editResourceDocumentPage.getAlertArea().closeAlert();
        String oasis2GeneratedDocumentValue = editResourceDocumentPage.getDocumentValue();

        editResourceDocumentPage.clickOnValidate();

        soft.assertEquals(editResourceDocumentPage.getAlertArea().getAlertMessage(), "Document is Valid.", "Generated document is not valid");
        soft.assertNotNull(oasis2GeneratedDocumentValue, "Document is empty");
        XMLUtils oasis2DocumentXML = new XMLUtils(oasis2GeneratedDocumentValue);
        soft.assertTrue(oasis2DocumentXML.isNodePresent("ns5:ServiceGroup"), " Service group Node is not present in generated document");

        //Generate resource Oasis 3
        ResourceModel resourceModelOasis1 = ResourceModel.generatePublicResourceUnregisteredToSML();
        resourceModelOasis1.setResourceTypeIdentifier(ResourceTypes.OASIS1.getName());
        resourceModelOasis1 = rest.resources().createResourceForGroup(domainModel, groupModel, resourceModelOasis1);
        rest.resources().addMembersToResource(domainModel, groupModel, resourceModelOasis1, adminMember);

        editResourcePage = homePage.getSidebar().navigateTo(Pages.ADMINISTRATION_EDIT_RESOURCES);
        editResourcePage.refreshPage();
        editResourcePage.selectDomain(domainModel, groupModel, resourceModelOasis1);
        editResourcePage.goToTab("Resource details");
        editResourceDocumentPage = editResourcePage.getResourceDetailsTab().clickOnEditDocument();
        editResourceDocumentPage.clickOnNewVersion();
        editResourceDocumentPage.clickOnGenerate();
        editResourceDocumentPage.clickOnSave();
        editResourceDocumentPage.getAlertArea().closeAlert();


        String oasis1GeneratedDocumentValue = editResourceDocumentPage.getDocumentValue();

        editResourceDocumentPage.clickOnValidate();

        soft.assertEquals(editResourceDocumentPage.getAlertArea().getAlertMessage(), "Document is Valid.", "Generated document is not valid");
        soft.assertNotNull(oasis1GeneratedDocumentValue, "Document is empty");
        XMLUtils oasis1DocumentXML = new XMLUtils(oasis1GeneratedDocumentValue);
        soft.assertTrue(oasis1DocumentXML.isNodePresent("ServiceGroup"), " Service group Node is not present in generated document");
        soft.assertAll();
    }

    @Test(description = "EDTRES-04 Resource admins are able to add document using Document wizard for Oasis 1.0", priority = 1)
    public void resourceAdminsAreAbleToAddDocimentUsingDocumentWizardOasis1() throws ParserConfigurationException, JsonProcessingException {

        ResourceModel resourceModelOasis1 = ResourceModel.generatePublicResource(ResourceTypes.OASIS1);


        //add resource to group
        resourceModelOasis1 = rest.resources().createResourceForGroup(domainModel, groupModel, resourceModelOasis1);
        rest.resources().addMembersToResource(domainModel, groupModel, resourceModelOasis1, adminMember);

        editResourcePage.refreshPage();
        editResourcePage.selectDomain(domainModel, groupModel, resourceModelOasis1);

        editResourcePage.goToTab("Resource details");
        EditResourceDocumentPage editResourceDocumentPage = editResourcePage.getResourceDetailsTab().clickOnEditDocument();
        editResourceDocumentPage.clickOnNewVersion();
        editResourceDocumentPage.clickOnGenerate();
        editResourceDocumentPage.clickOnSave();
        editResourceDocumentPage.getAlertArea().closeAlert();
        editResourceDocumentPage.clickOnValidate();
        soft.assertEquals(editResourceDocumentPage.getAlertArea().getAlertMessage(), "Document is Valid.");

        EditResourceDocumentWizardDialog editResourceDocumentWizardDialog = editResourceDocumentPage.clickOnDocumentWizard();

        String generatedExtensionIdvalue = Generator.randomAlphaNumericValue(8);
        String generatedExtensionNamevalue = Generator.randomAlphaNumericValue(8);
        String generatedExtensionAgencyIdvalue = Generator.randomAlphaNumericValue(8);
        String generatedExtensionAgencyNamevalue = Generator.randomAlphaNumericValue(8);
        String generatedExtensionAgencyURIvalue = "www." + Generator.randomAlphaNumericValue(8) + ".com";
        String generatedExtensionVersionIdvalue = Generator.randomAlphaNumericValue(8);
        String generatedExtensionURIvalue = "www." + Generator.randomAlphaNumericValue(8) + ".com";
        String generatedExtensionReasonCodevalue = Generator.randomAlphaNumericValue(8);
        String generatedExtensionReasonvalue = Generator.randomAlphaNumericValue(8);


        editResourceDocumentWizardDialog.getExtensionIdInput().fill(generatedExtensionIdvalue);
        editResourceDocumentWizardDialog.getExtensionNamenput().fill(generatedExtensionNamevalue);
        editResourceDocumentWizardDialog.getExtensionAgencyIdnput().fill(generatedExtensionAgencyIdvalue);
        editResourceDocumentWizardDialog.getExtensionAgencyNameInput().fill(generatedExtensionAgencyNamevalue);
        editResourceDocumentWizardDialog.getExtensionAgencyURIInput().fill(generatedExtensionAgencyURIvalue);
        editResourceDocumentWizardDialog.getExtensionVersionIDInput().fill(generatedExtensionVersionIdvalue);
        editResourceDocumentWizardDialog.getExtensionURIInput().fill(generatedExtensionURIvalue);
        editResourceDocumentWizardDialog.getExtensionReasonCodeInput().fill(generatedExtensionReasonCodevalue);
        editResourceDocumentWizardDialog.getExtensionReasonInput().fill(generatedExtensionReasonvalue);

        editResourceDocumentWizardDialog.clickOK();
        editResourceDocumentPage.clickOnSave();
        editResourceDocumentPage.clickOnValidate();

        String document = editResourceDocumentPage.getDocumentValue();
        XMLUtils documentXML = new XMLUtils(document);


        soft.assertEquals(documentXML.getNodeValue("ExtensionID"), generatedExtensionIdvalue, "Wrong ExtensionId value");
        soft.assertEquals(documentXML.getNodeValue("ExtensionName"), generatedExtensionNamevalue, "Wrong ExtensionName value");
        soft.assertEquals(documentXML.getNodeValue("ExtensionAgencyID"), generatedExtensionAgencyIdvalue, "Wrong ExtensionAgencyID value");
        soft.assertEquals(documentXML.getNodeValue("ExtensionAgencyName"), generatedExtensionAgencyNamevalue, "Wrong ExtensionAgencyName value");
        soft.assertEquals(documentXML.getNodeValue("ExtensionAgencyURI"), generatedExtensionAgencyURIvalue, "Wrong ExtensionAgencyURI value");
        soft.assertEquals(documentXML.getNodeValue("ExtensionVersionID"), generatedExtensionVersionIdvalue, "Wrong ExtensionVersionID value");
        soft.assertEquals(documentXML.getNodeValue("ExtensionURI"), generatedExtensionURIvalue, "Wrong ExtensionURI value");
        soft.assertEquals(documentXML.getNodeValue("ExtensionReasonCode"), generatedExtensionReasonCodevalue, "Wrong ExtensionReasonCode value");
        soft.assertEquals(documentXML.getNodeValue("ExtensionReason"), generatedExtensionReasonvalue, "Wrong ExtensionReason value");

        soft.assertAll();
    }

    @Test(description = "EDTRES-15 - Resource Administrator can publish resource documents with approve status", priority = 1)
    public void resourceAdministratorsCanPublisResourceDocumentsWithApproveStatus() throws JsonProcessingException {

        ResourceModel resourceModelOasis1 = ResourceModel.generatePublicResourceWithReview(ResourceTypes.OASIS1);


        //add resource to group
        resourceModelOasis1 = rest.resources().createResourceForGroup(domainModel, groupModel, resourceModelOasis1);
        rest.resources().addMembersToResource(domainModel, groupModel, resourceModelOasis1, adminMember);

        editResourcePage.refreshPage();
        editResourcePage.selectDomain(domainModel, groupModel, resourceModelOasis1);

        editResourcePage.goToTab("Resource details");
        EditResourceDocumentPage editResourceDocumentPage = editResourcePage.getResourceDetailsTab().clickOnEditDocument();
        editResourceDocumentPage.clickOnNewVersion();
        editResourceDocumentPage.clickOnDocumentWizard().getExtensionAgencyIdnput().fill("NewVersion");
        new EditResourceDocumentWizardDialog(driver).clickOK();
        editResourceDocumentPage.clickOnSave();
        editResourceDocumentPage.getAlertArea().closeAlert();

        soft.assertTrue(editResourceDocumentPage.getRequestReviewBtn().isEnabled(), "Request review button is not enabled");
        soft.assertEquals("DRAFT", editResourceDocumentPage.getStatusValue());

        //Request review
        editResourceDocumentPage.getRequestReviewBtn().click();
        soft.assertEquals("UNDER_REVIEW", editResourceDocumentPage.getStatusValue());

        //Self approve
        soft.assertTrue(editResourceDocumentPage.getApproveBtn().isEnabled(), "Approve button is not enabled");
        editResourceDocumentPage.clickOnApproveAndConfirm();
        soft.assertEquals("APPROVED", editResourceDocumentPage.getStatusValue());

        soft.assertTrue(editResourceDocumentPage.getPublishBtn().isEnabled(), "Publish is not enabled");
        editResourceDocumentPage.clickOnPublishAndConfirm();
        soft.assertEquals("PUBLISHED", editResourceDocumentPage.getStatusValue());
        editResourceDocumentPage.selectVersion(1);
        soft.assertEquals("RETIRED", editResourceDocumentPage.getStatusValue());
        ResourcesPage resourcesPage = editResourceDocumentPage.getSidebar().navigateTo(Pages.SEARCH_RESOURCES);
        XMLUtils documentXML = resourcesPage.openURLResouceDocument(resourceModelOasis1.getIdentifierValue(), resourceModelOasis1.getIdentifierScheme());
        soft.assertEquals(documentXML.getNodeValue("ExtensionAgencyID"), "NewVersion", "Document value is wrong");
        soft.assertAll();

    }

    @Test(description = "EDTRES-15 - Resource Administrator can publish subresource documents with approve status", priority = 1)

    public void resourceAdministratorsCanPublisSUBResourceDocumentsWithApproveStatus() throws Exception {

        ResourceModel resourceModelOasis1 = ResourceModel.generatePublicResourceWithReview(ResourceTypes.OASIS1);
        SubresourceModel subresourceModel = SubresourceModel.generatePublicSubResource();

        //add resource to group
        resourceModelOasis1 = rest.resources().createResourceForGroup(domainModel, groupModel, resourceModelOasis1);
        rest.resources().addMembersToResource(domainModel, groupModel, resourceModelOasis1, adminMember);

        editResourcePage.refreshPage();
        editResourcePage.selectDomain(domainModel, groupModel, resourceModelOasis1);

        editResourcePage.goToTab("Subresources");
        CreateSubresourceDetailsDialog createSubresourceDetailsDialog = editResourcePage.getSubresourceTab().createSubresource();
        createSubresourceDetailsDialog.fillResourceDetails(subresourceModel);
        createSubresourceDetailsDialog.tryClickOnSave();

        EditSubresourceDocumentPage editSubresourceDocumentPage = editResourcePage.getSubresourceTab().editSubresouceDocument(subresourceModel);
        editSubresourceDocumentPage.clickOnNewVersion();
        SubresourceWizardDialog subresourceWizardDialog = editSubresourceDocumentPage.clickOnDocumentWizard();
        subresourceWizardDialog.processIdentifierInput().fill("123-123-123");
        subresourceWizardDialog.accessPointUrlInput().fill("www.domibustest.com");
        String path = FileUtils.getAbsoluteTruststorePath("validCertificate.cer");


        subresourceWizardDialog.uploadCertificateBtn(path);
        subresourceWizardDialog.clickOK();

        editSubresourceDocumentPage.clickOnSave();
        editSubresourceDocumentPage.getAlertArea().closeAlert();

        soft.assertTrue(editSubresourceDocumentPage.getRequestReviewBtn().isEnabled(), "Request review button is not enabled");
        soft.assertEquals("DRAFT", editSubresourceDocumentPage.getStatusValue());

        //Request review
        editSubresourceDocumentPage.getRequestReviewBtn().click();
        soft.assertEquals("UNDER_REVIEW", editSubresourceDocumentPage.getStatusValue());

        //Self approve
        soft.assertTrue(editSubresourceDocumentPage.getApproveBtn().isEnabled(), "Approve button is not enabled");
        editSubresourceDocumentPage.clickOnApproveAndConfirm();
        soft.assertEquals("APPROVED", editSubresourceDocumentPage.getStatusValue());

        soft.assertTrue(editSubresourceDocumentPage.getPublishBtn().isEnabled(), "Publish is not enabled");
        editSubresourceDocumentPage.clickOnPublishAndConfirm();
        soft.assertEquals("PUBLISHED", editSubresourceDocumentPage.getStatusValue());
        editSubresourceDocumentPage.selectVersion(1);
        soft.assertEquals("RETIRED", editSubresourceDocumentPage.getStatusValue());
        ResourcesPage resourcesPage = editSubresourceDocumentPage.getSidebar().navigateTo(Pages.SEARCH_RESOURCES);
        XMLUtils documentXML = resourcesPage.openURLSubResouceDocument(resourceModelOasis1.getIdentifierValue(), resourceModelOasis1.getIdentifierScheme(), subresourceModel.getIdentifierValue());

        soft.assertEquals(documentXML.getNodeValue("ParticipantIdentifier"), resourceModelOasis1.getIdentifierValue(), "EndpointURI value is wrong");
        soft.assertEquals(documentXML.getNodeValue("DocumentIdentifier"), subresourceModel.getIdentifierValue(), "EndpointURI value is wrong");
        soft.assertEquals(documentXML.getNodeValue("EndpointURI"), "www.domibustest.com", "EndpointURI value is wrong");
        soft.assertAll();
    }
}



