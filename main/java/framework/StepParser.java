package framework;

import Selenium.Commands;
import logger.Logger;
import org.apache.commons.io.FileUtils;
import org.json.simple.JSONObject;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.Augmenter;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import Exception.TesboException;
import static org.assertj.core.api.Assertions.assertThat;

public class StepParser {


    Logger logger = new Logger();
    DataDrivenParser dataDrivenParser =new DataDrivenParser();
    public static void main(String[] args) {
        StepParser parser = new StepParser();

        //parser.parseTextToEnter("roshan mistry 'form Jsbot to' vadodara surat khate");
    }

    public void parseStep(WebDriver driver, JSONObject test, String step) throws Exception {
        Commands cmd = new Commands();
        GetLocator locator = new GetLocator();

        if (!step.toLowerCase().contains("enter") && !step.toLowerCase().contains("url"))
            logger.stepLog(step);

        //Clicks
        if (step.toLowerCase().contains("click")) { cmd.findElement(driver, locator.getLocatorValue(test.get("suiteName").toString(), parseElementName(step))).click();
        }

        //Sendkeys
        if (step.toLowerCase().contains("enter")) {
            cmd.findElement(driver, locator.getLocatorValue(test.get("suiteName").toString(), parseElementName(step))).sendKeys(parseTextToEnter(test,step));
        }

        // Get URL
        if (step.toLowerCase().contains("url")) {
            driver.get(parseTextToEnter(test,step));
        }

        //Switch
        if (step.toLowerCase().contains("switch")) {
            switchFunction(driver, test, step);
        }

        //navigate
        if (step.toLowerCase().contains("navigate")) {
            navigateFunction(driver, step);
        }

        //scroll
        if (step.toLowerCase().contains("scroll")) {
            scrollFunction(driver, test.get("suiteName").toString(), step);
        }

        //pause
        if (step.toLowerCase().contains("pause")) {
            pauseFunction(driver, test.get("suiteName").toString(), step);
        }

        //select
        if (step.toLowerCase().contains("select")) {
            selectFunction(driver, test, step);
        }

        //Clear
        if (step.toLowerCase().contains("clear")) {
            cmd.findElement(driver, locator.getLocatorValue(test.get("suiteName").toString(), parseElementName(step))).clear();
        }
        logger.testPassed("Passed");



    }


    public void switchFunction(WebDriver driver, JSONObject test, String step) throws Exception {

        Commands cmd = new Commands();
        GetLocator locator = new GetLocator();


        try {
            //Step :  switch to active Element
            if (step.toLowerCase().contains("active element")) {
                cmd.switchToActiveElement(driver);
            }

            /**
             * Switch to alert.
             */
            if (step.toLowerCase().contains("alert")) {
                /**
                 * accept identify.
                 * Step : Switch to alert then accept
                 */
                if (step.toLowerCase().contains("accept")) {
                    cmd.switchAlertAccept(driver);
                }
                /**
                 * close and cancel identify.
                 * Step : Switch to alert then close.
                 * Step : Switch to alert then cancel.
                 */
                else if (step.toLowerCase().contains("close") || step.toLowerCase().contains("cancel")) {
                    cmd.switchAlertDismiss(driver);
                }
                /**
                 * verify text identify.
                 * Step : Switch to alert then verify text with 'text'.
                 */
                else if (step.toLowerCase().contains("verify text")) {
                    String alertText = cmd.switchAlertRead(driver);
                    assertThat(alertText).containsIgnoringCase(parseTextToEnter(test,step));
                }
                /**
                 * enter identify.
                 * Step : Switch to alert then enter 'Text'.
                 */
                else if (step.toLowerCase().contains("enter")) {
                    cmd.switchAlertSendKey(driver, parseTextToEnter(test,step));
                }
            }


            //Step :  switch to default content
            if (step.toLowerCase().contains("default content")) {
                cmd.switchToDefaultContent(driver);
            }

            /**
             * Switch to frame
             */
            if (step.toLowerCase().contains("frame")) {
                //using identify.
                if (step.toLowerCase().contains("using")) {
                    //Step : Switch to frame using id 'FrameID'.
                    if (step.toLowerCase().contains("id")) {
                        cmd.switchFrame(driver, parseTextToEnter(test,step));
                    }
                    //Step : Switch to frame using name 'FrameName'.
                    else if (step.toLowerCase().contains("name")) {
                        cmd.switchFrame(driver, parseTextToEnter(test,step));
                    }
                }
                /**
                 * parent or main identify.
                 * Step : Switch to parent frame.
                 * Step : Switch to main frame.
                 */
                else if (step.toLowerCase().contains("parent") || step.toLowerCase().contains("main")) {
                    cmd.switchMainFrame(driver);
                }
                /**
                 * element identify.
                 * Step : Switch to frame @WebElement.
                 */
                else if (parseElementName(step) != null) {
                    try {
                        cmd.switchFrameElement(driver, cmd.findElement(driver, locator.getLocatorValue(test.get("suiteName").toString(), parseElementName(step))));
                    } catch (NullPointerException e) {
                        logger.errorLog("No element found.");
                        throw e;
                    }
                }
            }

            /**
             * Switch to window.
             */
            if (step.toLowerCase().contains("window")) {
                /**
                 * Step : Switch to new window.
                 */
                if (step.toLowerCase().contains("new")) {
                    cmd.switchNewWindow(driver);
                }
                /**
                 * Step : Switch to main window.
                 * Step : Switch to parent window.
                 */
                else if (step.toLowerCase().contains("main") || step.toLowerCase().contains("parent")) {
                    cmd.switchMainWindow(driver);
                }
                /**
                 * Step : close window.
                 */
                else if (step.toLowerCase().contains("close")) {
                    cmd.closeWindow(driver);
                }
            }

        } catch (Exception e) {
            logger.testFailed("Step Failed");
            throw e;
        }
    }

    public void navigateFunction(WebDriver driver, String step) {
        Commands cmd = new Commands();
        /**
         * back identify.
         * Step : Navigate to back
         */
        if (step.toLowerCase().contains("back")) {
            cmd.navigateBack(driver);
        }

        /**
         * forward identify.
         * Step : Navigate to forward
         */
        else if (step.toLowerCase().contains("forward")) {
            cmd.navigateForward(driver);
        }
        /**
         * refresh identify.
         * Step : Navigate refresh
         */
        else if (step.toLowerCase().contains("refresh")) {
            cmd.navigateRefresh(driver);
        }
    }

    public void scrollFunction(WebDriver driver, String suiteName, String step) throws Exception {
        Commands cmd = new Commands();
        GetLocator locator = new GetLocator();

        /**
         * 'Bottom' identify.
         * Step :Scroll to bottom.
         */
        if (step.toLowerCase().contains("bottom")) {
            cmd.scrollBottom(driver);
        }
        /**
         * 'top' identify.
         * step : Scroll to top.
         */
        else if (step.toLowerCase().contains("top")) {
            cmd.scrollTop(driver);
        }
        /**
         * number identify.
         * Step : Scroll to coordinate (50,100)
         */
        else if (step.toLowerCase().contains("coordinate")) {
            try {
                String x = parseNumverToEnter(step, 0);
                String y = parseNumverToEnter(step, 1);
                cmd.scrollToCoordinate(driver, x, y);
            } catch (NullPointerException e) {
                logger.testFailed("No coordinate found");
            }
        }
        /**
         * element identify.
         * Step : Scroll to @element
         */
        else if (parseElementName(step) != "") {
            try {
                cmd.scrollToElement(driver, cmd.findElement(driver, locator.getLocatorValue(suiteName, parseElementName(step))));
            } catch (NullPointerException e) {
                logger.testFailed("No element found");
                throw e;
            } catch (Exception e) {
                throw e;
            }
        }
    }

    public void pauseFunction(WebDriver driver, String suiteName, String step) throws Exception {
        Commands cmd = new Commands();
        GetLocator locator = new GetLocator();

        /**
         * 'disappear' identify.
         * Step : pause until @element is disappear
         */
        if (step.toLowerCase().contains("disappear")) {
            try {
                cmd.pauseElementDisappear(driver, cmd.findElement(driver, locator.getLocatorValue(suiteName, parseElementName(step))));
            } catch (Exception e) {
            }
        }
        /**
         * 'clickable' identify.
         * Step : pause until element is clickable
         */
        else if (step.toLowerCase().contains("clickable")) {
            try {
                cmd.pauseElementClickable(driver, cmd.findElement(driver, locator.getLocatorValue(suiteName, parseElementName(step))));
            } catch (Exception e) {
            }
        }
        /**
         * 'display' identify.
         * Step : pause until @Submit_Btn is display
         */
        else if (step.toLowerCase().contains("display")) {
            try {
                cmd.pauseElementDisplay(driver, locator.getLocatorValue(suiteName, parseElementName(step)));
            } catch (Exception e) {
            }
        }
        /**
         * 'sec' identify.
         * Step : pause for 5sec
         */
        else if (step.toLowerCase().contains("sec")) {
            cmd.pause(Integer.parseInt(parseNumverToEnter(step, 0)));
        }
    }

    public void selectFunction(WebDriver driver, JSONObject test, String step) throws Exception {
        Commands cmd = new Commands();
        GetLocator locator = new GetLocator();

        if (step.toLowerCase().contains("deselect")) {
            /**
             * 'all' identify.
             * Step : Deselect all from @element
             */
            if (step.toLowerCase().contains("all")) {
                cmd.deselectAll(cmd.findElement(driver, locator.getLocatorValue(test.get("suiteName").toString(), parseElementName(step))));
            }
            /**
             * 'text' identify.
             * Step : Deselect using text 'Text' from @element
             */
            else if (step.toLowerCase().contains("text")) {
                cmd.deselectText(cmd.findElement(driver, locator.getLocatorValue(test.get("suiteName").toString(), parseElementName(step))), parseTextToEnter(test,step));
            }
            /**
             * 'index' identify.
             * Step : Deselect using index 1 from @element
             */
            else if (step.toLowerCase().contains("index")) {
                cmd.deselectIndex(cmd.findElement(driver, locator.getLocatorValue(test.get("suiteName").toString(), parseElementName(step))), Integer.parseInt(parseNumverToEnter(step, 0)));
            }
            /**
             * 'value' identify.
             * Step : Deselect using value 'Text' from @element
             */
            else if (step.toLowerCase().contains("value")) {
                cmd.deselectValue(cmd.findElement(driver, locator.getLocatorValue(test.get("suiteName").toString(), parseElementName(step))), parseTextToEnter(test,step));
            }
        } else {
            /**
             * 'text' identify.
             * Step : Select using text 'Text' from @element
             */
            if (step.toLowerCase().contains("text")) {
                cmd.selectText(cmd.findElement(driver, locator.getLocatorValue(test.get("suiteName").toString(), parseElementName(step))), parseTextToEnter(test,step));
            }
            /**
             * 'index' identify.
             * Step : Select using index 1 from @element
             */
            else if (step.toLowerCase().contains("index")) {
                cmd.selectIndex(cmd.findElement(driver, locator.getLocatorValue(test.get("suiteName").toString(), parseElementName(step))), Integer.parseInt(parseNumverToEnter(step, 0)));
            }
            /**
             * 'value' identify.
             * Step : Select using value 'Text' form @element
             */
            else if (step.toLowerCase().contains("value")) {
                cmd.selectValue(cmd.findElement(driver, locator.getLocatorValue(test.get("suiteName").toString(), parseElementName(step))), parseTextToEnter(test,step));
            }
        }
    }

    public String parseElementName(String step) {

        String[] stepWordList = step.split(":|\\s+");

        String elementName = "";

        for (String word : stepWordList) {
            if (word.contains("@")) {
                elementName = word.substring(1);
            }

        }
        return elementName;
    }

    /**
     * @auther :
     * @lastModifiedBy: Ankit Mistry
     * @param test
     * @param step
     * @return
     */
    public String parseTextToEnter(JSONObject test, String step) {
        String textToEnter = "";

        int startPoint = 0;
        int endPoint = 0;

        if (step.contains("{") && step.contains("}")) {
            startPoint = step.indexOf("{") + 1;
            endPoint = step.lastIndexOf("}");
            String headerName = step.substring(startPoint, endPoint);
            try {
                if(test.get("dataType").toString().equalsIgnoreCase("excel")) {
                    try {

                        textToEnter = dataDrivenParser.getcellValuefromExcel(dataDrivenParser.getExcelUrl(test.get("suiteName").toString(), test.get("dataSetName").toString()), headerName, (Integer) test.get("row"));
                        logger.stepLog(step.replace(headerName, textToEnter));

                    } catch (StringIndexOutOfBoundsException e) {
                        logger.stepLog(step);
                        logger.testFailed("no string to enter. Create a separate exeception here");
                    }
                }
            }
            catch (Exception e){}
            try {
                if(test.get("dataType").toString().equalsIgnoreCase("global")){
                    textToEnter = dataDrivenParser.getGlobalDataValue(test.get("suiteName").toString(), test.get("dataSetName").toString(),headerName);
                    logger.stepLog(step.replace(headerName, textToEnter));

                }
            }
            catch (Exception e){
                throw new TesboException("Key name " + headerName + " is not found in " + test.get("dataSetName").toString() + " data set");
            }
        } else {
            startPoint = step.indexOf("'") + 1;
            endPoint = step.lastIndexOf("'");
            try {
                logger.stepLog(step);
                textToEnter = step.substring(startPoint, endPoint);

            } catch (StringIndexOutOfBoundsException e) {
                throw new TesboException("No string found to enter on element.");
            }
        }
            return textToEnter;

    }



    public String parseNumverToEnter(String step, int index) {
        String numbers;

        //extracting string
        numbers = step.replaceAll("[^-?0-9]+", " ");

        return Arrays.asList(numbers.trim().split(" ")).get(index);
    }

    public String screenshot(WebDriver driver, String suitName, String testName){
        generateReportDir();
        String screenshotName = captureScreen(driver,suitName,testName);
        return screenshotName;
    }

    public void generateReportDir() {
        File htmlReportMainDir = new File("./screenshots");

        if (!htmlReportMainDir.exists()) {
            htmlReportMainDir.mkdir();
        }
    }

    public String captureScreen(WebDriver driver, String suitName, String testName){
        String path;
        try {
            File filePath = new File("screenshots");
            WebDriver augmentedDriver = new Augmenter().augment(driver);
            File scrFile = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
            path = filePath.getAbsolutePath()+"/" + (suitName.split(".s"))[0] +"_"+testName.replaceAll("\\s", "")+"_"+dtf.format(LocalDateTime.now())+".png";
            FileUtils.copyFile(scrFile, new File(path));
        }
        catch(IOException e) {
            path = "Failed to capture screenshot: " + e.getMessage();
        }
        return path;
    }
}
