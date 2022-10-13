package pages.alert;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;
import pages.components.ConfirmationDialog;
import pages.components.baseComponents.PaginationControls;
import pages.components.baseComponents.SMPPage;
import pages.properties.PropertiesGrid;
import utils.PROPERTIES;


public class AlertPage extends SMPPage{

        public AlertPage(WebDriver driver) {
            super(driver);
            this.pageHeader.waitForTitleToBe("Alerts");
            PageFactory.initElements( new AjaxElementLocatorFactory(driver, PROPERTIES.TIMEOUT), this);
        }

    @FindBy(id = "searchTable")
    private WebElement alertTableContainer;

    @FindBy(css = "datatable-footer > div  div.page-count.ng-star-inserted")
    private WebElement roewNo;

    public int getTotalNoOfAlert(){

        String rowNoInformation = roewNo.getText();
        int length = rowNoInformation.length();
        int index = length-1;
        int rowno =rowNoInformation.charAt(index);
        return rowno;

    }

    public AlertGrid grid() {
        return new AlertGrid(driver, alertTableContainer);
    }


    public PaginationControls pagination = new PaginationControls(driver);

}
