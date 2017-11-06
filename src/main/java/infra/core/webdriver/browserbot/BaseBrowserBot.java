package infra.core.webdriver.browserbot;

import org.openqa.selenium.Point;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.By;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;
import org.testng.Assert;

import infra.configuration.DefaultTimeout;
import infra.core.webdriver.enums.EBrowser;

/// <summary>
/// 
/// </summary>
@Component
public abstract class BaseBrowserBot implements IBrowserBot {
	protected WebDriver driver;
	protected Actions _SeleniumActions;

	// protected DefaultTimeout baseDefaultTimeout;
	
	public JavascriptExecutor javascriptExecutor;
	protected WebDriverWait wait;
	private  EBrowser browserType;
	protected Point elementPosition;
	protected static boolean inIframe = false;
	protected Optional<Boolean> isDropDownMethodCalling;
	protected Optional<By> dropDownOptionsSelector;
	protected Optional<Long> timeout;

	protected BaseBrowserBot(WebDriver driver) {
		// System.out.println(getDefaultTimeout().click);
		// System.out.println(baseDefaultTimeout.click);

		this.driver = driver;
		this._SeleniumActions = new Actions(driver);
		// this.getDefaultTimeout() = null;// Config.get<DefaultTimeout>();
		this.javascriptExecutor = (JavascriptExecutor) driver;
		this.wait = new WebDriverWait(driver, 50L);// TimeUnit.FromSeconds(getDefaultTimeout().Wait));

	}

	public abstract DefaultTimeout getDefaultTimeout();

	public IBrowserBot setTimeout(Long timeout) {
		this.timeout = Optional.of(timeout);
		return this;
	}

	/// <summary>
	/// BaseBrowserBot is an Abstract Factory design pattern implementation
	/// Which encapsulates to each browser its own bot API
	/// Where methods that are common to all browsers arent overriden
	/// </summary>
	/// <param name="driver"></param>
	/// <returns></returns>
	public static IBrowserBot browserBotFactory(WebDriver driver, EBrowser browser) {
		IBrowserBot browserBot = null;
		
		Capabilities cap = ((RemoteWebDriver) driver).getCapabilities();
		String browserName = cap.getBrowserName().toLowerCase();
		System.out.println(browserName);
		String v = cap.getVersion().toString();
		
		switch (browser) {
			case CHROME: { browserBot = new ChromeBot(driver); }break;
			case FIREFOX: { browserBot = new FireFoxBot(driver); }break;
			case IE: { browserBot = new InternetExplorerBot(driver); }break;
			case EDGE: { browserBot = new InternetExplorerBot(driver); }break;
		}
		return browserBot;
	}

	public IBrowserBot waitForPageToLoad(WebDriver driver) {
		wait.withTimeout(timeout.isPresent() ? timeout.get() : getDefaultTimeout().pageLoad, TimeUnit.SECONDS);
		wait.pollingEvery(50, TimeUnit.MILLISECONDS);
		wait.until(d -> {
			try {
				if (d.getCurrentUrl().contains("GlobalError"))
					throw new RuntimeException("Website Global Error, ScreenShot available");
				return javascriptExecutor.executeScript("return document.readyState").equals("complete");
			} catch (TimeoutException e) {
				throw new TimeoutException("Page Load timeout exception, page: " + this.getClass().getName()
						+ " has failed to load after " + getDefaultTimeout().pageLoad + " seconds");
			}
		});
		return this;
	}

	/// <summary>
	/// Browserbot refresh, waits until
	/// page is fully loaded.
	/// </summary>
	/// <returns>this</returns>
	public IBrowserBot Refresh() {
		driver.navigate().refresh();
		wait.withTimeout(timeout.isPresent() ? timeout.get() : getDefaultTimeout().refresh, TimeUnit.SECONDS);
		wait.pollingEvery(50, TimeUnit.MILLISECONDS);
		wait.until(d -> javascriptExecutor.executeScript("return document.readyState").equals("complete"));
		return this;
	}

	/// <summary>
	/// BaseBrowserBot Wait until a boolean
	/// condition is met, or timeout
	/// interval is reached
	/// </summary>
	/// <param name="Condition"></param>
	/// <param name="timeout"></param>
	/// <param name="PollingInterval"></param>
	/// <returns></returns>

	public boolean WaitUntil(boolean Condition) {
		wait.withTimeout(timeout.isPresent() ? timeout.get() : getDefaultTimeout().wait, TimeUnit.SECONDS);
		wait.pollingEvery(50, TimeUnit.MILLISECONDS);
		return wait.until(d -> {
			return Condition;
		});
	}

	/// <summary>
	/// BaseBrowserBot Wait (Overload) until a
	/// boolean function condition is met,
	/// or timeout interval is reached
	/// Usage: Browserbot.WaitUntil(() => { return "boolean function";});
	/// </summary>
	/// <param name="Condition"></param>
	/// <param name="timeout"></param>
	/// <param name="PollingInterval"></param>
	/// <returns></returns>
	public IBrowserBot WaitUntil(Callable<Boolean> Condition) {
		wait.withTimeout(timeout.isPresent() ? timeout.get() : getDefaultTimeout().wait, TimeUnit.SECONDS);
		wait.pollingEvery(50, TimeUnit.MILLISECONDS);
		wait.until(d -> {
			return Condition;
		});
		return this;
	}

	/// <summary>
	/// private method, wait until jquery
	/// animation activity is finished
	/// </summary>
	/// <param name="timeout"></param>
	/// <returns></returns>
	public IBrowserBot waitForjQueryActivity() {
		if (!inIframe) {
			wait.withTimeout(timeout.isPresent() ? timeout.get() : getDefaultTimeout().wait, TimeUnit.SECONDS);
			wait.pollingEvery(50, TimeUnit.MILLISECONDS);
			wait.until((d) -> {
				return (boolean) javascriptExecutor.executeScript("return jQuery.active == 0");
			});
		}
		return this;
	}

	/// <summary>
	/// TODO..
	/// </summary>
	/// <param name="timeout"></param>
	/// <returns></returns>
	public IBrowserBot waitForAjax() {
		if (!inIframe) {
			wait.withTimeout(timeout.isPresent() ? timeout.get() : getDefaultTimeout().wait, TimeUnit.SECONDS);
			wait.pollingEvery(50, TimeUnit.MILLISECONDS);
			wait.until((d) -> {
				try {
					return (boolean) javascriptExecutor
							.executeScript("return window.jQuery != undefined && jQuery.active==0")
							&& (boolean) javascriptExecutor
									.executeScript("return $('.spinner').is(':visible') == false");
				} catch (Exception e) {
					return true;
				}
			});
		}
		return this;
	}

	/// <summary>
	/// TODO...
	/// </summary>
	/// <param name="element"></param>
	/// <param name="timeout"></param>
	/// <returns></returns>
	public IBrowserBot waitForPureJavaScriptAnimation(WebElement element) {
		if (!inIframe) {
			boolean elementIsStatic = false;
			wait.withTimeout(timeout.isPresent() ? timeout.get() : getDefaultTimeout().wait, TimeUnit.SECONDS);
			wait.pollingEvery(50, TimeUnit.MILLISECONDS);
			wait.until((d) -> {
				try {
					if (element.getLocation().equals(null))
						return true;
					Point elementFirstTempPosition = getElementPoistion(element);
					_SeleniumActions.moveToElement(element).perform();
					Point elementSecondTempPosition = getElementPoistion(element);
					if (elementFirstTempPosition == elementSecondTempPosition)
						return true;
					else
						return false;
				} catch (Exception e) {
					return true;
				}
			});
		}
		return this;
	}

	/// <summary>
	/// BaseBrowserBot wait for element to exist
	/// in the DOM and to be displayed on the
	/// page, until timeout interval is reached
	/// </summary>
	/// <param name="selector"></param>
	/// <param name="timeout"></param>
	/// <returns></returns>
	public IBrowserBot waitForElementToBeVisible(By selector) {
		wait.withTimeout(timeout.isPresent() ? timeout.get() : getDefaultTimeout().wait, TimeUnit.SECONDS);
		wait.pollingEvery(50, TimeUnit.MILLISECONDS);
		wait.until(ExpectedConditions.visibilityOfElementLocated(selector));
		waitForAjax();
		return this;
	}

	/// <summary>
	/// BaseBrowserBot Exists, a boolean method
	/// that checkes if element is located
	/// in the DOM.
	/// </summary>
	/// <param name="selector"></param>
	/// <returns>bool</returns>
	public boolean Exists(By selector) {
		try {
			driver.findElement(selector);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/// <summary>
	/// BaseBrowserBot IsDisplayed a boolean method
	/// that checkes if element is displayed
	/// on the current web page.
	/// </summary>
	/// <param name="selector"></param>
	/// <returns>bool</returns>
	public boolean IsDisplayed(By selector) {
		try {
			return driver.findElement(selector).isDisplayed();
		} catch (Exception e) {
			return false;
		}
	}

	/// <summary>
	/// Enabled, bollean method that returns true
	/// if element is enabled, false if disabled.
	/// </summary>
	/// <param name="selector"></param>
	/// <returns>bool</returns>
	public boolean Enabled(By selector) {
		try {
			return driver.findElement(selector).isEnabled();
		} catch (Exception e) {
			return false;
		}

	}

	public IBrowserBot SwitchToFrame(By selector) {
		driver.switchTo().frame(driver.findElement(selector));
		inIframe = true;
		return this;
	}

	public IBrowserBot SwitchToFrame(int frame) {
		driver.switchTo().frame(frame);
		inIframe = true;
		return this;
	}

	public IBrowserBot SwitchToDefaultFrame() {
		driver.switchTo().defaultContent();
		inIframe = false;
		return this;
	}

	public boolean AttributeExists(By selector, String attributeName) {
		try {
			return driver.findElement(selector).getAttribute(attributeName) != null;
		} catch (Exception e) {
			return false;
		}
	}

	public boolean IsElementSelectable(By selector) {
		boolean isSelectable = false;
		if (AttributeExists(selector, "type"))
			if (GetAttribute(selector, "type").equals("checkbox") || GetAttribute(selector, "type").equals("radio"))
				isSelectable = true;
		return isSelectable;
	}

	public IBrowserBot AssertTextIsDisplayed(By selector, String text) throws Exception {
		try {
			Assert.assertEquals(GetText(selector), text);
		} catch (Exception e) {
			throw new Exception("Text in the given selector :" + selector + " is : " + GetText(selector)
					+ " \n Expected text : " + text);
		}
		try {
			Assert.assertTrue(IsDisplayed(selector));
		} catch (Exception e) {
			throw new Exception(
					"Selector " + selector + " containing the text :" + GetText(selector) + " isn't displayed");
		}
		return this;
	}

	/// <summary>
	/// Selenium GetElement wrapper, waits for
	/// multiple conditions to be met until
	/// element is found.
	/// </summary>
	/// <param name="selector"></param>
	/// <returns>IWebElement</returns>
	public WebElement GetElement(By selector) {
		wait.withTimeout(timeout.isPresent() ? timeout.get() : getDefaultTimeout().getElement, TimeUnit.SECONDS);
		wait.pollingEvery(50, TimeUnit.MILLISECONDS);
		waitForAjax();
		wait.withMessage(
				"BrowserBot.Exists timeout reached: " + getDefaultTimeout().getElement + " - seconds.\nAt page:"
						+ driver.getCurrentUrl() + ".\nCouldn't find Element in the DOM: " + selector);
		wait.until(d -> {
			return this.Exists(selector) == true;
		});
		wait.withMessage("BrowserBot.IsDisplayed timeout reached: " + getDefaultTimeout().getElement
				+ " - seconds.\nAt page:" + driver.getCurrentUrl() + ".\nCouldn't detec: " + selector);
		wait.until(d -> {
			return this.IsDisplayed(selector) == true;
		});
		wait.withMessage("Timeout exception after: " + getDefaultTimeout().getElement
				+ " - seconds. \n While trying to find element: " + selector);
		return (WebElement) wait.until((d) -> {
			try {
				return d.findElement(selector);
			} catch (StaleElementReferenceException se) {
				throw new StaleElementReferenceException("Reference to element: " + selector + " is no longer valid");
			} catch (NoSuchElementException nse) {
				throw new NoSuchElementException("Can't find element: " + selector);
			}
		});
	}

	/// <summary>
	/// Selenium GetElements wrapper, waits for
	/// multiple conditions to be met until
	/// elements are found.
	/// </summary>
	/// <param name="selector"></param>
	/// <returns>IList<IWebElement></returns>
	public List<WebElement> GetElements(By elementsSelector) {
		wait.withTimeout(timeout.isPresent() ? timeout.get() : getDefaultTimeout().getElement, TimeUnit.SECONDS);
		wait.pollingEvery(50, TimeUnit.MILLISECONDS);
		waitForjQueryActivity();
		wait.until(d -> {
			return this.Exists(elementsSelector) == true;
		});
		return (List<WebElement>) wait.until(d -> {
			try {
				return d.findElements(elementsSelector);
			} catch (StaleElementReferenceException e) {
				throw new StaleElementReferenceException(
						"Reference to element: " + elementsSelector + " is no longer valid");
			} catch (NoSuchElementException e) {
				throw new NoSuchElementException("Can't find element: " + elementsSelector);
			} catch (TimeoutException e) {
				throw new TimeoutException("Timeout exception after: " + getDefaultTimeout().getElement
						+ " - seconds. \n While trying to find element: " + elementsSelector);
			}
		});
	}

	/// <summary>
	/// Selenium click wrapper by a selector,
	/// waits for multiple conditions, navigates
	/// (scrolls) to element and clicks it
	/// </summary>
	/// <param name="selector"></param>
	/// <param name="timeout"></param>
	/// <returns></returns>
	public IBrowserBot click(By selector) {
		WebElement element = GetElement(selector);
		Point elementOriginalPosition = getElementPoistion(element), elementPreClickPosition = new Point(0, 0),
				elementAfterClickPosition = new Point(0, 0);

		String pageUrlBeforeClick = driver.getCurrentUrl();
		wait.withTimeout(timeout.isPresent() ? timeout.get() : getDefaultTimeout().click, TimeUnit.SECONDS);
		wait.pollingEvery(50, TimeUnit.MILLISECONDS);
		waitForAjax();
		wait.until(d -> {// ExpectedConditions.ElementToBeClickable isn't stabled.
			return (Exists(selector) && IsDisplayed(selector) && Enabled(selector));
		});

		String selectorXpath = BaseBrowserBot.Helper.convertByMethodStringToXpath(selector.toString());

		elementPreClickPosition = getElementPoistion(element);
		if (elementOriginalPosition.equals(elementPreClickPosition))
			waitForPureJavaScriptAnimation(element);
		try {
			wait.withTimeout(timeout.isPresent() ? timeout.get() : getDefaultTimeout().click, TimeUnit.SECONDS);
			clickElement(element);
		} catch (TimeoutException e) {
			throw new TimeoutException("Timeout exception after: " + getDefaultTimeout().click
					+ " - seconds. \n While trying to click Element: " + selector);
		} catch (Exception ex) {
			// driver couldnt find url , rif (_driver.Url.Contains("Error")) throw new
			// Exception("WebSite global error");
			throw new RuntimeException("Timeout exception after: " + getDefaultTimeout().click
					+ " - seconds. \n While trying to click Element: " + selector);
		}
		wait.withTimeout(timeout.isPresent() ? timeout.get() : getDefaultTimeout().click, TimeUnit.SECONDS);
		if (!inIframe) {
			wait.until((d) -> {
				return (boolean) javascriptExecutor
						.executeScript(("return window.jQuery != undefined && jQuery.active == 0"));
			});
		}
		wait.until(d -> javascriptExecutor.executeScript("return document.readyState").equals("complete"));

		String pageUrlAfterClick = driver.getCurrentUrl();
		if (pageUrlBeforeClick == pageUrlAfterClick && Exists(selector) && !isDropDownMethodCalling.get()) {
			if (Enabled(selector) && IsDisplayed(selector)) {
				Point pointAfterClick = getElementPoistion(element);
				if (elementAfterClickPosition != elementPreClickPosition && elementAfterClickPosition != null)
					waitForPureJavaScriptAnimation(element);
			}
		}
		return this;
	}

	/// <summary>
	/// GOING TO BE DELETED AND MERGED TO CLICK.
	/// </summary>
	/// <param name="element"></param>
	/// <param name="timeout"></param>
	/// <returns></returns>
	public IBrowserBot clickElement(WebElement element) {
		wait.withTimeout(timeout.isPresent() ? timeout.get() : getDefaultTimeout().clickElement, TimeUnit.SECONDS);
		wait.pollingEvery(50, TimeUnit.MILLISECONDS);
		waitForAjax();
		_SeleniumActions.moveToElement(element);
		_SeleniumActions.perform();
		wait.until(ExpectedConditions.elementToBeClickable(element));
		try {
			// _SeleniumActions.MoveToElement(element).Click().Build().Perform();
				
			element.click();
			_SeleniumActions.release().perform();
			waitForAjax();
			// _javascriptExecutor.ExecuteScript("arguments[0].click();", element);
		} catch (Exception ex) {
			if ((ex instanceof TimeoutException))
				throw new TimeoutException(
						"Timeout exception:" + getDefaultTimeout().clickElement + " while performing Click method");
			try {
				if (driver.getCurrentUrl().contains("GlobalError"))
					throw new Exception("Website Global Error, see screenshot");
				waitForAjax();
				javascriptExecutor.executeScript("arguments[0].click();", element);
				waitForAjax();
			} catch (Exception e) {
				throw new RuntimeException(
						"Standard Click method & javascriptExecutor click method have failed, check selector. \n"
								+ ex.getStackTrace());
			}
		}
		waitForAjax();
		return this;
	}

	/// <summary>
	/// TODO
	/// </summary>
	/// <param name="TextBoxSelector"></param>
	/// <param name="input"></param>
	/// <param name="timeout"></param>
	/// <returns></returns>
	public IBrowserBot SendKeys(By TextBoxSelector, String input) {
		wait.withTimeout(timeout.isPresent() ? timeout.get() : getDefaultTimeout().clickElement, TimeUnit.SECONDS);
		wait.pollingEvery(50, TimeUnit.MILLISECONDS);
		wait.until(ExpectedConditions.elementToBeClickable(TextBoxSelector));
		WebElement element = GetElement(TextBoxSelector);
		clickElement(element);
		element.clear();
		wait.until(ExpectedConditions.textToBePresentInElementValue(element, ""));
		_SeleniumActions.moveToElement(element);
		_SeleniumActions.perform();
		element.sendKeys(input);
		wait.until(ExpectedConditions.textToBePresentInElementValue(element, input));
		return this;
	}

	public IBrowserBot SendKeysToHiddenElement(By TextBoxSelector, String input) {
		wait.withTimeout(timeout.isPresent() ? timeout.get() : getDefaultTimeout().type, TimeUnit.SECONDS);
		wait.pollingEvery(50, TimeUnit.MILLISECONDS);
		WebElement element = GetElement(TextBoxSelector);
		clickElement(element).clickElement(element);
		wait.until(d -> {
			return Exists(By.xpath(BaseBrowserBot.Helper
					.convertByMethodStringToXpath(TextBoxSelector.toString() + "//input[last()]")));
		});
		element = GetElement((By.xpath(
				BaseBrowserBot.Helper.convertByMethodStringToXpath(TextBoxSelector.toString() + "//input[last()]"))));
		element.clear();
		wait.until(ExpectedConditions.textToBePresentInElementValue(element, ""));
		_SeleniumActions.moveToElement(element);
		_SeleniumActions.perform();
		element.sendKeys(input);
		wait.until(ExpectedConditions.textToBePresentInElementValue(element, input));
		element.sendKeys(Keys.TAB);
		_SeleniumActions.sendKeys(Keys.ESCAPE).perform();
		waitForAjax();
		return this;
	}

	/// <summary>
	/// TODO
	/// </summary>
	/// <param name="DropDowSelector"></param>
	/// <param name="option"></param>
	/// <param name="timeout"></param>
	/// <returns></returns>
	public IBrowserBot SelectDropDownByText(By DropDowSelector, String option) {
		wait.withTimeout(timeout.isPresent() ? timeout.get() : getDefaultTimeout().wait, TimeUnit.SECONDS);
		wait.pollingEvery(50, TimeUnit.MILLISECONDS);
		waitForjQueryActivity();
		click(DropDowSelector).setTimeout(getDefaultTimeout().wait);
		String BtnDDSelectorXpath = BaseBrowserBot.Helper.convertByMethodStringToXpath(DropDowSelector.toString());

		String DropDownAriaOwnsAttributeValue = "";

		if (Exists(By.xpath(BtnDDSelectorXpath + "/../preceding-sibling::*")))
			DropDownAriaOwnsAttributeValue = GetAttribute(By.xpath(BtnDDSelectorXpath + "/../preceding-sibling::*"),
					"aria-owns");

		By optSelector = BaseBrowserBot.Helper.DropDownOptSelectorGenerator(DropDowSelector, option, driver,
				Optional.of(DropDownAriaOwnsAttributeValue));
		try {
			waitForAjax();
			wait.until(d -> {
				return IsDisplayed(optSelector) == true;
			});
			waitForAjax();
		} catch (Exception e) {
		}
		if (!IsDisplayed(optSelector))
			try {
				waitForAjax();
				javascriptExecutor.executeScript("arguments[0].click();", DropDowSelector);
				waitForAjax();
			} catch (Exception e) {
				throw new RuntimeException(
						"Standard Click method & javascriptExecutor click method have failed, Check selector: "
								+ DropDowSelector);
			}
		click(optSelector);
		waitForAjax();
		return this;
	}

	/// <summary>
	/// TODO
	/// </summary>
	/// <param name="DropDowSelector"></param>
	/// <param name="option"></param>
	/// <param name="timeout"></param>
	/// <returns></returns>
	public IBrowserBot SelectDropDownByTextFromHiddenElement(By DropDownSelector, String option) {
		wait.withTimeout(timeout.isPresent() ? timeout.get() : getDefaultTimeout().clickDropDown, TimeUnit.SECONDS);
		wait.pollingEvery(50, TimeUnit.MILLISECONDS);
		waitForjQueryActivity();
		wait.until(d -> {
			return IsDisplayed(DropDownSelector);
		});
		click(DropDownSelector);
		By optSelector = BaseBrowserBot.Helper.DropDownOptSelectorGenerator(DropDownSelector, option, driver, null);
		WebElement optElement = wait.until(d -> {
			try {
				return d.findElement(optSelector);
			} catch (NoSuchElementException e) {
				throw new NoSuchElementException("Can't find element: " + optSelector);
			}
		});
		click(DropDownSelector);
		try {
			waitForAjax();
			wait.until(d -> {
				return IsDisplayed(optSelector) == true;
			});
			waitForAjax();
		} catch (Exception e) {
		}
		if (!IsDisplayed(optSelector))
			try {
				waitForAjax();
				javascriptExecutor.executeScript("arguments[0].click();", DropDownSelector);
				waitForAjax();
			} catch (Exception e) {
				throw new RuntimeException(
						"Standard Click method & javascriptExecutor click method have failed, Check selector: "
								+ DropDownSelector);
			}
		clickElement(optElement);
		_SeleniumActions.sendKeys(Keys.TAB);
		_SeleniumActions.sendKeys(Keys.ESCAPE).perform();
		waitForAjax();
		return this;
	}

	private IBrowserBot dropDownMethodCalling() {
		this.isDropDownMethodCalling = Optional.of(true);
		return this;
	}

	public IBrowserBot setDropdownOptionSelector(By selector) {
		this.dropDownOptionsSelector = Optional.of(selector);
		return this;
	}

	/// <summary>
	/// Assert drop down options: A method that
	/// opens the dropdown menu runs over all drop
	/// down options, and verifies that every drop
	/// down option exists in the DOM and is visualy
	/// displayed , function will fail the test if 1
	/// or more drop down options are missing.
	/// </summary>
	/// <param name="dropDownBtnSelector"></param>
	/// <param name="dropDownOptionsSelector"></param>
	/// <param name="options"></param>
	/// <param name="timeout"></param>
	/// <returns></returns>
	public IBrowserBot AssertDropDownOptions(By dropDownBtnSelector, List<String> options) {
		wait.withTimeout(timeout.isPresent() ? timeout.get() : getDefaultTimeout().clickDropDown, TimeUnit.SECONDS);
		wait.pollingEvery(50, TimeUnit.MILLISECONDS);
		waitForjQueryActivity();
		click(dropDownBtnSelector);
		By optSelector = BaseBrowserBot.Helper.DropDownOptSelectorGenerator(dropDownBtnSelector, options.get(0), driver,
				null);
		String selectorsGroupXPath = BaseBrowserBot.Helper
				.convertByMethodStringToXpath(optSelector.toString() + "/../*");
		int collectionCount;
		try {
			collectionCount = GetElements(By.xpath(selectorsGroupXPath)).size();
		} catch (Exception e) {
			if (dropDownOptionsSelector.isPresent()) {
				String dropDownOptionsXpath = BaseBrowserBot.Helper
						.convertByMethodStringToXpath(dropDownOptionsSelector.toString());
				collectionCount = GetElements(By.xpath(dropDownOptionsXpath)).size();
			} else
				throw new StaleElementReferenceException(
						"Couldn't Automatically generate all drop down options XPath, please insert their Xpath manualy via the 3rd optional argument");
		}
		for (int i = 1; i <= collectionCount; i++) {
			final int tempIndex = i;
			wait.until(d -> {
				return Exists((By.xpath(selectorsGroupXPath + "[" + tempIndex + "]"))) == true;
			});
			wait.until(d -> {
				if (!IsDisplayed((By.xpath(selectorsGroupXPath + "[" + tempIndex + "]"))))
					click(dropDownBtnSelector);
				return IsDisplayed((By.xpath(selectorsGroupXPath + "[" + tempIndex + "]"))) == true;
			});
			final int finalCollectionCount = collectionCount;
			Assert.assertTrue(wait.until(d -> {
				try {
					if (!(options.size() == finalCollectionCount))
						throw new RuntimeException(
								"Drop Down assert failed, the length of the given list:" + options.size()
										+ " doesnt mach the existing list in the DOM: " + finalCollectionCount);

					return options.get(tempIndex - 1)
							.equals(d.findElement(By.xpath(selectorsGroupXPath + "[" + tempIndex + "]"))
									.getAttribute("textContent"));
				} catch (StaleElementReferenceException e) {
					throw new StaleElementReferenceException(
							"Reference to elements: " + selectorsGroupXPath + " is no longer valid");
				} catch (NoSuchElementException e) {
					throw new NoSuchElementException("Can't find elements: " + selectorsGroupXPath);
				} catch (TimeoutException e) {
					throw new TimeoutException("Timeout exception after: " + getDefaultTimeout().clickDropDown
							+ " - seconds. \n While trying to find elements: " + selectorsGroupXPath);
				}
			}));
		}
		;
		click(dropDownBtnSelector);
		return this;
	}

	/// <summary>
	/// TODO
	/// </summary>
	/// <param name="selector"></param>
	/// <param name="filePath"></param>
	/// <param name="timeout"></param>
	/// <returns></returns>
	public IBrowserBot UpLoadFile(final By attachBtnselector, String filePath) {
		if (!new File(filePath).exists())
			throw new RuntimeException(
					"The File you're trying to upload doesn't exists, check the file path.\n  filePath: " + filePath);
		wait.withTimeout(timeout.isPresent() ? timeout.get() : getDefaultTimeout().clickDropDown, TimeUnit.SECONDS);
		wait.pollingEvery(50, TimeUnit.MILLISECONDS);

		wait.until(d -> {
			return Exists(attachBtnselector);
		});
		_SeleniumActions.moveToElement(driver.findElement(attachBtnselector));
		_SeleniumActions.perform();
		String hiddenInput = BaseBrowserBot.Helper.convertByMethodStringToXpath(attachBtnselector.toString())
				+ "/following-sibling::*//input[last()]";
		By newAttachBtnSelector = null;
		if (Exists(By.xpath(hiddenInput)))
			newAttachBtnSelector = By.xpath(hiddenInput);

		driver.findElement(newAttachBtnSelector).submit();
		driver.findElement(newAttachBtnSelector).sendKeys(filePath);
		waitForjQueryActivity();
		final By finalAttachBtnSlctor = newAttachBtnSelector;
		wait.until(d -> {
			return this.Exists(finalAttachBtnSlctor) == true;
		});
		WebElement hiddenElement = (WebElement) wait.until(d -> {
			try {
				return d.findElement(By.xpath("//ul[contains(@class,'k-upload-files')]/li/span[1]"));
			} catch (StaleElementReferenceException e) {
				throw new StaleElementReferenceException(
						"Reference to element: " + attachBtnselector + " is no longer valid");
			} catch (NoSuchElementException e) {
				throw new NoSuchElementException("Can't find element: " + attachBtnselector);
			} catch (TimeoutException e) {
				throw new TimeoutException("Timeout exception after: " + getDefaultTimeout().clickDropDown
						+ " - seconds. \n While trying to find element: " + attachBtnselector);
			}
		});
		waitForAjax();
		wait.until(d -> (wait.until(q -> {
			try {
				return (String) javascriptExecutor.executeScript("return arguments[0].innerHTML;", hiddenElement);
			} catch (StaleElementReferenceException e) {
				throw new StaleElementReferenceException(
						"element //ul[contains(@class,'k-upload-files')]/li/span[1] , didn't appear in the DOM.");
			}
		}).equals("uploaded")));
		waitForAjax();
		return this;
	}

	/// <summary>
	/// TODO
	/// </summary>
	/// <param name="selector"></param>
	/// <param name="timeout"></param>
	/// <returns></returns>
	public String GetText(By selector) {
		String result = "";
		wait.withTimeout(timeout.isPresent() ? timeout.get() : getDefaultTimeout().getOuterHtml, TimeUnit.SECONDS);
		wait.pollingEvery(50, TimeUnit.MILLISECONDS);
		WebElement element = GetElement(selector);
		wait.ignoring(StaleElementReferenceException.class);
		wait.until(ExpectedConditions.textToBePresentInElementLocated(selector, result = wait.until(d -> {
			try {
				return element.getText();
			} catch (StaleElementReferenceException e) {
				throw new StaleElementReferenceException("element " + element + ", did not appear in the DOM.");
			}
		})));
		return result;
	}

	/// <summary>
	/// Overload
	/// </summary>
	/// <param name="element"></param>
	/// <param name="timeout"></param>
	/// <returns></returns>
	public String GetText(WebElement element) {
		try {
			return element.getText();
		} catch (Exception e) {
		}
		String result = "";
		wait.withTimeout(timeout.isPresent() ? timeout.get() : getDefaultTimeout().getOuterHtml, TimeUnit.SECONDS);
		wait.pollingEvery(50, TimeUnit.MILLISECONDS);
		wait.ignoring(StaleElementReferenceException.class);
		wait.until(ExpectedConditions.textToBePresentInElementValue(element, result = wait.until(d -> {
			try {
				return element.getText();
			} catch (StaleElementReferenceException e) {
				throw new StaleElementReferenceException(
						"Element " + element + " could not provide inner html text, stale element exception");
			}
		})));
		return result;
	}

	/// <summary>
	/// TODO
	/// </summary>
	/// <param name="selector"></param>
	/// <param name="attributeName"></param>
	/// <param name="timeout"></param>
	/// <returns></returns>
	public String GetAttribute(By selector, String attributeName) {
		wait.withTimeout(timeout.isPresent() ? timeout.get() : getDefaultTimeout().getAttribute, TimeUnit.SECONDS);
		wait.pollingEvery(50, TimeUnit.MILLISECONDS);
		WebElement element = GetElement(selector);
		wait.ignoring(StaleElementReferenceException.class);
		wait.until(d -> {
			try {
				return element.getAttribute(attributeName) != null;
			} catch (StaleElementReferenceException e) {
				throw new StaleElementReferenceException(
						"Element " + element + " could not provide outer html attribute, stale element exception");
			}
		});
		return element.getAttribute(attributeName);
	}

	/// <summary>
	/// TODO
	/// </summary>
	/// <returns></returns>
	public String GetPageTitle() {
		return driver.getTitle();
		// return (String)_javascriptExecutor.ExecuteScript("return document.title");
	}

	/// <summary>
	/// TODO..
	/// </summary>
	/// <param name="element"></param>
	/// <returns></returns>
	public Point getElementPoistion(WebElement element) {
		wait.withTimeout(timeout.isPresent() ? timeout.get() : getDefaultTimeout().getAttribute, TimeUnit.SECONDS);
		wait.pollingEvery(50, TimeUnit.MILLISECONDS);
		wait.until(d -> {
			try {
				return !element.getLocation().equals(new Point(0, 0));
			} catch (Exception e) {
				return false;
			} // (Exception ex) { throw new Exception("getElementPoisition exception, can't
				// fetch element poistion \n"+ ex.StackTrace); }
		});
		return element.getLocation();
	}

	/// <summary>
	/// TODO
	/// </summary>
	/// <param name="selector"></param>
	/// <param name="timeout"></param>
	/// <returns></returns>
	public String GetInnerHtml(By selector) {
		WebElement element = GetElement(selector);
		return (String) javascriptExecutor.executeScript("return arguments[0].innerHTML;", element);
	}

	public IBrowserBot WaitForLoadingWindow() {
		wait.withTimeout(timeout.isPresent() ? timeout.get() : getDefaultTimeout().getAttribute, TimeUnit.SECONDS);
		wait.pollingEvery(50, TimeUnit.MILLISECONDS);
		wait.ignoring(StaleElementReferenceException.class);
		wait.until(d -> {
			return IsDisplayed(By.xpath("//div[@class='k-animation-container']"));
		});
		wait.until(d -> {
			return !IsDisplayed(By.xpath("//div[@class='k-animation-container']"));
		});
		return this;
	}

	public IBrowserBot AssertBetween(Object objLeft, Object objRight, Optional<Boolean> keepTestAlive) {
		if (!(objLeft.toString().equals(objRight.toString())))
			keepTestAlive.ifPresent(flag -> {
				if (flag == true)
					throw new RuntimeException(
							"Object left: [" + objLeft + "], is not equal to Object right[" + objRight + "]");
			});
		else
			System.err.println("TEST ASSERTION FAILURE: Object left: [" + objLeft + "], is not equal to Object right ["
					+ objRight + "]");
		return this;
	}

	protected static class Helper {
		private WebDriver driver;

		public Helper(WebDriver driver, EBrowser browser) {
			this.driver = driver;
		}

		/// <summary>
		/// Workaround for not being able to extract the
		/// full xpath value from any By method.
		/// For example once a selector is passed to an
		/// actionbot function By.Id("value");
		/// The ToString() result is -> "(\n) By.Id: value".
		/// By's API doesn't contain any nescessary
		/// function for this issue..
		/// </summary>
		/// <param name="selector"></param>
		/// <returns></returns>
		public static String convertByMethodStringToXpath(String selector) {
			String fullXpathResult = "";
			Matcher match = Pattern.compile("^\\s*([^:]*):[^\\S](.*)").matcher(selector);

			// GroupCollection regexGroup = Regex.Match(selector.ToString(),
			// @"^\s*([^:]*):[^\S](.*)").Groups;
			String byMethod = match.group(1);// regexGroup[1].Value;
			String selectorContent = match.group(2);// regexGroup[2].Value;
			switch (byMethod) {
			case ("By.XPath"):
				fullXpathResult = selectorContent;
				break;
			case ("By.Id"):
				fullXpathResult = "//*[@id='" + selectorContent + "']";
				break;
			case ("By.Name"):
				fullXpathResult = "//*[@name='" + selectorContent + "']";
				break;
			case ("By.ClassName"):
				fullXpathResult = "//*[@class='" + selectorContent + "']";
				break;
			}
			return fullXpathResult;
		}

		public static By DropDownOptSelectorGenerator(By DropDowSelector, String option, WebDriver driver,
				Optional<String> ariaOwnsAttributeVal) {
			IBrowserBot Actionbot = BaseBrowserBot.browserBotFactory(driver,null);
			String dropDownXpath = BaseBrowserBot.Helper.convertByMethodStringToXpath(DropDowSelector.toString());
			By result = null;
			// option = option.Replace(@"\", "");
			if (Actionbot
					.Exists(result = By.xpath("//*[@id='" + ariaOwnsAttributeVal + "']//li[text()='" + option + "']")))
				return result;
			if (Actionbot.Exists(result = By.xpath("//li[text()='" + option + "']")))
				return result;
			if (Actionbot.Exists(result = By.xpath(dropDownXpath + "/option[text()='" + option + "']")))
				return result;
			// if (Actionbot.Exists(result = By.XPath("//li[contains(text(),'" +
			// String.join(" ", option.split(' ').Skip(1)) + "')]"))) return result;
			// if (Actionbot.Exists(result = By.XPath("//li[contains(text(),'" +
			// String.join(" ", option.Split(' ').Reverse().Skip(1).Reverse()) + "')]")))
			// return result;
			// if (Actionbot.Exists(result = By.XPath("//li[contains(text(),'" +
			// String.join(" ", option.Split(' ').Skip(1).Reverse().Skip(1).Reverse()) +
			// "')]"))) return result;
			if (Actionbot
					.Exists(result = By.xpath("//*[@id='" + ariaOwnsAttributeVal + "']//li[text()='" + option + "']")))
				return result;
			if (Actionbot.AttributeExists(DropDowSelector, "aria-controls"))
				if (Actionbot.Exists(result = By
						.xpath("//ul[@id='" + Actionbot.GetAttribute(By.xpath(dropDownXpath), "aria-controls")
								+ "']//li[contains(text(),'" + option + "')]")))
					return result;
			throw new RuntimeException("Couldn't find the dropdown option selector by text " + option);
		}

		/// <summary>
		/// Every hebrew String with apostrophies comes along with a
		/// backslash, which has to be removed.
		/// The following approaches aren't working on hebrew text
		/// Regex.Replace(option, @"[\\]", "");
		/// String.Replace(@"\","")
		/// String.Replace(@"\",String.Empty);
		/// String.Replace("\\","");
		///
		/// So this workaround was created
		/// </summary>
		/// <param name="input"></param>
		/// <returns></returns>
		private static String removeBackSlashFromHebrewString(String input) {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < input.length(); i++)
				if (input.indexOf(i) != '\\')
					sb.append(input.indexOf(i));
			return sb.toString();
		}
	}

	// if (_debugLog) Debug.WriteLine("Element {0} was clicked, with timeout: {1}",
	// element, wait.Timeout.TotalSeconds);

	// public BaseBrowserBot WaitforSpinner(Optional<Double> timeout)
	// {
	// WebDriverWait wait = new WebDriverWait(_driver,
	// TimeSpan.FromSeconds(timeout));
	// wait.Timeout = timeout > 0 ? TimeSpan.FromSeconds(timeout) :
	// TimeSpan.FromSeconds(getDefaultTimeout().Default.WaitForAngular);
	// wait.PollingInterval = TimeSpan.FromMilliseconds(100);
	// wait.IgnoreExceptionTypes(typeof(NoSuchElementException));
	// wait.Until<bool>((d) => {
	// return Convert.ToBoolean(
	// _javascriptExecutor.ExecuteAsyncScript(
	// @"var callback = arguments[arguments.length - 1];
	// var el = document.querySelector('html');
	// if (!window.angular) {
	// callback('False')
	// }
	// if (angular.getTestability) {
	// angular.getTestability(el).whenStable(function(){callback('True')});
	// } else {
	// if (!angular.element(el).injector()) {
	// callback('False')
	// }
	// var browser = angular.element(el).injector().get('$browser');
	// browser.notifyWhenNoOutstandingRequests(function(){callback('True')});
	// }"));
	// });
	// return this;
	// }

	// public BaseBrowserBot WaitForExpCondition(By selector, ExpectedConditions
	// condition, Optional<Double> timeout)
	// {
	// WebDriverWait wait = new WebDriverWait(_driver,
	// TimeSpan.FromSeconds(timeout));
	// wait.Timeout = timeout > 0 ? TimeSpan.FromSeconds(timeout) :
	// TimeSpan.FromSeconds(getDefaultTimeout().Default.WaitForExpCondition);
	// wait.PollingInterval = TimeSpan.FromMilliseconds(100);
	// wait.Until<ExpectedConditions>((d) => {
	// return condition;
	// });
	// return this;
	// }

	// public BaseBrowserBot Wait(By Selector, Predicate<bool> predicate,
	// Optional<Double> timeout)
	// {
	// WebDriverWait wait = new WebDriverWait(_driver,
	// TimeSpan.FromSeconds(timeout));
	// wait.Timeout = timeout > 0 ? TimeSpan.FromSeconds(timeout) :
	// TimeSpan.FromSeconds(getDefaultTimeout().Default.Wait);
	// wait.PollingInterval = TimeSpan.FromMilliseconds(100);
	// wait.Until<Predicate<bool>>((d) => {
	// return predicate;
	// });
	// return this;
	// }

	// wait.Until<bool>(d => {
	// return d.FindElement(selector).Text != "";
	// });
	// wait.Until(ExpectedConditions.TextToBePresentInElementLocated(selector,
	// wait.Until<String>(d => {
	// return result = d.FindElement(selector).Text;
	// })
	// ));
	// return result;
	// }

	// public String GetOuterHtml(IWebElement element, Optional<Double> timeout)
	// {
	// TimeSpan timeOutFormat = timeout > 0 ? TimeSpan.FromSeconds(timeout) :
	// TimeSpan.FromSeconds(getDefaultTimeout().Default.GetOuterHtml);
	// return (String)_javascriptExecutor.ExecuteScript("return
	// arguments[0].innerHTML;", element);
	// }

	// public BaseBrowserBot WaitForPageToLoad(String url, Optional<Double> timeout)
	// {
	// WebDriverWait wait = new WebDriverWait(_driver,
	// TimeSpan.FromSeconds(timeout));
	// wait.Timeout = timeout > 0 ? TimeSpan.FromSeconds(timeout) :
	// TimeSpan.FromSeconds(getDefaultTimeout().Default.WaitForPageToLoad);
	// wait.PollingInterval = TimeSpan.FromMilliseconds(100);
	// wait.Until<bool>((d) => {
	// return _driver.Url == url;
	// });
	// return this;
	// }

	//// TODO
	// public String GetText(By selector, [Optional] TimeSpan timeout)
	// {
	// ///The text in the element, probably equals to "0.0000" at the time you try
	//// to read it.
	// ///Before trying to extract it, wait for it 5 miliseconds:
	// WebDriverWait wait = new WebDriverWait(_driver, new TimeSpan(0, 0, 5));
	// IWebElement element = wait.Until<IWebElement>(d => d.FindElement(
	// By.XPath(selector+"[text()!='0.0000']")));
	// }

}

// wait.Until<bool>((d) =>
// {
// var remoteWebDriver = (RemoteWebElement)GetElement(TextBoxSelector, timeout);
// var javaScriptExecutor = (IJavaScriptExecutor)remoteWebDriver.WrappedDriver;
// return input == javaScriptExecutor.ExecuteScript("return
// arguments[0].innerHTML;", element).ToString();
// });

// public BaseBrowserBot WaitFor<T>(Func<T, bool> condition)
// {
// WebDriverWait<T> wait = new WebDriverWait<T>();
// wait.Until(condition);
// return this;
// }
