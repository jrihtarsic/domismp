package pages;

import ddsl.dcomponents.DComponent;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;
import rest.models.DomainModel;

import java.util.List;

public class SmlPage extends DComponent {

    @FindBy(css = "body>pre")
    private List<WebElement> dnsRecords;

    public SmlPage(WebDriver driver) {
        super(driver);
        PageFactory.initElements(new AjaxElementLocatorFactory(driver, data.getTIMEOUT()), this);

    }

    public boolean isDomainRegistered(DomainModel domainModel) {
        for (WebElement element : dnsRecords) {
            String dnsRecord = domainModel.getSmlSmpId();
            String elementRecords = element.getText();

            if (elementRecords.contains(dnsRecord)) {
                return true;
            }
            return false;
        }
        return false;
    }
}
