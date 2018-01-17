package Selenium;

import framework.Utility;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import static com.google.common.truth.Truth.assertThat;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;

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

        int webdriverTime = 600;

        WebDriverWait wait = new WebDriverWait(driver, webdriverTime);
        pause(3);
        try {
            System.out.println("Inside CSS");

            element = driver.findElement(By.cssSelector(elementvalue));

        } catch (NoSuchElementException css) {

            try {
                System.out.println("Inside id");
                element = driver.findElement(By.id(elementvalue));

            } catch (NoSuchElementException id) {
                try {

                    System.out.println("Inside xpath" + elementvalue);

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


    public void pause(int sec) {
        try {
            Thread.sleep(sec * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public void switchToActiveElement(WebDriver driver) {
        driver.switchTo().activeElement();
    }


    public void switchToDefaultContent(WebDriver driver) {
        driver.switchTo().defaultContent();
    }

    /**
     * @param driver
     * @param ID
     * @Description : Switch to IFrame using ID, Name and Web Element.
     */
    public void switchFrame(WebDriver driver, String ID) {
        driver.switchTo().frame(ID);
    }

    /**
     * @param driver
     * @Description : Switch to main or parent IFrame.
     */
    public void switchMainFrame(WebDriver driver) {
        driver.switchTo().parentFrame();
    }

    /**
     * @param driver
     * @Description : Switch to main or parent IFrame.
     */
    public void switchFrameElement(WebDriver driver, WebElement element) {
        driver.switchTo().frame(element);
    }

    /**
     * @param driver
     * @Description : Switch to alert and Accept.
     */
    public void switchAlertAccept(WebDriver driver) {
        driver.switchTo().alert().accept();
    }

    /**
     * @param driver
     * @Description : Switch to alert and Dismiss.
     */
    public void switchAlertDismiss(WebDriver driver) {
        driver.switchTo().alert().dismiss();
    }

    /**
     * @param driver
     * @Description : Switch to alert and get alert text.
     */
    public String switchAlertRead(WebDriver driver) {
        return driver.switchTo().alert().getText();
    }

    /**
     * @param driver
     * @param Text
     * @Description : Switch to alert and enter text.
     */
    public void switchAlertSendKey(WebDriver driver, String Text) {
        driver.switchTo().alert().sendKeys(Text);
    }
}
