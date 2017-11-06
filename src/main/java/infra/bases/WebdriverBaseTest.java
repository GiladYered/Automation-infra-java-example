package infra.bases;

import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.testng.annotations.BeforeSuite;

import com.jayway.awaitility.core.ConditionFactory;

import infra.core.webdriver.WebDriverFactory;
import infra.core.webdriver.browserbot.BaseBrowserBot;
import infra.core.webdriver.enums.EBrowser;


public class WebdriverBaseTest extends MasterBaseTest {
	
	protected ThreadLocal<WebDriver> webDriver = new ThreadLocal<>();
	
	
//	@BeforeSuite
//	public void setup() {
//		initWebDriver(EBrowser.CHROME);
//		AbstractApplicationContext  ctx = new ClassPathXmlApplicationContext("timeouts.xml");
//		ctx.registerShutdownHook();
//		BaseBrowserBot.browserBotFactory(webDriver.get());
//	}
	

}
