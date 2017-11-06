package infra.core.webdriver.browserbot;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.Point;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import infra.configuration.DefaultTimeout;
import infra.core.webdriver.browserbot.BaseBrowserBot;
import infra.core.webdriver.browserbot.IBrowserBot;

public class FireFoxBot extends BaseBrowserBot implements IBrowserBot{
	
	@Autowired
	@Qualifier("chromebot")
	private DefaultTimeout defaultTimeout;

	public FireFoxBot(WebDriver driver) {
		super(driver);
	}

	@Override
	public DefaultTimeout getDefaultTimeout() {
		return defaultTimeout;
	}

	public void setDefaultTimeout(DefaultTimeout defaultTimeout) {
		this.defaultTimeout = defaultTimeout;
	}

	/// <summary>
	/// Selenium click wrapper by a selector,
	/// waits for multiple conditions, navigates
	/// (scrolls) to element and clicks it
	/// </summary>
	/// <param name="selector"></param>
	/// <param name="timeout"></param>
	/// <returns></returns>
	@Override
	public IBrowserBot click(By selector) {
		WebElement element = GetElement(selector);
		Point originPoint = new Point(0, 0);
		Point elementOriginalPosition = originPoint, elementPreClickPosition = originPoint,
				elementAfterClickPosition = originPoint;

		if (!inIframe)
			elementOriginalPosition = getElementPoistion(element);

		boolean isElementSelectable = IsElementSelectable(selector);
		String pageUrlBeforeClick = driver.getCurrentUrl();
		wait.withTimeout(timeout.isPresent() ? timeout.get() : getDefaultTimeout().click, TimeUnit.SECONDS);
		wait.pollingEvery(50, TimeUnit.MILLISECONDS);
		waitForAjax();
		wait.until(d -> {
			return (Exists(selector) && IsDisplayed(selector));
		});
		String selectorXpath = BaseBrowserBot.Helper.convertByMethodStringToXpath(selector.toString());

		if (!inIframe) {
			elementPreClickPosition = getElementPoistion(element);
			if (elementOriginalPosition != elementPreClickPosition)
				waitForPureJavaScriptAnimation(element);
		}
		try {
			waitForAjax();
			_SeleniumActions.moveToElement(element);
			_SeleniumActions.perform();
			if (isElementSelectable) {
				wait.until(d -> {
					return (Exists(selector) && IsDisplayed(selector));
				});
			} else
				wait.until(ExpectedConditions.elementToBeClickable(element));
			try {
				element.click();
				_SeleniumActions.release().perform();
				waitForAjax();
			} catch (Exception ex) {
				if ((ex instanceof TimeoutException))
					throw new TimeoutException("Timeout exception:" + timeout + " whileperforming Click method");
				try {
					if (driver.getCurrentUrl().contains("GlobalError"))
						throw new Exception("Website GlobalError, see screenshot");
					waitForAjax();
					javascriptExecutor.executeScript("arguments[0].click();", element);
					waitForAjax();
				} catch (Exception e) {
					throw new Exception(
							"Standard Click method & javascriptExecutor click method have failed, check selector. \n"
									+ ex.getStackTrace());
				}
			}
			try {
				if (isElementSelectable && Exists(selector)) {
					if (!element.isSelected())
						_SeleniumActions.click().doubleClick(element).perform();
					wait.until(d -> {
						return element.isSelected();
					});
				}
			} catch (StaleElementReferenceException e) {
				throw new StaleElementReferenceException(
						"Selectable element wasn't able to return avalue whether it was selected or not , element: "
								+ selector);
			}

			waitForAjax();
		} catch (TimeoutException e) {
			throw new TimeoutException(
					"Timeout exception after: " + timeout + " -seconds. \n While trying to click Element: " + selector);
		} catch (Exception ex) {
			// driver couldnt find url , rif (_driver.Url.Contains("Error")) throw
			// newException("WebSite global error");
			throw new RuntimeException(
					"Timeout exception after: " + timeout + " -seconds. \n While trying to click Element: " + selector);
		}
		timeout = Optional.of(60L);
		if (!inIframe) {
			wait.until((d) -> {
				return (boolean) javascriptExecutor
						.executeScript(("return window.jQuery !=undefined && jQuery.active == 0"));
			});
		}
		wait.until(d -> javascriptExecutor.executeScript("return document.readyState").equals("complete"));

		String pageUrlAfterClick = driver.getCurrentUrl();
		if (pageUrlBeforeClick == pageUrlAfterClick && Exists(selector) && !isDropDownMethodCalling.isPresent()) {
			if (!inIframe) {
				if (Enabled(selector) && IsDisplayed(selector) || isElementSelectable && IsDisplayed(selector)) {
					Point pointAfterClick = getElementPoistion(element);
					if (elementAfterClickPosition != elementPreClickPosition && elementAfterClickPosition != null)
						if (Exists(selector))
							waitForPureJavaScriptAnimation(element);
				}
			}
		}
		return this;
	}

}
