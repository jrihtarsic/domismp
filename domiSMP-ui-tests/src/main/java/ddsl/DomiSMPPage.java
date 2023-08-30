package ddsl;


import ddsl.dcomponents.AlertComponent;
import ddsl.dcomponents.BreadcrumpComponent;
import ddsl.dcomponents.DComponent;
import ddsl.dcomponents.SideNavigationComponent;
import ddsl.dobjects.DButton;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pages.LoginPage;

public class DomiSMPPage extends DComponent {

    /**
     * Page object for the common components from Domismp like navigation, right menu. This contains the locators of the page and the methods for the behaviour of the page
     */
    private final static Logger LOG = LoggerFactory.getLogger(DomiSMPPage.class);
    @FindBy(css = "cdk-overlay-backdrop cdk-overlay-dark-backdrop cdk-overlay-backdrop-showing")
    protected WebElement overlay;
    @FindBy(id = "login_id")
    private WebElement loginBtnTop;
    @FindBy(id = "settingsmenu_id")
    private WebElement rightMenuBtn;
    @FindBy(id = "logout_id")
    private WebElement logoutMenuBtn;
    @FindBy(css = "#okbuttondialog_id ")
    private WebElement dialogOKbutton;


    public DomiSMPPage(WebDriver driver) {
        super(driver);
        PageFactory.initElements(new AjaxElementLocatorFactory(driver, data.getTIMEOUT()), this);
        waitForPageToLoaded();
    }

    public SideNavigationComponent getSidebar() {
        return new SideNavigationComponent(driver);
    }

    public BreadcrumpComponent getBreadcrump() {
        return new BreadcrumpComponent(driver);
    }

    public LoginPage goToLoginPage() {
        loginBtnTop.click();
        return new LoginPage(driver);
    }

    public void logout() {
        wait.waitforOverlayToGone();
        rightMenuBtn.click();
        logoutMenuBtn.click();

    }

    public void refreshPage() {
        driver.navigate().refresh();
        waitForPageToLoaded();
    }

    public AlertComponent getAlertArea() {
        return new AlertComponent(driver);
    }

    public DButton getExpiredDialoginbutton() {
        return weToDButton(dialogOKbutton);
    }

    public boolean isExpiredDialoginbuttonEnabled() {
        try {
            return dialogOKbutton.isEnabled();
        } catch (Exception e) {
            LOG.info("Expiration poup not found", e);
            return false;
        }
    }
    public void waitForPageToLoaded() {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("return document.readyState").toString().equals("complete");
    }

}
