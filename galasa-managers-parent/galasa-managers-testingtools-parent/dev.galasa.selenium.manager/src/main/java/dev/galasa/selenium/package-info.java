/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2020.
 */
/**
 * Selenium Manager
 * 
 * @galasa.manager Selenium
 * 
 * @galasa.release.state ALPHA - This Manager is being actively developed. It is subject to change and has not been extensively tested. 
 * 
 * @galasa.description
 * 
 * This Manager enables the test to run Selenium WebDrivers in order to drive Web Browsers during the test. Browsers can have actions performed against them 
 * to navigate WebPages and extract information about the current page.
 * <br><br>
 * As an absolute minimum, the CPS property <br>
 * <code>selenium.instance.PRIMARY.gecko.path</code><br>
 * must be provided as the manager will default to using a GECKO WebDriver if no WebDriver is provided.
 * <br><br>
 * The CPS property <br>
 * <code>selenium.instance.PRIMARY.web.driver</code><br>
 * can be used to set a different WebDriver to be used. This will also require the corresponding driver path to be set. <br>
 * eg. <code>selenium.instance.PRIMARY.web.driver=CHROME</code><br>
 * requires <code>selenium.instance.PRIMARY.chrome.path=...</code><br>
 * 
 * @galasa.limitations
 * 
 * The Selenium Manager only supports GECKO, CHROME, EDGE and IE WebDrivers
 */
package dev.galasa.selenium;
