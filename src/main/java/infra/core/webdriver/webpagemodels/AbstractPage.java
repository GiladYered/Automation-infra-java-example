package infra.core.webdriver.webpagemodels;

import java.util.Optional;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import infra.configuration.DefaultTimeout;
import infra.core.webdriver.WebDriverFactory;
import infra.core.webdriver.browserbot.BaseBrowserBot;
import infra.core.webdriver.browserbot.IBrowserBot;
import infra.core.webdriver.enums.EBrowser;

public class AbstractPage {

    protected WebDriver driver;
    protected IBrowserBot browserbot;
    private Optional<String> _url;
    private EBrowser browser ;
    
    public AbstractPage(WebDriver driver)
    {
    	
    	driver = null;// new WebDriverFactory(driver);
        this.driver = driver;
        this.browserbot = BaseBrowserBot.browserBotFactory(driver,browser);
       
        if (_url.isPresent()) browserbot.waitForPageToLoad(driver);
        else navigate(_url.get());
       
    }
    
    public AbstractPage setPageUrl(String url) {
    	this._url = Optional.of(url);
    	return this;
    }


    public String getPageTitle()
    {
        return driver.getTitle();
    }

    public void navigate(String url)
    {
        try
        {
            driver.navigate().to(url);
        }
        catch (Exception e) { throw new RuntimeException("Was unable to execute, driver.Navigate() , driver name = " + driver.toString()); }
        browserbot.waitForPageToLoad(driver);
    }
}
