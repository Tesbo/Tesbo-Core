package Selenium;

import framework.Utility;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static com.google.common.truth.Truth.assertThat;
import static org.openqa.selenium.support.ui.ExpectedConditions.*;

public class Commands {

    protected static Wait<WebDriver> wait;

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

    public String parantWindow = "";
    public String childWindow = "";

    /**
     * @param driver
     * @Description : Switch to new open window.
     */
    public void switchNewWindow(WebDriver driver) {
        //Get parent window.
        parantWindow = driver.getWindowHandle();
        //Get all open windows.
        Set<String> handles = driver.getWindowHandles();

        for (String windowHandle : handles) {
            //Condition for get new window.
            if (!windowHandle.equals(parantWindow)) {
                childWindow = windowHandle;
                //Switch to new window.
                driver.switchTo().window(childWindow);
            }
        }
    }

    /**
     * @param driver
     * @Description : Switch to main/parent window.
     */
    public void switchMainWindow(WebDriver driver) {
        driver.switchTo().window(parantWindow);
    }

    /**
     * @param driver
     * @Description : Close window.
     */
    public void closeWindow(WebDriver driver) {
        driver.close();
    }

    /**
     * @param driver
     * @Description : Navigate to back window.
     */
    public void navigateBack(WebDriver driver) {
        driver.navigate().back();
    }

    /**
     * @param driver
     * @Description : Navigate to forward window.
     */
    public void navigateForward(WebDriver driver) {
        driver.navigate().forward();
    }

    /**
     * @param driver
     * @Description : Navigate refresh window.
     */
    public void navigateRefresh(WebDriver driver) {
        driver.navigate().refresh();
    }

    /**
     * @param driver
     * @Description : Scrolling to bottom.
     */
    public void scrollBottom(WebDriver driver) {
        JavascriptExecutor js = ((JavascriptExecutor) driver);
        js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
    }

    /**
     * @param driver
     * @Description : Scrolling to top.
     */
    public void scrollTop(WebDriver driver) {
        JavascriptExecutor js = ((JavascriptExecutor) driver);
        js.executeScript("window.scrollTo(0, -document.body.scrollHeight);");
    }

    /**
     * @param driver
     * @param element
     * @Description : Scrolling to web element.
     */
    public void scrollToElement(WebDriver driver, WebElement element) {
        JavascriptExecutor js = ((JavascriptExecutor) driver);
        js.executeScript("arguments[0].scrollIntoView();", (element));
    }

    /**
     * @param driver
     * @param x
     * @param y
     * @Description : Scrolling to coordinate.
     */
    public void scrollToCoordinate(WebDriver driver, String x, String y) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("window.scrollBy(" + x + ", " + y + ")");
    }

    /**
     * @param driver
     * @param element
     * @Description : pause driver until element disappear.
     */
    public void pauseElementDisappear(WebDriver driver, WebElement element) {
        wait = new WebDriverWait(driver, 100);
        wait.until(invisibilityOf(element));
    }

    /**
     * @param driver
     * @param element
     * @Description : pause driver until element clickable.
     */
    public void pauseElementClickable(WebDriver driver, WebElement element) {
        wait = new WebDriverWait(driver, 100);
        wait.until(elementToBeClickable(element));
    }

    /**
     * @param driver
     * @param elementvalue
     * @return : Web element
     * @Description : pause driver until element display.
     */
    public WebElement pauseElementDisplay(WebDriver driver, String elementvalue) {
        Wait<WebDriver> wait = new FluentWait<WebDriver>(driver)
                .withTimeout(100, TimeUnit.SECONDS)
                .pollingEvery(1, TimeUnit.SECONDS)
                .ignoring(NoSuchElementException.class);

        WebElement foo = wait.until(new Function<WebDriver, WebElement>() {
            public WebElement apply(WebDriver driver) {
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
        });

        return foo;
    }
}
