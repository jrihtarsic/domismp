package ui;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.alert.AlertPage;
import pages.alert.AlertRowInfo;
import pages.components.baseComponents.SMPPage;
import pages.login.LoginPage;
import pages.properties.PropertiesPage;
import pages.properties.PropertyPopup;
import pages.properties.PropertyRowInfo;
import pages.service_groups.edit.EditPage;
import pages.service_groups.edit.ServiceGroupPopup;
import pages.service_groups.search.SearchPage;
import utils.Generator;
import utils.enums.SMPMessages;
import utils.rest.SMPRestClient;


public class PropertiesPgTest extends BaseTest{
    @AfterMethod
    public void logoutAndReset(){
        genericLogoutProcedure();
    }


    @BeforeMethod
    public void loginAndGoToDomainPage(){

        SMPPage page = genericLoginProcedure("SYS_ADMIN");

        logger.info("Going to Property page");
        page.sidebar.goToPage(PropertiesPage.class);
    }

    /*@Test(description = "PROP-0")*/
    @Test(enabled = false)
    public void verifyParticipantschemeMandatoryProperty(){

        SoftAssert soft = new SoftAssert();
        String property = "identifiersBehaviour.scheme.mandatory";

        PropertiesPage propertiesPage = new PropertiesPage(driver);
        propertiesPage.propertySearch(property);
        soft.assertTrue(propertiesPage.grid().rowContainPropertyName(property),"The row does not contain the searching property");
        PropertyRowInfo newRow0 = propertiesPage.grid().getRows().get(0);
        System.out.println("newRow0.getPropertyvalue() "+newRow0.getPropertyvalue());
        if(newRow0.getPropertyvalue().equals("true"))
        {
           propertiesPage.grid().selectRow(0);
           PropertyPopup popup = propertiesPage.clickEdit();
           propertiesPage = popup.disableCheckboxOfProperty();
            soft.assertTrue(propertiesPage.isSaveButtonEnabled(),"Save button is disbled");
            propertiesPage.clickSave().confirm();
            soft.assertTrue(propertiesPage.alertArea.getAlertMessage().isError(), "Message listed is success");
            soft.assertTrue(propertiesPage.alertArea.getAlertMessage().getMessage().equalsIgnoreCase(SMPMessages.MSG_18), "Message is not listed is as expected");
       }

        propertiesPage.pageHeader.sandwichMenu.logout();
        SearchPage page = new SearchPage(driver);
        logger.info("Going to login page");
        page.pageHeader.goToLogin();
        SMPPage smpPage= genericLoginProcedure("SMP_ADMIN");
        smpPage.sidebar.goToPage(EditPage.class);
        EditPage editPage = new EditPage(driver);
        ServiceGroupPopup servicePopup = editPage.clickNew();
        soft.assertTrue(servicePopup.isParticipantIdentifierInputEnabled(), "Participant Identifier field is disabled");
        soft.assertTrue(servicePopup.isParticipantSchemeInputEnabled(), "Participant Scheme field is disabled");
        soft.assertTrue(servicePopup.isOwnersPanelEnabled(), "Owners panel is enabled");
        soft.assertTrue(servicePopup.isDomainsPanelEnabled(), "Domain panel is enabled");
        servicePopup.fillParticipantIdentifier("sonbrat1223");
        servicePopup.chooseFirstOwner();
        servicePopup.chooseFirstDomain();

        soft.assertTrue(servicePopup.isOKButtonEnable(),"Ok button is not enabled after creating servicegroup without scheme");
        soft.assertTrue(servicePopup.isCancelButtonEnable(),"cancel button is not enabled after creating servicegroup without scheme");

        servicePopup.clickOK();

        soft.assertTrue(editPage.isSaveButtonEnabled(),"Save button is not enable after cration of servicegroup without scheme");
        soft.assertTrue(editPage.isCancelButtonEnabled(),"Cancel button is not enable after cration of servicegroup without scheme");
        editPage.clickSave().confirm();

        soft.assertTrue(editPage.alertArea.getAlertMessage().getMessage().equalsIgnoreCase(SMPMessages.MSG_18), "Message is not listed is as expected");

        soft.assertAll();
    }

    @Test(description = "PROP-10")
    public void smpAlertPasswordExpiredMailSubjectAllowString()
    {
        SoftAssert soft = new SoftAssert();
        String property = "smp.alert.password.expired.mail.subject";
        String string = Generator.randomAlphaNumeric(10);

        PropertiesPage propertiesPage = new PropertiesPage(driver);
        propertiesPage.propertySearch(property);
        soft.assertTrue(propertiesPage.grid().rowContainPropertyName(property),"The row does not contain the searching property");
        int index = 0;
        propertiesPage.grid().selectRow(index);
        PropertyPopup popup = propertiesPage.clickEdit();
        String sentence = " ";
        for(int i=0;i<26;i++) {
            sentence = sentence + string;
        }
        popup.editInputField(sentence);
        popup.clickOK();
        soft.assertTrue(propertiesPage.getErrorMessage().equalsIgnoreCase(SMPMessages.MSG_23),"the errors message is not showing");
        popup.clickCancel();
        soft.assertAll();
    }


    @Test(description = "PROP-20")
    public void verifyPasswordExpiredAlertEvent(){
        SoftAssert soft = new SoftAssert();
        String property ="smp.alert.password.expired.enabled";
        PropertiesPage propertiesPage = new PropertiesPage(driver);
        propertiesPage.sidebar.goToPage(AlertPage.class);
        AlertPage alertPage = new AlertPage(driver);
        int initialAlertCount = alertPage.getTotalNoOfAlert();
        propertiesPage.propertySearch(property);
        soft.assertTrue(propertiesPage.grid().rowContainPropertyName(property),"The row does not contain the searching property");
        int index = 0;
        PropertyRowInfo newRow0 = propertiesPage.grid().getRows().get(0);
        propertiesPage.grid().selectRow(index);
        if(newRow0.getPropertyvalue().equals("true"))

        {
            propertiesPage.grid().selectRow(0);
            PropertyPopup popup = propertiesPage.clickEdit();
            propertiesPage = popup.disableCheckboxOfProperty();
            soft.assertTrue(propertiesPage.isSaveButtonEnabled(),"Save button is disbled");
            propertiesPage.clickSave().confirm();
            soft.assertTrue(propertiesPage.alertArea.getAlertMessage().isError(), "Message listed is success");
            soft.assertTrue(propertiesPage.alertArea.getAlertMessage().getMessage().equalsIgnoreCase(SMPMessages.MSG_18), "Message is not listed is as expected");
        }
       propertiesPage.sidebar.goToPage(AlertPage.class);
       int finalAlertCount = alertPage.getTotalNoOfAlert();
       AlertRowInfo alertRow0 = alertPage.grid().getRows().get(0);
       soft.assertEquals(alertRow0.getAlertType(),"CREDENTIAL_EXPIRED");
       soft.assertEquals (alertRow0.getCredentialType(),"USERNAME_PASSWORD");
       soft.assertEquals(alertRow0.getAlertDate(),"");
       soft.assertAll();
    }

    @Test(description = "PROP-30")
	public void verifyUserSuspendedAlertEvent(){
        String username = Generator.randomAlphaNumeric(10);
        SMPRestClient.createUser(username, "SYSTEM_ADMIN");
		SoftAssert soft = new SoftAssert();
		String property ="smp.alert.user.suspended.enabled";
		PropertiesPage propertiesPage = new PropertiesPage(driver);
		propertiesPage.sidebar.goToPage(AlertPage.class);
		AlertPage alertPage = new AlertPage(driver);
		int initialAlertCount = alertPage.getTotalNoOfAlert();
		propertiesPage.propertySearch(property);
		soft.assertTrue(propertiesPage.grid().rowContainPropertyName(property),"The row does not contain the searching property");
		int index = 0;
		propertiesPage.grid().selectRow(index);
			propertiesPage.grid().selectRow(0);
			PropertyPopup popup = propertiesPage.clickEdit();
			propertiesPage = popup.enableCheckboxOfProperty();
			soft.assertTrue(propertiesPage.isSaveButtonEnabled(),"Save button is disbled");
			propertiesPage.clickSave().confirm();
			soft.assertTrue(propertiesPage.alertArea.getAlertMessage().isError(), "Message listed is success");
			soft.assertTrue(propertiesPage.alertArea.getAlertMessage().getMessage().equalsIgnoreCase(SMPMessages.MSG_18), "Message is not listed is as expected");
        propertiesPage.pageHeader.sandwichMenu.logout();
        SearchPage page = new SearchPage(driver);
        logger.info("Going to login page");
        LoginPage loginPage = page.pageHeader.goToLogin();
        for (int i = 0; i < 5; i++) {
            loginPage.invalidLogin(username, "123456");
            loginPage.waitForXMillis(2000);
        }
        soft.assertTrue(loginPage.isLoginButtonEnable(), "Login Button Is Disabled");
        logger.info(loginPage.alertArea.getAlertMessage().getMessage());
        soft.assertTrue(loginPage.alertArea.getAlertMessage().getMessage().contains(SMPMessages.MSG_22), "Message listed is as expected");

        SMPPage smpPage = genericLoginProcedure("SYS_ADMIN");
        logger.info("Going to Alert page");
        smpPage.sidebar.goToPage(AlertPage.class);
		int finalAlertCount = alertPage.getTotalNoOfAlert();
        soft.assertEquals(initialAlertCount,finalAlertCount-1,"the new alert is not created");
		AlertRowInfo alertRow0 = alertPage.grid().getRows().get(0);
		soft.assertEquals(alertRow0.getAlertType(),"CREDENTIAL_SUSPENDED","The right alert type is not shown in the new alert row");
		soft.assertEquals (alertRow0.getCredentialType(),"USERNAME_PASSWORD","The right credential type is not shown in the new alert row");
        soft.assertEquals (alertRow0.getUser(),username,"The right username is not shown in the new alert row");
		soft.assertEquals(alertRow0.getAlertDate(),"");
		soft.assertAll();
	}


	@Test(description = "PROP-40")
	public void verifyLoginFailureAlertEvent(){
		SoftAssert soft = new SoftAssert();
		String property ="smp.alert.user.login_failure.enabled";
		PropertiesPage propertiesPage = new PropertiesPage(driver);
		propertiesPage.sidebar.goToPage(AlertPage.class);
		AlertPage alertPage = new AlertPage(driver);
		int initialAlertCount = alertPage.getTotalNoOfAlert();
		propertiesPage.propertySearch(property);
		soft.assertTrue(propertiesPage.grid().rowContainPropertyName(property),"The row does not contain the searching property");
		int index = 0;
		propertiesPage.grid().selectRow(index);

			propertiesPage.grid().selectRow(0);
			PropertyPopup popup = propertiesPage.clickEdit();
			propertiesPage = popup.enableCheckboxOfProperty();
			soft.assertTrue(propertiesPage.isSaveButtonEnabled(),"Save button is disbled");
			propertiesPage.clickSave().confirm();
			soft.assertTrue(propertiesPage.alertArea.getAlertMessage().isError(), "Message listed is success");
			soft.assertTrue(propertiesPage.alertArea.getAlertMessage().getMessage().equalsIgnoreCase(SMPMessages.MSG_18), "Message is not listed is as expected");

        propertiesPage.pageHeader.sandwichMenu.logout();
        SearchPage page = new SearchPage(driver);
        logger.info("Going to login page");

        LoginPage loginPage = page.pageHeader.goToLogin();
        loginPage.invalidLogin("system", "12356");

		propertiesPage.sidebar.goToPage(AlertPage.class);
        soft.assertTrue(loginPage.isLoginButtonEnable(), "Login Button Is Disabled");
        logger.info(loginPage.alertArea.getAlertMessage().getMessage());
        soft.assertTrue(loginPage.alertArea.getAlertMessage().getMessage().contains(SMPMessages.MSG_22), "Message listed is as expected");

        SMPPage smpPage = genericLoginProcedure("SYS_ADMIN");
        logger.info("Going to Alert page");
        smpPage.sidebar.goToPage(AlertPage.class);
        int finalAlertCount = alertPage.getTotalNoOfAlert();
        soft.assertEquals(initialAlertCount,finalAlertCount-1,"the new alert is not created");
		AlertRowInfo alertRow0 = alertPage.grid().getRows().get(0);
		soft.assertEquals(alertRow0.getAlertType(),"CREDENTIAL_SUSPENDED");
		soft.assertEquals (alertRow0.getCredentialType(),"USERNAME_PASSWORD");
		soft.assertEquals(alertRow0.getAlertDate(),"");
		soft.assertAll();
	}


	@Test(description = "PROP-50")
	public void verifyAccessTokenImminentExpirationAlertEvent(){
		SoftAssert soft = new SoftAssert();
		String property ="smp.alert.accessToken.imminent_expiration.enabled";
		PropertiesPage propertiesPage = new PropertiesPage(driver);
		propertiesPage.sidebar.goToPage(AlertPage.class);
		AlertPage alertPage = new AlertPage(driver);
		int initialAlertCount = alertPage.getTotalNoOfAlert();
		propertiesPage.propertySearch(property);
		soft.assertTrue(propertiesPage.grid().rowContainPropertyName(property),"The row does not contain the searching property");
		int index = 0;
		PropertyRowInfo newRow0 = propertiesPage.grid().getRows().get(0);
		propertiesPage.grid().selectRow(index);
		if(newRow0.getPropertyvalue().equals("true"))

		{
			propertiesPage.grid().selectRow(0);
			PropertyPopup popup = propertiesPage.clickEdit();
			propertiesPage = popup.disableCheckboxOfProperty();
			soft.assertTrue(propertiesPage.isSaveButtonEnabled(),"Save button is disbled");
			propertiesPage.clickSave().confirm();
			soft.assertTrue(propertiesPage.alertArea.getAlertMessage().isError(), "Message listed is success");
			soft.assertTrue(propertiesPage.alertArea.getAlertMessage().getMessage().equalsIgnoreCase(SMPMessages.MSG_18), "Message is not listed is as expected");
		}
		propertiesPage.sidebar.goToPage(AlertPage.class);
		int finalAlertCount = alertPage.getTotalNoOfAlert();
        soft.assertEquals(initialAlertCount,finalAlertCount-1,"the new alert row is not created");
		AlertRowInfo alertRow0 = alertPage.grid().getRows().get(0);
		soft.assertEquals(alertRow0.getAlertType(),"CREDENTIAL_IMMINENT_EXPIR");
		soft.assertEquals (alertRow0.getCredentialType(),"ACCESS_TOKEN");

		soft.assertAll();
	}

}
