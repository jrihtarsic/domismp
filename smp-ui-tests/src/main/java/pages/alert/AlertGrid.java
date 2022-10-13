package pages.alert;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import pages.components.grid.BasicGrid;

import java.util.ArrayList;
import java.util.List;


public class AlertGrid extends BasicGrid {
    public AlertGrid(WebDriver driver, WebElement container) {
        super(driver, container);
    }

    private By cellSelector = By.tagName("datatable-body-cell");

    public List<AlertRowInfo> getRows() {
        List<AlertRowInfo> rowInfos = new ArrayList<>();

        for (WebElement gridRow : gridRows) {
            List<WebElement> cells = gridRow.findElements(By.tagName("datatable-body-cell"));
            AlertRowInfo rowInfo = new AlertRowInfo();
            rowInfo.setAlertDate(cells.get(0).getText().trim());
            rowInfo.setCredentialType(cells.get(1).getText().trim());
            rowInfo.setUser(cells.get(2).getText().trim());
            rowInfo.setAlertType(cells.get(3).getText().trim());
            rowInfo.setAlertStatus(cells.get(4).getText().trim());
            rowInfo.setStatusDesc(cells.get(5).getText().trim());
            rowInfo.setAlertLevel(cells.get(6).getText().trim());
            rowInfos.add(rowInfo);
        }
        return rowInfos;
    }

}
