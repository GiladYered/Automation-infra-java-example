package infra.core.webdriver;


import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeDriverService;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.events.EventFiringWebDriver;

import infra.core.webdriver.enums.EBrowser;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

/**
 * Webdriver factory class that configures and returns either a local or remote
 * driver instance as required At the moment, Marionette is kept separately from
 * Firefox to allow the choice of where to run the tests This is because
 * Marionette doesn't yet support all the functionality. For Firefox v46 and
 * above, you need to use Marionette
 */

public class WebDriverFactory   {


	private DesiredCapabilities _capabilities;
	private WebDriver _driver;
	private EBrowser _browser;
	
	public EBrowser getBrowserType() {
		return _browser;
	}
	

	/**
	 * Creates a local driver, as it takes the LocalDriver Configuration object
	 * 
	 * @param configuration
	 *            the local driver configuration
	 * @return instance of the local driver as configured
	 */
	public WebDriver createLocalDriver(EBrowser browser) {
		this._browser = browser;
		switch (browser) {
			case CHROME:createChromeDriver(); break;
			case IE:createInternetExplorerDriver(); break;
			case FIREFOX:createFireFoxDriver(); break;
			case EDGE:createEdgeDriver(); break;
			// defaults to chrome
			default:createChromeDriver(); break;
		}
		return _driver;
	}
	
	
	protected void initWebDriver(EBrowser browser) {
		_driver = new WebDriverFactory().createLocalDriver(browser);
		_driver.manage().timeouts().pageLoadTimeout(20, TimeUnit.SECONDS);
		_driver.manage().window().maximize();
	}

	protected void webDriverTearDown() {
		if (_driver != null)
			_driver.quit();
		//Stream.of(EBrowser.values()).forEach(browser -> killProcess(browser.getProcessName()));
	}
	

	/**
	 * Creates a remote driver, as it takes the RemoteDriver configuration
	 * object
	 * 
	 * @param configuration
	 *            the remote driver configuration
	 * @return instance of the remote driver as configured
	 */

	/**
	 * Sets the relevant driver properties for the local driver instance
	 * 
	 * @param propKey
	 *            the driver property to set
	 * @param relativeToUserPath
	 *            the relative location of the exe file
	 */
	private static void setDriverPropertyIfRequired(String propKey, String relativeToUserPath) {

		if (!System.getProperties().containsKey(propKey)) {
			String currentDir = "C:\\WebDrivers\\";
		
			String driverLocation = currentDir + relativeToUserPath;
			File driverExecutable = new File(driverLocation);

			try {
				if (driverExecutable.exists()) {
					System.setProperty(propKey, driverLocation);
				}
			} catch (Exception e) {
				System.err.println("The driver does not exist at that location: " + driverLocation);
			}
		}
	}

	/**
	 * Build a Uri for your GRID Hub instance
	 * 
	 * @param remoteServer
	 *            The hostname or IP address of your GRID instance, include the
	 *            http://
	 * @param remoteServerPort
	 *            Port of your GRID Hub instance
	 * @return the URL as a string
	 */
	public static String buildRemoteServer(String remoteServer, int remoteServerPort) {
		return String.format("%s/%d/wd/hub", remoteServer, remoteServerPort);
	}

	private WebDriver createChromeDriver() {
		// Chrome driver requires the webdriver property to be set
		// Sets property if not supplied
		setDriverPropertyIfRequired("webdriver.chrome.driver", "chromedriver.exe");

		ChromeOptions options = new ChromeOptions();
		options.addArguments("disable-plugins");
		options.addArguments("disable-extensions");
		options.addArguments("test-type");

		
		_driver = new ChromeDriver(options);
		return _driver;
	}

	private WebDriver createFireFoxDriver() {
		// Firefox driver requires the webdriver property to be set
		setDriverPropertyIfRequired("webdriver.gecko.driver", "geckodriver.exe");
		_driver = new FirefoxDriver();
		//TODO check if Ben want?
		//final EventFiringWebDriver wrap = new EventFiringWebDriver(_driver);
		// register listener to webdriver
		//wrap.register(new CustomWebDriverEventListener()); // implements WebDriverEventListener
		//return wrap;
		return _driver;
	}

	private WebDriver createInternetExplorerDriver() {
		// IE driver requires the webdriver property to be set
		// Sets property if not supplied
		DesiredCapabilities capabilities = DesiredCapabilities.internetExplorer();
		capabilities.setCapability(InternetExplorerDriver.IGNORE_ZOOM_SETTING, true);
		setDriverPropertyIfRequired("webdriver.ie.driver", "IEDriverServer.exe");
		_driver = new InternetExplorerDriver(capabilities);
		return _driver;
	}

	private WebDriver createEdgeDriver() {
		// Edge Driver requires you install the Microsoft Webdriver Server, then
		// set the system property
		EdgeDriverService service = new EdgeDriverService.Builder()
				.usingDriverExecutable(
						new File("C:\\ensilo-performance\\drivers\\MicrosoftWebDriver.exe"))
				.usingAnyFreePort().build();
		try {
			service.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
		_driver = new EdgeDriver(service, DesiredCapabilities.edge());// edgeOptions);

		return _driver;
	}
}
