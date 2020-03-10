/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
package dev.galasa.selenium;

public interface ISeleniumManager {

    /**
     * Allocate a new WebPage
     */
    public IWebPage allocateWebPage();

    /**
     * Allocate a new WebPage for a provided URL
     */
    public IWebPage allocateWebPage(String url);

    /**
     * Allocate a new WebPage for a provided URL and Tag
     */
    public IWebPage allocateWebPage(String url, String tag);

    /**
     * Returns the WebPage with a given Tag
     */
    public IWebPage getWebPage(String tag);

}