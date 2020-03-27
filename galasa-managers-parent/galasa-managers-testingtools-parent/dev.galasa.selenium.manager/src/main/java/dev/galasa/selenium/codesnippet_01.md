### Create the Selenium Manager

The following snippet shows the minimum code that is required to request the Selenium Manager in a test:

```
@SeleniumManager
public ISeleniumManager seleniumManager;
```

The code creates an interface to the Selenium Manager which will allow the tester to provision WebPages to test against.

### Create a WebPage

```
IWebPage page = seleniumManager.allocateWebPage("https://galasa.dev/");
```

The code creates a WebPage with a Selenium WebDriver controlling the browser. This object provides an interface for the tester to perform actions on the page to navigate around, check the page content and switch between windows.

At the end of the test, the Selenium Manager automatically closes down the WebDriver which removes the WebPage.

There is no limit in Galasa on how many Selenium WebPages can be used within a single test. The only limit is the ability of the Galasa Ecosystem they are running on to support the Selenium WebDrivers not timing out.

### Navigating around a WebPage Browser

```
page.clearElementByCssSelector("input.js-search-input.search__input--adv");
page.sendKeysToElementByClass("js-search-input.search__input--adv", "Galasa");
page.clickElementById("search_button_homepage");
```

The code showcases different actions which can be performed on a WebPage interface to interact with different WebElements on the Browser. These WebElements are selected using a range of different techniques which allows the tester flexibility in how they are selected.

### Extracting WebPage information

```
WebElement element = page.findElementById("search_button_homepage");
String pageTitle = page.getTitle();
String pageSource = page.getPageSource();
```

The code shows different ways of gaining information about the WebPage to be tested against. Extracting the title is a very simple way of checking if the WebDriver is on the correct page and making sure that a WebElement is found.