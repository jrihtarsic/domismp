package ddsl.dobjects;

import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * Generic element object used to have access to element actions.
 */
public class DObject {
    protected final Logger log = LoggerFactory.getLogger(this.getClass());
    public WebElement element;
    protected WebDriver driver;
    protected DWait wait;
    public DObject(WebDriver driver, WebElement element) {
        wait = new DWait(driver);
        this.driver = driver;
        this.element = element;
    }

    public boolean isPresent() {
        try {
            wait.forElementToBe(element);
            scrollIntoView();
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public boolean isEnabled() throws ElementNotInteractableException {
        try {
            if (isPresent()) {
                wait.forElementToBeEnabled(element);
                return element.isEnabled();
            }
        }
        catch (ElementNotInteractableException e){
            throw new ElementNotInteractableException("Element not enabled: "+ e);
        }

        return false;
    }

    public boolean isDisabled() throws Exception {
        if (isPresent()) {
            wait.forElementToBeDisabled(element);
            return !element.isEnabled();
        }
        throw new Exception();
    }

    public boolean isVisible() throws Exception {
        if (isPresent()) {
            wait.forElementToBeEnabled(element);
            return element.isDisplayed();
        }
        throw new Exception();
    }

    public String getText() {
        if (!isPresent()) {
            throw new ElementNotInteractableException("Element: ["+ element.getAccessibleName() +"] not present");
        }
        scrollIntoView();
        String text = ((JavascriptExecutor) driver).executeScript("return arguments[0].innerText;", element).toString();
        return text.trim();
    }

    public void scrollIntoView() {
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView();", element);
    }

    public void click()  {
        if (isEnabled()) {
            wait.forElementToBeClickable(element).click();
        } else {
            throw new ElementNotInteractableException(element.getAccessibleName() + "Not enabled");
        }
    }

    public String getAttribute(String attributeName) {
        if (isPresent()) {
            String attr = element.getAttribute(attributeName);
            if (attr == null) {
                log.debug("Attribute " + attributeName + " not found");
                return null;
            }
            return attr.trim();
        }
        throw new RuntimeException();
    }


}
