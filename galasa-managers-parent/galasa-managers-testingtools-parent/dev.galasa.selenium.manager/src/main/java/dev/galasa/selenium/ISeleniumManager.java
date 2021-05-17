/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020,2021.
 */
package dev.galasa.selenium;

import java.util.List;

import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.ie.InternetExplorerOptions;
import org.openqa.selenium.opera.OperaOptions;

public interface ISeleniumManager {
	 /**
     * Allocate a new WebPage
     * @throws SeleniumManagerException
     */
    public IWebPage allocateWebPage() throws SeleniumManagerException;

    /**
     * Allocate a new WebPage for a provided URL
     * @throws SeleniumManagerException
     */
    public IWebPage allocateWebPage(String url) throws SeleniumManagerException;

    /**
     * Allocate a new WebPage for a provided URL with Firefox Options
     * @throws SeleniumManagerException
     */
    public IWebPage allocateWebPage(String url, IFirefoxOptions options) throws SeleniumManagerException;

    /**
     * Allocate a new WebPage for a provided URL with Chrome Options
     * @throws SeleniumManagerException
     */
    public IWebPage allocateWebPage(String url, ChromeOptions options) throws SeleniumManagerException;

    /**
     * Allocate a new WebPage for a provided URL with Edge Options
     * @throws SeleniumManagerException
     */
    public IWebPage allocateWebPage(String url, EdgeOptions options) throws SeleniumManagerException;

    /**
     * Allocate a new WebPage for a provided URL with Internet Explorer Options
     * @throws SeleniumManagerException
     */
    public IWebPage allocateWebPage(String url, InternetExplorerOptions options) throws SeleniumManagerException;

    /**
     * Allocate a new WebPage for a provided URL with Opera Options
     * @throws SeleniumManagerException
     */
    public IWebPage allocateWebPage(String url, OperaOptions options) throws SeleniumManagerException;

    /**
     * Creates a new interface to the Firefox properties that can be set.
     * @return IFirefoxOptions
     */
    public IFirefoxOptions getFirefoxOptions();

    /**
     * Creates a new interface to the Chrome properties that can be set.
     * @return IChromeOptions
     */
    public IChromeOptions getChromeOptions();

    /**
     * Creates a new interface to the Edge properties that can be set.
     * @return IEdgeOptions
     */
    public IEdgeOptions getEdgeOptions();

    /**
     * Creates a new interface to the InternetExplorer properties that can be set.
     * @return IInternetExplorerOptions
     */
    public IInternetExplorerOptions getInternetExplorerOptions();  
    
    /**
     * Return the active pages
     * @return List<IWebPage>
     */
    public List<IWebPage> getPages();
    
    /**
     * Cycle through any pages and quit
     */
    public void discard();
}