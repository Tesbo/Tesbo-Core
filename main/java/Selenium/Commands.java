package Selenium;

import framework.Utility;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import static com.google.common.truth.Truth.assertThat;

public class Commands {


    public String getElementValue(String elementName, String suiteName) {
        Utility jsonParser = new Utility();

        return jsonParser.loadJsonFile(suiteName).get(elementName).toString();
    }


    /**
     * @param driver       webdriver object for the Test
     * @param elementvalue element loactor valuse
     * @return webelement
     */


    public WebElement findElement(WebDriver driver, String elementvalue) {


        WebElement element = null;

        try {
            element = driver.findElement(By.cssSelector(elementvalue));
        } catch (NoSuchElementException css) {

            try {
                element = driver.findElement(By.id(elementvalue));
            } catch (NoSuchElementException id) {
                try {
                    element = driver.findElement(By.xpath(elementvalue));
                } catch (Exception xpath) {
                    try {
                        element = driver.findElement(By.className(elementvalue));
                    } catch (Exception className) {
                        try {
                            element = driver.findElement(By.name(elementvalue));
                        } catch (Exception name) {
                            try {
                                element = driver.findElement(By.tagName(elementvalue));
                            } catch (Exception tagName) {
                                try {
                                    element = driver.findElement(By.linkText(elementvalue));
                                } catch (Exception linkText) {
                                    try {
                                        element = driver.findElement(By.partialLinkText(elementvalue));
                                    } catch (Exception partialLinkText) {
                                        System.out.println("Please enter valid locator value");
                                    }
                                }

                            }
                        }
                    }

                }

            }


        }


        return element;
    }

    /**
     * @param el webelement
     */

    public void clickOnElement(WebElement el) {
        el.click();
    }

    /**
     * @param el   webelement
     * @param text text to enter into the text box
     */

    public void sendskeyOnElement(WebElement el, String text) {
        el.sendKeys(text);
    }


    /**
     * @param el verify element is displayed or not
     */
    public void verifyElementShouldDisplayed(WebElement el) {
        assertThat(el.isDisplayed()).isTrue();
    }


    public void openUrl(WebDriver driver, String url) {
        driver.get(url);
    }


}
