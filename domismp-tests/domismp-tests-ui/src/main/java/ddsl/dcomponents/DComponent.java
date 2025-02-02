package ddsl.dcomponents;

import ddsl.dcomponents.mat.MatSelect;
import ddsl.dobjects.*;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import utils.TestRunData;
/**
 * Generic component which gives access of driver, wait and wrappers of elements. This should be inhered by each component class.
 */
public class DComponent {
    public DWait wait;
    protected WebDriver driver;
    protected TestRunData data = TestRunData.getInstance();
    public DComponent(WebDriver driver) {
        this.driver = driver;
        this.wait = new DWait(driver);
    }

    protected DButton weToDButton(WebElement element) {
        return new DButton(driver, element);
    }

    protected DCheckbox weToDChecked(WebElement element) {
        return new DCheckbox(driver, element);
    }

    protected DInput weToDInput(WebElement element) {
        return new DInput(driver, element);
    }

    protected DSelect weToDSelect(WebElement element) {
        return new DSelect(driver, element);
    }

    protected MatSelect weToMatSelect(WebElement element) {
        return new MatSelect(driver, element);
    }

}
