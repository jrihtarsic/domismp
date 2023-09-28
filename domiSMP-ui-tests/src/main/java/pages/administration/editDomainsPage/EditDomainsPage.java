package pages.administration.editDomainsPage;

import ddsl.CommonPageWithTabsAndGrid;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EditDomainsPage extends CommonPageWithTabsAndGrid {
    /**
     * Page object for the Edit domains page. This contains the locators of the page and the methods for the behaviour of the page
     */
    private final static Logger LOG = LoggerFactory.getLogger(EditDomainsPage.class);

    public EditDomainsPage(WebDriver driver) {
        super(driver);
        LOG.debug("Loading Edit domains page.");

    }

    public DomainMembersTab getDomainMembersTab() {

        return new DomainMembersTab(driver);
    }

    public GroupTab getGroupTab() {

        return new GroupTab(driver);
    }

}
