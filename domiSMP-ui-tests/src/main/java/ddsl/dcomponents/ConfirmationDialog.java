package ddsl.dcomponents;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfirmationDialog extends DComponent {
    private final static Logger LOG = LoggerFactory.getLogger(ConfirmationDialog.class);

    @FindBy(id = "yesbuttondialog_id")
    private WebElement yesBtn;
    @SuppressWarnings("SpellCheckingInspection")
    @FindBy(id = "nobuttondialog_id")
    private WebElement noBtn;

    public ConfirmationDialog(WebDriver driver) {
        super(driver);
        PageFactory.initElements(new AjaxElementLocatorFactory(driver, data.getTIMEOUT()), this);
    }

    public void confirm() {
        LOG.info("dialog .. confirm");
        wait.forElementToBeClickable(yesBtn);
        yesBtn.click();
        wait.forElementToBeClickable(yesBtn);
    }

    public void cancel() {
        LOG.info("dialog .. cancel");
        wait.forElementToBeClickable(noBtn);
        noBtn.click();
        wait.forElementToBeGone(noBtn);
    }
}
