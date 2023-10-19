package pages.systemSettings.domainsPage;

import ddsl.dcomponents.commonComponents.members.MembersComponent;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;

/**
 * Page object Resource tab of Domains page. This contains the locators of the page and the methods for the behaviour of the page
 */
public class MembersTab extends MembersComponent {
    public MembersTab(WebDriver driver) {
        super(driver);
        PageFactory.initElements(new AjaxElementLocatorFactory(driver, data.getWaitTimeShort()), this);
    }
}
