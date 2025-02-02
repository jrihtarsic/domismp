package pages.systemSettings.propertiesPage;

import ddsl.dcomponents.DComponent;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * Property popup component used when modifying a property.
 */
public class PropertyPopup extends DComponent {
    private final static Logger LOG = LoggerFactory.getLogger(PropertiesPage.class);
    @FindBy(id = "updatePropertyButton")
    WebElement popupOkBtn;
    @FindBy(css = "mat-dialog-actions button:nth-of-type(2)")
    WebElement popupCancelBtn;
    @FindBy(css = "span.mat-checkbox-inner-container input")
    WebElement propertyCheckbox;
    @FindBy(css = "property-details-dialog input")
    WebElement propertryEditInput;
    @FindBy(css = "property-details-dialog .alert-message-error")
    WebElement errorMessageLbl;


    public PropertyPopup(WebDriver driver) {
        super(driver);
        PageFactory.initElements(driver, this);

    }

    public boolean isOKButtonActive() {
        try {
            return weToDButton(popupOkBtn).isEnabled();

        } catch (Exception e) {
            LOG.error("Element is not visible", e);
            return false;
        }
    }

    public boolean isCancelButtonActive() {
        try {
            return weToDButton(popupCancelBtn).isEnabled();

        } catch (Exception e) {
            LOG.error("Element is not visible", e);
            return false;
        }
    }

    public void clickOK(){
        LOG.info("click OK");
        wait.forElementToBeClickable(popupOkBtn);
        weToDButton(popupOkBtn).click();
        try {
            if (!errorMessageLbl.isDisplayed()) {
                wait.forElementToBeGone(popupOkBtn);
            }
        } catch (RuntimeException e) {
            LOG.debug("No error are present");
        }
    }
    public PropertiesPage clickCancel() {
        LOG.info("click cancel");
        wait.forElementToBeClickable(popupCancelBtn);
        popupCancelBtn.click();
        wait.forElementToBeGone(popupCancelBtn);
        return new PropertiesPage(driver);
    }

    public PropertiesPage enableCheckboxOfProperty() {
        boolean bool = propertyCheckbox.isSelected();

        if (!bool) {
            propertyCheckbox.click();
            popupOkBtn.click();
        } else {
            popupCancelBtn.click();
        }
        return new PropertiesPage(driver);
    }

    public PropertiesPage disableCheckboxOfProperty() {
        boolean bool = propertyCheckbox.isSelected();
        if (bool) {
            JavascriptExecutor executor = (JavascriptExecutor) driver;
            executor.executeScript("arguments[0].click();", propertyCheckbox);
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                LOG.error(String.valueOf(e));
            }
            popupOkBtn.click();
        } else {
            popupCancelBtn.click();
        }
        return new PropertiesPage(driver);

    }

    public void editInputField(String string) {
        propertryEditInput.clear();
        propertryEditInput.sendKeys(string);
    }

    public String getErrorMessage() {
        if (!errorMessageLbl.isDisplayed()) {
            return null;
        }
        return errorMessageLbl.getText();
    }
}
