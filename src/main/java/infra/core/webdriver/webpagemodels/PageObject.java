package infra.core.webdriver.webpagemodels;

import org.openqa.selenium.WebDriver;

public class PageObject extends AbstractPage {
    public PageObject(WebDriver driver) 
    {
        super(driver);
    }

    public String GetPageObjectName()
    {
        return this.getClass().getName();
    }
}
