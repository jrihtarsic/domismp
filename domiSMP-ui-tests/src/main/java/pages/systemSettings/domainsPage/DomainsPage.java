package pages.systemSettings.domainsPage;

import ddsl.CommonPageWithTabsAndGrid;
import ddsl.dobjects.DButton;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Page object for the Users page. This contains the locators of the page and the methods for the behaviour of the page
 */
public class DomainsPage extends CommonPageWithTabsAndGrid {
    private final static Logger LOG = LoggerFactory.getLogger(DomainsPage.class);
    @FindBy(css = "smp-warning-panel span")
    private WebElement warningLabel;
    public DomainsPage(WebDriver driver) {
        super(driver);
        LOG.debug("Loading Domains page.");
    }

    public DButton getCreateDomainBtn() {
        return new DButton(driver, addBtn);
    }

    public ResourceTab getResourceTab() {

        return new ResourceTab(driver);
    }

    public DomainTab getDomainTab() {

        return new DomainTab(driver);
    }

    public SMLIntegrationTab getSMLIntegrationTab() {

        return new SMLIntegrationTab(driver);
    }

    public MembersTab getMembersTab() {

        return new MembersTab(driver);
    }

    public String getDomainWarningMessage() {
        return warningLabel.getText();
    }


}
