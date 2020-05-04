package Selenium;

import Execution.SetCommandLineArgument;
import framework.GetConfiguration;
import framework.Validation;
import logger.TesboLogger;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import Exception.TesboException;

import static org.openqa.selenium.support.ui.ExpectedConditions.*;

public class Commands {

    protected static Wait<WebDriver> wait;
    public String parantWindow = "";
    public String childWindow = "";
    TesboLogger tesboLogger =new TesboLogger();
    private static final Logger log = LogManager.getLogger(Validation.class);
    boolean isIf=true;

    /**
     * @param driver       webdriver object for the Test
     * @param elementvalue element loactor valuse
     * @return webelement
     */

    public WebElement findElement(WebDriver driver, String elementvalue) {
        WebElement element = null;
        GetConfiguration config = new GetConfiguration();
        int webdriverTime = 600;

        WebDriverWait wait = new WebDriverWait(driver, webdriverTime);
        pause(3);

        if(elementvalue.contains("_IF")){
            isIf=false;
            elementvalue=elementvalue.replace("_IF","");
        }
        ArrayList<String> locatorTypes=new ArrayList<>();
        locatorTypes=config.getLocatorPreference();
        if(locatorTypes!=null){
            if(locatorTypes.size()==0){
                log.error("Please enter locator Preference");
                throw new TesboException("Please enter locator Preference");
            }
            element= findElementFromLocatorPreference(driver,elementvalue,locatorTypes);

        }else {
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
                                        } catch (NoSuchElementException e) {
                                            if(isIf) {
                                                tesboLogger.testFailed("Element Not found With Locator '"+elementvalue+"'");
                                                throw new TesboException("Element Not found With Locator '"+elementvalue+"'");
                                            }
                                        }
                                    }

                                }
                            }
                        }
                    }
                }
            }
            if(element==null){
                //throw new TesboException("Please enter valid locator value");
            }
            if (config.getHighlightElement()) {
                JavascriptExecutor js = (JavascriptExecutor) driver;
                js.executeScript("arguments[0].setAttribute('style', 'background: yellow; border: 2px solid red;');", element);
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                js.executeScript("arguments[0].setAttribute('style', arguments[1]);", element, "");
            }
        }
        return element;
    }

    public WebElement findElementFromLocatorPreference(WebDriver driver, String elementvalue,ArrayList<String> locatorTypes) {
        WebElement element = null;
        GetConfiguration config = new GetConfiguration();
        int webdriverTime = 600;

        WebDriverWait wait = new WebDriverWait(driver, webdriverTime);
        pause(3);
        int locatorTypesSize=locatorTypes.size();
        int locatorCount=1;
        for (String locatorType:locatorTypes){

            if(locatorType.equalsIgnoreCase("css")){
                try {
                    element = driver.findElement(By.cssSelector(elementvalue));
                    break;
                } catch (Exception css) {
                    if(locatorCount==locatorTypesSize) {
                        if(isIf) {
                            tesboLogger.testFailed("Element Not found With Locator '"+elementvalue+"'");
                            throw new TesboException("Element Not found With Locator '"+elementvalue+"'");
                        }
                    }
                }
            }
            else if(locatorType.equalsIgnoreCase("id")){
                try {
                    element = driver.findElement(By.id(elementvalue));
                    break;
                } catch (Exception id) {
                    if(locatorCount==locatorTypesSize) {
                        {
                            if(isIf){
                                tesboLogger.testFailed("Element Not found With Locator '"+elementvalue+"'");
                                throw new TesboException("Element Not found With Locator '"+elementvalue+"'");
                            }
                        }
                    }
                }
            }
            else if(locatorType.equalsIgnoreCase("xpath")){
                try {
                    element = driver.findElement(By.xpath(elementvalue));
                    break;
                } catch (Exception xpath) {
                    if(locatorCount==locatorTypesSize) {
                        if(isIf) {
                            tesboLogger.testFailed("Element Not found With Locator '"+elementvalue+"'");
                            throw new TesboException("Element Not found With Locator '"+elementvalue+"'");
                        }
                    }
                }
            }
            else if(locatorType.equalsIgnoreCase("className")){
                try {
                    element = driver.findElement(By.className(elementvalue));
                    break;
                } catch (Exception className) {
                    if(locatorCount==locatorTypesSize) {
                        if(isIf) {
                            tesboLogger.testFailed("Element Not found With Locator '"+elementvalue+"'");
                            throw new TesboException("Element Not found With Locator '"+elementvalue+"'");
                        }
                    }
                }
            }
            else if(locatorType.equalsIgnoreCase("name")){
                try {
                    element = driver.findElement(By.name(elementvalue));
                    break;
                } catch (Exception name) {
                    if(locatorCount==locatorTypesSize) {
                        if(isIf) {
                            tesboLogger.testFailed("Element Not found With Locator '"+elementvalue+"'");
                            throw new TesboException("Element Not found With Locator '"+elementvalue+"'");
                        }
                    }
                }
            }
            else if(locatorType.equalsIgnoreCase("tagName")){
                try {
                    element = driver.findElement(By.tagName(elementvalue));
                    break;
                } catch (Exception tagName) {
                    if(locatorCount==locatorTypesSize) {
                        if(isIf) {
                            tesboLogger.testFailed("Element Not found With Locator '"+elementvalue+"'");
                            throw new TesboException("Element Not found With Locator '"+elementvalue+"'");
                        }
                    }
                }
            }
            else if(locatorType.equalsIgnoreCase("linkText")){
                try {
                    element = driver.findElement(By.linkText(elementvalue));
                    break;
                } catch (Exception linkText) {
                    if(locatorCount==locatorTypesSize) {
                        if(isIf) {
                            tesboLogger.testFailed("Element Not found With Locator '"+elementvalue+"'");
                            throw new TesboException("Element Not found With Locator '"+elementvalue+"'");
                        }
                    }
                }
            }
            else if(locatorType.equalsIgnoreCase("partialLinkText")){
                try {
                    element = driver.findElement(By.partialLinkText(elementvalue));
                    break;
                } catch (Exception e) {
                    if(locatorCount==locatorTypesSize) {
                        if(isIf) {
                            tesboLogger.testFailed("Element Not found With Locator '"+elementvalue+"'");
                            throw new TesboException("Element Not found With Locator '"+elementvalue+"'");
                        }
                    }
                }
            }
            locatorCount++;
        }
        if(element==null){
            //throw new TesboException("Please enter valid locator value");
        }

        if (config.getHighlightElement() && isIf) {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("arguments[0].setAttribute('style', 'background: yellow; border: 2px solid red;');", element);

            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            js.executeScript("arguments[0].setAttribute('style', arguments[1]);", element, "");
        }
        return element;
    }

    public List<WebElement> findElements(WebDriver driver, String elementvalue) {

        List<WebElement> listOfElements =null;
        int webdriverTime = 600;

        WebDriverWait wait = new WebDriverWait(driver, webdriverTime);
        pause(3);
        try {

            listOfElements = driver.findElements(By.cssSelector(elementvalue));

        } catch (NoSuchElementException css) {

            try {
                listOfElements = driver.findElements(By.xpath(elementvalue));
            } catch (Exception xpath) {
                try {
                    listOfElements = driver.findElements(By.id(elementvalue));

                } catch (NoSuchElementException id) {
                    try {
                        listOfElements = driver.findElements(By.className(elementvalue));
                    } catch (Exception className) {
                        try {
                            listOfElements = driver.findElements(By.name(elementvalue));
                        } catch (Exception name) {
                            try {
                                listOfElements = driver.findElements(By.tagName(elementvalue));
                            } catch (Exception tagName) {
                                try {
                                    listOfElements = driver.findElements(By.linkText(elementvalue));
                                } catch (Exception linkText) {
                                    try {
                                        listOfElements = driver.findElements(By.partialLinkText(elementvalue));
                                    } catch (NoSuchElementException e) {
                                        if(isIf) {
                                            tesboLogger.testFailed("Element Not found With Locator '"+elementvalue+"'");
                                            throw new TesboException("Element Not found With Locator '"+elementvalue+"'");
                                        }
                                    }
                                }

                            }
                        }
                    }

                }

            }
        }

        return listOfElements;
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
     *  verify element is displayed or not
     */
    /*public void verifyElementShouldDisplayed(WebElement el) {
        assertThat(el.isDisplayed()).isTrue();
    }*/

    public void openUrl(WebDriver driver, String url) {
        driver.get(url);
    }

    /**
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     * @param driver
     * @param element
     */
    public void scrollAndClick(WebDriver driver, WebElement element)  {
        scrollToCoordinate(driver, "250", "300");
        pause(1);
        element.click();
    }

    public void pause(int sec) {
        try {
            Thread.sleep(sec * 1000);
        } catch (InterruptedException e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            tesboLogger.testFailed(sw.toString());
            log.error(sw.toString());
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
     * @Description :
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
        //driver.switchTo().alert().sendKeys(Text);
        Alert alert  = new WebDriverWait(driver, 10).until(ExpectedConditions.alertIsPresent());
        alert.sendKeys(Text);
        alert.accept();
    }

    /**
     * @param driver
     * @Description : Switch to new open window.
     */
    public void switchNewWindow(WebDriver driver, String browser) {
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
                if(browser.equals("firefox")){
                    break;
                }
            }
        }
    }

    /**
     * @param driver
     * @Description : Switch to main/parent window.
     */
    public void switchMainWindow(WebDriver driver) {
        Set<String> handles = driver.getWindowHandles();

        for (String windowHandle : handles) {
            //Condition for get new window.
            driver.switchTo().window(windowHandle);
            break;
        }

    }

    /**
     * @param driver
     * @Description : Close window.
     */
    public void closeWindow(WebDriver driver) {
        driver.close();
    }

    /**
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     * @param driver
     * @param step
     * @param browser
     * @Description : Close window tab using index.
     */
    public void closeWindowByIndex(WebDriver driver, String step,String browser) {

        int startPoint = step.indexOf("<") + 1;
        int endPoint = step.lastIndexOf(">");
        String indexes = step.substring(startPoint, endPoint).trim();
        String index[];
        if(indexes.toLowerCase().contains("to")){
            int startIndex=Integer.parseInt(indexes.toLowerCase().split("to")[0].trim());
            int endIndex=Integer.parseInt(indexes.toLowerCase().split("to")[1].trim());
            if(startIndex>endIndex) {
                throw new TesboException("Starting index is greater than end index <" + indexes + ">");
            }
            int numberOfIndex=0;
            for(int i=startIndex;i<=endIndex;i++){numberOfIndex++;}
            index= new String[numberOfIndex];
            for(int i=0;i<numberOfIndex;i++){ index[i]= String.valueOf(startIndex++); }
        }else {
            if (indexes.contains(",")) {
                index = indexes.split(",");
            } else {
                index = new String[1];
                index[0] = indexes;
            }
        }
        ArrayList<String> tabs = new ArrayList<> (driver.getWindowHandles());
        if(browser.equalsIgnoreCase("chrome")){
            ArrayList<String> chromeTabs= new ArrayList<>();
            chromeTabs.add(tabs.get(0));
            for(int i=tabs.size()-1;i>0;i--){
                chromeTabs.add(tabs.get(i));
            }
            tabs=new ArrayList<>();
            tabs.addAll(chromeTabs);
        }
        int tabIndex;
        for(int i=0; i<index.length;i++) {
            try {
                 tabIndex=Integer.parseInt(index[i]);
            }catch (Exception e){
                throw new TesboException("Enter numeric index value for close particular browser tab");
            }
            try {
                driver.switchTo().window(tabs.get(tabIndex));
                driver.close();
            }catch (Exception e){
                throw new TesboException("Browser tab index <"+tabIndex+"> is not found");
            }
        }

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
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     * @param driver
     */
    public void scrollHorizontal(WebDriver driver) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("window.scrollBy(1000,0)", "");
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
    public void pauseAndClick(WebDriver driver, WebElement element) {
        wait = new WebDriverWait(driver, 100);
        wait.until(elementToBeClickable(element));
        element.click();
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
     * @param element
     * @return : Web element
     * @Description : pause driver until element display.
     */
    public void pauseElementDisplay(WebDriver driver, WebElement element) {
        wait = new WebDriverWait(driver, 100);
        wait.until(visibilityOf(element));

    }

    /**
     * @param element
     * @param Text
     * @Description : select drop down using visible text.
     */
    public void selectText(WebElement element, String Text) {
        Select dropDown = new Select(element);
        dropDown.selectByVisibleText(Text);
    }

    /**
     * @param element
     * @param Index
     * @Description : select drop down value using index.
     */
    public void selectIndex(WebElement element, int Index) {
        Select dropDown = new Select(element);
        dropDown.selectByIndex(Index);
    }

    /**
     * @param element
     * @param Text
     * @Description : select drop down using value.
     */
    public void selectValue(WebElement element, String Text) {
        Select dropDown = new Select(element);
        dropDown.selectByValue(Text);
    }

    /**
     * @param element
     * @Description : Deselect all the value from drop down.
     */
    public void deselectAll(WebElement element) {
        Select dropDown = new Select(element);
        dropDown.deselectAll();
    }

    /**
     * @param element
     * @param Text
     * @Description : Deselect value using visible text.
     */
    public void deselectText(WebElement element, String Text) {
        Select dropDown = new Select(element);
        dropDown.deselectByVisibleText(Text);
    }

    /**
     * @param element
     * @param Index
     * @Description : Deselect value using Index.
     */
    public void deselectIndex(WebElement element, int Index) {
        Select dropDown = new Select(element);
        dropDown.deselectByIndex(Index);
    }

    /**
     * @param element
     * @param Text
     * @Description : Deselect drop down using value.
     */
    public void deselectValue(WebElement element, String Text) {
        Select dropDown = new Select(element);
        dropDown.deselectByValue(Text);
    }


    public String captureScreenshot(WebDriver driver, String suitName, String testName) {
        String screenshotName = captureScreen(driver, suitName, testName);
        return screenshotName;
    }

    public String captureScreen(WebDriver driver, String suitName, String testName) {
        String path;
        try {
            File filePath = new File("screenshots");
            WebDriver augmentedDriver = new Augmenter().augment(driver);
            File scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
            path = filePath + "/" + (suitName.split(".s"))[0] + "_" + testName.replaceAll("\\s", "") + "_" + dtf.format(LocalDateTime.now()) + ".png";
            FileUtils.copyFile(scrFile, new File(path));
        } catch (IOException e) {
            path = "Failed to capture screenshot: " + e.getMessage();
        }
        return path;
    }

    /**
     * @param browserName
     * @return
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     */
    public boolean IsCapabilities(String browserName) {
        GetConfiguration config = new GetConfiguration();
        JSONObject capabilities = null;
        boolean browser = false;
        try {
            capabilities = config.getCapabilities(browserName);
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            tesboLogger.testFailed(sw.toString());
            log.error(sw.toString());
        }
        if (capabilities != null) {
            if(capabilities.size()>0){
                browser = true;
            }
            else {
                browser = false;
            }
        }
        return browser;
    }

    /**
     * @return
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     */
    public String getSeleniumAddress() {
        GetConfiguration config = new GetConfiguration();
        String seleniumAddress = null;
        try {
            seleniumAddress = config.getSeleniumAddress();
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            tesboLogger.testFailed(sw.toString());
            log.error(sw.toString());
        }
        return seleniumAddress;
    }

    /**
     * @param browserName
     * @return
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     */
    public JSONObject getCapabilities(String browserName) {
        GetConfiguration config = new GetConfiguration();
        JSONObject capabilities = null;
        try {
            capabilities = config.getCapabilities(browserName);
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            tesboLogger.testFailed(sw.toString());
            log.error(sw.toString());
        }
        //ArrayList<String> capabilitieList = (ArrayList<String>) capabilities.get(browserName);

        return capabilities;
    }

    /**
     * @param Capabilities
     * @param capability
     * @return
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     */
    public DesiredCapabilities setCapabilities(JSONObject Capabilities, DesiredCapabilities capability) {
        SetCommandLineArgument setCommandLineArgument=new SetCommandLineArgument();
        for (Object cap : Capabilities.keySet()) {
            capability.setCapability(cap.toString(), Capabilities.get(cap.toString()));
        }

        if(setCommandLineArgument.platform!=null){
            capability.setCapability("platform", setCommandLineArgument.platform);
        }
        return capability;
    }

    /**
     * @param seleniumAddress
     * @param driver
     * @param capability
     * @return
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     */
    public WebDriver openRemoteBrowser(WebDriver driver, DesiredCapabilities capability, String seleniumAddress) {
        try {
            driver = new RemoteWebDriver(new URL(seleniumAddress), capability);
        } catch (MalformedURLException e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            tesboLogger.testFailed(sw.toString());
            log.error(sw.toString());
        }
        return driver;
    }

    /**
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     * @param driver
     * @param element
     */
    public void rightClick(WebDriver driver,WebElement element) {
        Actions action = new Actions(driver).contextClick(element);
        action.build().perform();
    }

    /**
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     * @param driver
     * @param element
     */
    public void doubleClick(WebDriver driver,WebElement element) {
        Actions action = new Actions(driver);
        action.doubleClick(element).perform();
    }

    /**
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     * @param driver
     *
     */
    public void deleteAllCookies(WebDriver driver) {
        driver.manage().deleteAllCookies();
    }

    /**
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     * @param driver
     * @param element
     */
    public void mouseHover(WebDriver driver, WebElement element)  {
        Actions action = new Actions(driver);
        action.moveToElement(element).build().perform();
    }

    /**
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     *
     * @param driver
     * @param elementTo
     * @param elementFrom
     */
    public void dragAndDrop(WebDriver driver, WebElement elementFrom,WebElement elementTo)  {
        Actions action = new Actions(driver);
        action.dragAndDrop(elementFrom, elementTo).build().perform();

        /*Action dragAndDrop = action.clickAndHold(elementFrom)
                .moveToElement(elementTo)
                .release(elementTo)
                .build();

        dragAndDrop.perform();*/

       /*action.keyDown(Keys.CONTROL)
                .click(elementFrom)
                .dragAndDrop(elementFrom, elementTo)
                .keyUp(Keys.CONTROL);
        Action selected = action.build();
        selected.perform();*/

        //action.clickAndHold(elementFrom).moveToElement(elementTo).release(elementFrom).build().perform()

        //Action dragAndDrop = action.clickAndHold(elementFrom).moveToElement(elementTo).release().build();
        //dragAndDrop.perform();

       /* WebDriver _driver=driver;
        WebElement _sourceElement = elementFrom;
        WebElement _targetElement = elementTo;
        JavascriptExecutor _js = (JavascriptExecutor) _driver;
        _js.executeScript("$(arguments[0]).simulate('drag-n-drop',{dragTarget:arguments[1],interpolation:{stepWidth:100,stepDelay:50}});",( _sourceElement), (_targetElement));*/
    }


    /**
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     * @param driver
     * @param element
     */
    public void clickAndHold(WebDriver driver, WebElement element)  {
        Actions action = new Actions(driver);
        action.clickAndHold(element).build().perform();
    }

    /**
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     * @param driver
     *
     */
    public void getPageSource(WebDriver driver)  {
        log.error(driver.getPageSource());
        tesboLogger.stepLog(driver.getPageSource());
    }

    /**
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     * @param driver
     * @param cookieName
     */
    public boolean isCookieAvailable(WebDriver driver, String cookieName)  {
        boolean isCookie=false;
        for(Cookie ck : driver.manage().getCookies())
        {
            if(cookieName.equalsIgnoreCase(ck.getName())){ isCookie=true; }
        }
        return isCookie;

    }

    /**
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     * @param driver
     * @param element
     * @param suitName
     * @param testName
     * @throws IOException
     */
    public String screenshotElement(WebDriver driver, WebElement element, String suitName, String testName) throws IOException {

        // Get entire page screenshot
        String path;
        File screenshot = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
        BufferedImage fullImg = null;
        try {
            fullImg = ImageIO.read(screenshot);
            // Get the location of element on the page
            Point point = element.getLocation();
            // Get width and height of the element
            int eleWidth = element.getSize().getWidth();
            int eleHeight = element.getSize().getHeight();
            // Crop the entire page screenshot to get only element screenshot
            BufferedImage eleScreenshot= fullImg.getSubimage(point.getX(), point.getY(), eleWidth, eleHeight);
            ImageIO.write(eleScreenshot, "png", screenshot);
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
            path ="ElementScreenshots/"+(suitName.split(".s"))[0] + "_" + testName.replaceAll("\\s", "") + "_" + dtf.format(LocalDateTime.now()) + ".png";
            File screenshotLocation = new File(path);
            FileUtils.copyFile(screenshot, screenshotLocation);
        } catch (IOException e) {
            throw e;
        }
        return path;
    }

    /**
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     * @param driver
     * @param offset
     * @param element
     */
    public void clickOnOffset(WebDriver driver,WebElement element, String[] offset)  {
        Actions builder = new Actions(driver);
        builder.moveToElement(element, Integer.parseInt(offset[0].trim()), Integer.parseInt(offset[1].trim())).click().build().perform();

    }

    /**
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     * @param driver
     */
    public boolean getCurrentUrl(WebDriver driver,String url)  {
        boolean isURL=false;
        if(url.equals(driver.getCurrentUrl())){ isURL=true; }
        return isURL;
    }

    /**
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     * @param driver
     */
    public boolean verifyCurrentUrlContains(WebDriver driver,String url)  {
        boolean isURL=false;
        if(driver.getCurrentUrl().contains(url)){ isURL=true; }
        return isURL;
    }

}
