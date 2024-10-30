package ddsl.dcomponents;

import ddsl.DomiSMPPage;
import ddsl.enums.Pages;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pages.administration.ReviewTasksPage;
import pages.administration.editDomainsPage.EditDomainsPage;
import pages.administration.editGroupsPage.EditGroupsPage;
import pages.administration.editResourcesPage.EditResourcePage;
import pages.search.ResourcesPage;
import pages.systemSettings.TruststorePage;
import pages.systemSettings.UsersPage;
import pages.systemSettings.domainsPage.DomainsPage;
import pages.systemSettings.keyStorePage.KeystorePage;
import pages.systemSettings.propertiesPage.PropertiesPage;
import pages.userSettings.ProfilePage;

import java.util.Objects;
/**
 * Navigation object to navigate through application.
 */
public class SideNavigationComponent extends DomiSMPPage {
    private final static Logger LOG = LoggerFactory.getLogger(SideNavigationComponent.class);
    @FindBy(id = "window-sidenav-panel")
    public WebElement sideBar;

    //	--------------------Search-------------------------
    @FindBy(id = "search-resourcesButton")
    private WebElement resourcesLnk;

    @FindBy(id = "search-toolsButton")
    private WebElement searchExpandLnk;
    //	----------------------------------------------------

    //	--------------Administration---------------------------
    @FindBy(id = "edit-domainButton")
    private WebElement editDomainsLnk;

    @FindBy(id = "edit-groupButton")
    private WebElement editGroupsLnk;

    @FindBy(id = "edit-resourceButton")
    private WebElement editResourcesLnk;

    @FindBy(id = "review-tasksButton")
    private WebElement reviewTasksLnk;

    @FindBy(id = "editButton")
    private WebElement administrationExpand;
    //	----------------------------------------------------

    //	--------------System Settings ---------------------------
    @FindBy(id = "system-admin-userButton")
    private WebElement usersLnk;

    @FindBy(id = "system-admin-domainButton")
    private WebElement domainsLnk;

    @FindBy(id = "system-admin-keystoreButton")
    private WebElement keystoreLnk;
    @FindBy(id = "system-admin-truststoreButton")
    private WebElement truststoreLnk;

    @FindBy(id = "system-admin-extensionButton")
    private WebElement extensionsLnk;

    @FindBy(id = "system-admin-propertiesButton")
    private WebElement propertiesLnk;

    @FindBy(id = "system-admin-alertButton")
    private WebElement alersLnk;

    @FindBy(id = "system-settingsButton")
    private WebElement systemSettingsExpand;
    //	----------------------------------------------------

    //	--------------User Settings---------------------------
    @FindBy(id = "user-data-profileButton")
    private WebElement profileLnk;

    @FindBy(id = "user-data-access-tokenButton")
    private WebElement accessTokensLnk;

    @FindBy(id = "user-data-certificatesButton")
    private WebElement certificatesLnk;

    @FindBy(id = "user-dataButton")
    private WebElement userSettingsExpand;
    //	----------------------------------------------------

    public SideNavigationComponent(WebDriver driver) {
        super(driver);
        PageFactory.initElements(new AjaxElementLocatorFactory(driver, 1), this);
        waitForPageToLoaded();
    }

    private MenuNavigation getNavigationLinks(Pages pages) {
        if (Objects.requireNonNull(pages) == Pages.USER_SETTINGS_PROFILE) {
            return new MenuNavigation(userSettingsExpand, profileLnk);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public <T> T navigateTo(Pages page) {
        LOG.debug("Get link to " + page.name());
        // DomiSMP behaviour. Button is not expanded if already focused and not expanded - issue when re-login
        // with this we make sure the starting point from Search
        openSubmenu(searchExpandLnk, null);

        if (page == Pages.SEARCH_RESOURCES) {
            openSubmenu(searchExpandLnk, resourcesLnk);
            return (T) new ResourcesPage(driver);
        }
        if (page == Pages.ADMINISTRATION_EDIT_DOMAINS) {
            openSubmenu(administrationExpand, editDomainsLnk);
            return (T) new EditDomainsPage(driver);
        }
        if (page == Pages.ADMINISTRATION_EDIT_GROUPS) {
            openSubmenu(administrationExpand, editGroupsLnk);
            return (T) new EditGroupsPage(driver);
        }
        if (page == Pages.ADMINISTRATION_EDIT_RESOURCES) {
            openSubmenu(administrationExpand, editResourcesLnk);
            return (T) new EditResourcePage(driver);
        }
        if (page == Pages.ADMINISTRATION_REVIEW_TASKS) {
            openSubmenu(administrationExpand, reviewTasksLnk);
            return (T) new ReviewTasksPage(driver);
        }
        if (page == Pages.SYSTEM_SETTINGS_USERS) {
            openSubmenu(systemSettingsExpand, usersLnk);
            return (T) new UsersPage(driver);
        }
        if (page == Pages.SYSTEM_SETTINGS_DOMAINS) {
            openSubmenu(systemSettingsExpand, domainsLnk);
            return (T) new DomainsPage(driver);
        }

        if (page == Pages.SYSTEM_SETTINGS_KEYSTORE) {
            openSubmenu(systemSettingsExpand, keystoreLnk);
            return (T) new KeystorePage(driver);
        }

        if (page == Pages.SYSTEM_SETTINGS_TRUSTSTORE) {
            openSubmenu(systemSettingsExpand, truststoreLnk);
            return (T) new TruststorePage(driver);
        }
        //            case SYSTEM_SETTINGS_EXTENSIONS:
        //                expandSection(systemSettingsExpand);
        //                return new DLink(driver, extensionsLnk);
        if (page == Pages.SYSTEM_SETTINGS_PROPERTIES) {
            openSubmenu(systemSettingsExpand, propertiesLnk);
            return (T) new PropertiesPage(driver);
        }

        //            case SYSTEM_SETTINGS_ALERS:
        //                expandSection(systemSettingsExpand);
        //                return new DLink(driver, alersLnk);
        if (page == Pages.USER_SETTINGS_PROFILE) {
            openSubmenu(userSettingsExpand, profileLnk);
            return (T) new ProfilePage(driver);
//            case USER_SETTINGS_ACCESS_TOKEN:
//                //expandSection(userSettingsExpand);
//                //accessTokensLnk.click();
//                return new ProfilePage(driver);
//            case USER_SETTINGS_CERTIFICATES:
//                expandSection(userSettingsExpand);
//                return new DLink(driver, certificatesLnk);
        }
        return null;
    }

    public Boolean isMenuAvailable(Pages page) {
        MenuNavigation navigationLinks = getNavigationLinks(page);
        try {
            if (navigationLinks.menuLink.isEnabled()) {
                navigationLinks.menuLink.click();
                return navigationLinks.submenuLink.isEnabled();
            }
            return false;
        } catch (NoSuchElementException e) {
            LOG.error("No menu element found");
            return false;
        }
    }

    private void openSubmenu(WebElement menuBtn, WebElement submenuBtn) {

        if (!menuBtn.getAttribute("class").contains("cdk-focused")) {
            // Driver Issue:  is not clickable at point (105, 356). Other element would receive the click:
            Actions actions = new Actions(driver);
            actions.moveToElement(menuBtn);
            actions.perform();

            menuBtn.click();
        }
        if (submenuBtn == null){
            return;
        }
        submenuBtn.click();
        if (submenuBtn.getText().contains(getBreadcrump().getCurrentPage())) {
            LOG.info("Current page is " + getBreadcrump().getCurrentPage());

        } else {
            LOG.error("Current page is not as expected. EXPECTED: " + submenuBtn.getText() + "but ACTUAL PAGE: " + getBreadcrump().getCurrentPage());
            throw new RuntimeException();
        }
    }
    public static class MenuNavigation {
        WebElement menuLink;
        WebElement submenuLink;

        public MenuNavigation(WebElement menuLink, WebElement submenuLink) {
            this.menuLink = menuLink;
            this.submenuLink = submenuLink;
        }
    }
}



