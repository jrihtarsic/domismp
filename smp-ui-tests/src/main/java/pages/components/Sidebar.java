package pages.components;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;
import pages.components.baseComponents.SMPPage;
import pages.components.baseComponents.PageComponent;
import utils.PROPERTIES;

import java.security.PublicKey;
import java.util.List;

public class Sidebar extends PageComponent {

	public Sidebar(WebDriver driver) {
		super(driver);
		PageFactory.initElements( new AjaxElementLocatorFactory(driver, PROPERTIES.TIMEOUT), this);
	}

	@FindBy(tagName = "mat-sidenav")
	private WebElement sideBar;

	private WebElement topLogo;
	private WebElement topLogoText;

	@FindBy(id = "sidebar_search_id")
	private WebElement searchLnk;

	@FindBy(id = "sidebar_edit_id")
	private WebElement editLnk;

	@FindBy(id = "sidebar_domain_id")
	private WebElement domainLnk;

	@FindBy(id = "sidebar_user_id")
	private WebElement userLnk;

	/* Receives the Page object class as parameter and based on the class name it navigates to the apropriate page
	 and returns an instance of that class */
	public <T extends SMPPage> T goToPage(Class<T> expect){
		log.info("Navigating to " + expect.getSimpleName());
		switch (expect.getSimpleName()) {
			case "SearchPage":
				waitForElementToBeClickable(searchLnk).click();
				break;
			case "EditPage":
				waitForElementToBeClickable(editLnk).click();
				break;
			case "DomainPage":
				waitForElementToBeClickable(domainLnk).click();
				break;
			case "UsersPage":
				waitForElementToBeClickable(userLnk).click();
				break;
		}

		return PageFactory.initElements(driver, expect);
	}

	public boolean isSearchLnkVisible(){
		try {
			return searchLnk.isDisplayed() && searchLnk.isEnabled();
		} catch (Exception e) {	}
		return false;
	}
	public boolean isEditLnkVisible(){
		try {
			return editLnk.isDisplayed() && editLnk.isEnabled();
		} catch (Exception e) {	}
		return false;
	}
	public boolean isDomainLnkVisible(){
		try {
			return domainLnk.isDisplayed() && domainLnk.isEnabled();
		} catch (Exception e) {	}
		return false;
	}
	public boolean isUsersLnkVisible(){
		try {
			return userLnk.isDisplayed() && userLnk.isEnabled();
		} catch (Exception e) {	}
		return false;
	}

}
