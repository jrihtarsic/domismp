package pages.administration.editDomainsPage;

import ddsl.dcomponents.commonComponents.subcategoryTab.SubcategoryTabComponent;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;

public class GroupTab extends SubcategoryTabComponent {
    public GroupTab(WebDriver driver) {
        super(driver);
        PageFactory.initElements(new AjaxElementLocatorFactory(driver, data.getTIMEOUT()), this);

    }

    public CreateGroupDetailsDialog clickOnCreateNewGroup() throws Exception {
        create();
        return new CreateGroupDetailsDialog(driver);
    }
}
