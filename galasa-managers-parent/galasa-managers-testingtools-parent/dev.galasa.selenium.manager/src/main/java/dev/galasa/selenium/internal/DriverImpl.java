package dev.galasa.selenium.internal;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.WebDriver;

import dev.galasa.selenium.IWebPage;
import dev.galasa.selenium.SeleniumManagerException;

public class DriverImpl {
	private List<WebPageImpl> webPages = new ArrayList<>();
	 	 
	public IWebPage allocatePage(WebDriver driver, String url, Path screenshotRasDirectory)
			throws SeleniumManagerException {
		WebPageImpl webPage = new WebPageImpl(driver, webPages, screenshotRasDirectory);

		if (url != null && !url.trim().isEmpty())
			webPage.get(url);

		this.webPages.add(webPage);
		return webPage;
	}
	 	 
	public void discardPages() {
		for (WebPageImpl page : webPages) {
            page.managerQuit();
        }

	}	
}