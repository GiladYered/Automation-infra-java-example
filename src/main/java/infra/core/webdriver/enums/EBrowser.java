package infra.core.webdriver.enums;

import java.util.stream.Stream;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

public enum EBrowser {

	CHROME("chromedriver.exe"),
	FIREFOX("geckodriver.exe"),
	EDGE("MicrosoftWebDriver.exe"),
	IE("IEDriverServer.exe");
	
	private String processName;

	EBrowser(String processName) {
		this.processName = processName;
	}
	
	public String getProcessName(){
		return processName;
	}
	
	public static String[] toArray() {
		return (String[]) Stream.of(EBrowser.values()).map(browser -> browser.name()).toArray();
	}

}