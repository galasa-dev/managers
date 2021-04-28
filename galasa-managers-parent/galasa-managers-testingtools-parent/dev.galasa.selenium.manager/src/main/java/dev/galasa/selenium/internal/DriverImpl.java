/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2021.
 */
package dev.galasa.selenium.internal;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.WebDriver;

import dev.galasa.selenium.IWebPage;
import dev.galasa.selenium.SeleniumManagerException;

/**
 * Superclass for the local and remote driver implementations for tracking and discarding webpages
 * 
 * @author jamesdavies
 *
 */
public class DriverImpl {
	private List<IWebPage> webPages = new ArrayList<>();
	 	 
	public IWebPage allocatePage(WebDriver driver, String url, Path screenshotRasDirectory)
			throws SeleniumManagerException {
		WebPageImpl webPage = new WebPageImpl(driver, webPages, screenshotRasDirectory);

		if (url != null && !url.trim().isEmpty())
			webPage.get(url);

		this.webPages.add(webPage);
		return webPage;
	}
	 	 
	public void discardPages() {
		for (IWebPage page : webPages) {
            page.close();
        }
	}	
	
	public List<IWebPage> getPages() {
		return this.webPages;
	}
}