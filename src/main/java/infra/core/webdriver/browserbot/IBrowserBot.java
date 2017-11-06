package infra.core.webdriver.browserbot;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;

import org.openqa.selenium.By;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public interface IBrowserBot {
	
	IBrowserBot waitForPageToLoad(WebDriver driver);

	IBrowserBot Refresh();

	IBrowserBot WaitUntil(Callable<Boolean> Condition);

	IBrowserBot waitForjQueryActivity();

	IBrowserBot waitForAjax();

	IBrowserBot waitForPureJavaScriptAnimation(WebElement element);

	IBrowserBot waitForElementToBeVisible(By selector);

	boolean WaitUntil(boolean Condition);

	boolean Exists(By selector);

	boolean IsDisplayed(By selector);

	boolean Enabled(By selector);

	boolean AttributeExists(By selector, String attributeName);

	boolean IsElementSelectable(By selector);

	IBrowserBot AssertTextIsDisplayed(By selector, String text) throws Exception;

	WebElement GetElement(By selector);

	List<WebElement> GetElements(By elementsSelector);

	IBrowserBot click(By selector);// , boolean isDropDownMethodCalling = false);

	IBrowserBot clickElement(WebElement element);

	IBrowserBot SendKeys(By TextBoxSelector, String input);

	IBrowserBot SendKeysToHiddenElement(By TextBoxSelector, String input);

	IBrowserBot SelectDropDownByText(By DropDowSelector, String option);

	IBrowserBot SelectDropDownByTextFromHiddenElement(By DropDownSelector, String option);

	IBrowserBot AssertDropDownOptions(By dropDownBtnSelector, List<String> options);

	IBrowserBot UpLoadFile(By attachBtnselector, String filePath);

	String GetText(By selector);

	String GetText(WebElement element);

	String GetAttribute(By selector, String attributeName);

	String GetInnerHtml(By selector);

	Point getElementPoistion(WebElement element);

	IBrowserBot WaitForLoadingWindow();

	IBrowserBot AssertBetween(Object objLeft, Object objRight, Optional<Boolean> keepTestAlive);

	IBrowserBot SwitchToFrame(By selector);

	IBrowserBot SwitchToFrame(int frame);

	IBrowserBot SwitchToDefaultFrame();

	IBrowserBot setTimeout(Long timeout);
}
