/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.selenium;

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
     * Allocate a new WebPage for a provided URL and Tag
     * @throws SeleniumManagerException
     */
    public IWebPage allocateWebPage(String url, String tag) throws SeleniumManagerException;

    /**
     * Returns the WebPage with a given Tag
     */
    public IWebPage getWebPage(String tag);

}