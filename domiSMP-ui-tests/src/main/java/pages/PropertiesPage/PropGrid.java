package pages.PropertiesPage;

import ddsl.dcomponents.Grid.BasicGrid;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class PropGrid extends BasicGrid {

    public PropGrid(WebDriver driver, WebElement container) {
        super(driver, container);
    }

    public PropertyPopup selectValue(String propertyValue) {
        this.doubleClickRow(propertyValue);
        return new PropertyPopup(driver);
    }

    ;

}
