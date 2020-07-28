package framework;

import randomlibrary.RandLibrary;
import selenium.Commands;
import logger.TesboLogger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

public class StepParser {

    private static final Logger log = LogManager.getLogger(StepParser.class);
    TesboLogger tesboLogger = new TesboLogger();
    DataDrivenParser dataDrivenParser = new DataDrivenParser();
    ReportParser reportParser=new ReportParser();
    Commands cmd = new Commands();
    GetLocator locator = new GetLocator();
    CommonMethods commonMethods=new CommonMethods();
    RandLibrary randLibrary = new RandLibrary();

    static String screenShotURL=null;
    String stepText="Step: ";
    String noStringFoundMsg="No string found to enter.";
    String testsFileNameText="testsFileName";
    String testNameText="testName";
    String enterText="enter";
    String browserText="browser";
    String regexText=":|\\s+";

    public String parseStep(WebDriver driver, JSONObject test, String step) throws IOException {

        String pauseText="pause";
        String andClickText="and click";
        String scrollText="scroll";
        String testsFileName=test.get(testsFileNameText).toString();
        String testName=test.get(testNameText).toString();

        printStepDetails(step);

        //Clicks

        if(step.toLowerCase().contains("click") && !(step.toLowerCase().contains(pauseText) && step.toLowerCase().contains(andClickText)) && !(step.toLowerCase().contains(scrollText) && step.toLowerCase().contains(andClickText)) && !(step.toLowerCase().contains("right") || step.toLowerCase().contains("double") || step.toLowerCase().contains("and hold")) ) {

            clickFunctionality(step,driver, test,testsFileName);
        }
        else if(step.toLowerCase().contains("click") && step.toLowerCase().contains("and hold")) {
            // Click And Hold

            /**
             * Step: click and hold @element1
             * And
             * Step: click @element1 and hold
             */
            cmd.clickAndHold(driver,cmd.findElement(driver, locator.getLocatorValue(testsFileName, parseElementName(step))));
        }
        else if (step.toLowerCase().contains(pauseText) && step.toLowerCase().contains(andClickText)) {
            // pause and Click

            /**
             * Step: pause and click on @element
             * And
             * Step: pause to @element and click
             */
            cmd.pauseAndClick(driver,cmd.findElement(driver, locator.getLocatorValue(testsFileName, parseElementName(step))));
        }
        else if (step.toLowerCase().contains(scrollText) && step.toLowerCase().contains(andClickText)) {
            // scroll and Click

            /**
             * Step: scroll and click on @element1
             * And
             * Step: scroll to @element and click
             */
            cmd.scrollAndClick(driver,cmd.findElement(driver, locator.getLocatorValue(testsFileName, parseElementName(step))));
        }
        else if (step.toLowerCase().contains("right click")) {
            //Right Click

            cmd.rightClick(driver, cmd.findElement(driver, locator.getLocatorValue(testsFileName, parseElementName(step))));
        }
        else if (step.toLowerCase().contains("double click")) {
            //Double Click

            cmd.doubleClick(driver, cmd.findElement(driver, locator.getLocatorValue(testsFileName, parseElementName(step))));
        }
        else if (step.toLowerCase().contains("print")) {
            //Print

            /**
             * Step: print "User should redirect into the login page "
             * Step: print @elementName
             */
            String logStepMsg=printStep(driver,step,test);
            commonMethods.printStepInfo(logStepMsg,log);
        }
        else if (step.toLowerCase().contains("capture screenshot")) {
            //Capture Screenshot
            captureScreenshotFunctionality(step,driver,testName,testsFileName);
        }
        else if (step.toLowerCase().contains("clear cookies") || step.toLowerCase().contains("clear cache")) {
            //clear cookies and cache

            cmd.deleteAllCookies(driver);
        }
        else if (step.toLowerCase().contains("press")) {
            //Press Key

            pressKey(driver, test, step);
        }
        else if (step.toLowerCase().contains(enterText) && !(step.toLowerCase().contains("press") || step.toLowerCase().contains("switch"))) {
            //Sendkeys

            step=sendKyeFunctionality(step,driver,test,testsFileName);
        }
        else if((step.toLowerCase().contains("get ") || step.toLowerCase().contains("define "))){
            //Variables

            variableFunctionality(step,driver,test);
        }
        else if (step.toLowerCase().contains("upload") && step.toLowerCase().contains("file")) {
            //Upload File

            /**
            Step: Upload File 'filePath' @element
            */
            cmd.findElement(driver, locator.getLocatorValue(testsFileName, parseElementName(step))).sendKeys(parseTextToEnter(test, step));
        }
        else if (step.toLowerCase().contains("get page") && step.toLowerCase().contains("source")) {
            //Get Page Source

            /*
            Step: Get Page Source
            */
            cmd.getPageSource(driver);
        }
        else if (step.toLowerCase().contains("url")) {
            // Open URL

            /*
            Step: Open URL 'http://demo.guru99.com/test/upload/'
            */
            driver.get(parseTextToEnter(test, step));
        }
        else if (step.toLowerCase().contains("switch")) {
            //Switch

            switchFunction(driver, test, step);
        }
        else if (step.toLowerCase().contains("navigate") || (step.toLowerCase().contains("refresh") && step.toLowerCase().contains("page"))) {
            //navigate

            navigateFunction(driver, step);
        }
        else if (step.toLowerCase().contains(scrollText) && !(step.toLowerCase().contains(andClickText))) {
            //scroll

            scrollFunction(driver, testsFileName, step);
        }
        else if (step.toLowerCase().contains(pauseText) && !(step.toLowerCase().contains(andClickText))) {
            //pause

            pauseFunction(driver, testsFileName, step);
        }
        else if (step.toLowerCase().contains("select")) {
            //select

            selectFunction(driver, test, step);
        }
        else if (step.toLowerCase().contains("window") && !(step.toLowerCase().contains("close"))) {
            //Window Minimize, maximize and resize

            windowFunctionality(step,driver);
        }
        else if (step.toLowerCase().contains("mouse hover")) {
            //Mouse Hover

            /**
             * Step: Mouse Hover @element
             */
            cmd.mouseHover(driver,cmd.findElement(driver, locator.getLocatorValue(testsFileName, parseElementName(step))));
        }
        else if (step.toLowerCase().contains("drag") && step.toLowerCase().contains("and drop")) {
            //Drag and Drop

            /**
             * Step: Drag and Drop @Element_1 to @element_2 ,
             * Step: Drag and Drop @ele1 to ele2
             * and
             * Step: Drag @Element_1 to @Element2 and drop
             */
            // Not working
            dragAndDropElement(driver,test,step);
        }
        else if (step.toLowerCase().contains("clear") && !(step.toLowerCase().contains("cookies") || step.toLowerCase().contains("cache") || step.toLowerCase().contains(enterText))) {
            //Clear

            /**
            * Step: clear @ElementText
             */
            cmd.findElement(driver, locator.getLocatorValue(testsFileName, parseElementName(step))).clear();
        }
        else if (step.toLowerCase().contains("close window")) {
            //Close Window

            /**
             * Step: close window.
             */
            cmd.closeWindow(driver);
        }
        else if (step.toLowerCase().contains("close <") && step.toLowerCase().contains("> window"))
        {
            //Close window using index

            /**
             * Step: close <1> window
             * Step: close <1,4> window
             * Step: close <1 to 3> window
             */
            cmd.closeWindowByIndex(driver, step,test.get(browserText).toString());
        }

        tesboLogger.testPassed("Passed");
        return step;

    }

    public void printStepDetails(String step){
        if (!step.toLowerCase().contains("{") && !step.toLowerCase().contains("}") && !step.toLowerCase().contains("print") && !step.contains("random")) {
            if(step.contains("@")){
                String removeContent="";
                String[] stepsWord=step.split(" ");
                removeContent=removeContainsFromStep(stepsWord);
                if(removeContent.contains(".")){
                    String logStepMsg=step.replace("@"+removeContent,removeContent.split("\\.")[1]);
                    commonMethods.printStepInfo(logStepMsg,log);
                }
                else {
                    String logStepMsg=step.replace("@"+removeContent, removeContent);
                    commonMethods.printStepInfo(logStepMsg,log);
                }
            }
            else {
                String logStepMsg=step.replace("@", "");
                commonMethods.printStepInfo(logStepMsg,log);
            }
        }
    }

    public String removeContainsFromStep(String[] stepsWord){
        String removeContent="";
        for(String word:stepsWord){
            if(word.contains("@") && !(word.contains("'"))){
                removeContent= word.trim().replace("@","");
            }
        }
        return removeContent;
    }

    public void clickFunctionality(String step, WebDriver driver, JSONObject test,String testsFileName) {
        if (step.toLowerCase().contains("from list")) {
            clickOnElementFromList(driver,test,step);
        }
        else if(step.toLowerCase().contains("offset")){
            clickOnOffset(driver,test,step);
        }
        else {
            cmd.findElement(driver, locator.getLocatorValue(testsFileName, parseElementName(step))).click();
        }
    }

    public void captureScreenshotFunctionality(String step, WebDriver driver, String testName,String testsFileName) throws IOException {
        /**
         * Step: Capture Screenshot of @elementName
         */
        if(step.toLowerCase().contains("screenshot of") && step.toLowerCase().contains("@")) {
            synchronized (this) {
                screenShotURL = cmd.screenshotElement(driver, cmd.findElement(driver, locator.getLocatorValue(testsFileName, parseElementName(step))), testsFileName, testName);
            }
            commonMethods.printStepInfo("Screenshot: " + screenShotURL,log);
        }
        /**
         * Step: capture screenshot
         */
        else if(step.toLowerCase().contains("screenshot")) {
            synchronized (this) {
                screenShotURL = cmd.captureScreenshot(driver, testsFileName, testName);
            }
            commonMethods.printStepInfo("Screenshot: " + screenShotURL,log);
        }
    }

    public void variableFunctionality(String step,WebDriver driver,JSONObject test) {
        if(step.toLowerCase().contains("define ") || step.toLowerCase().contains(" set ") || step.toLowerCase().contains(" put ") || step.toLowerCase().contains(" assign ")) {
            /**
             Step: Get text of @element and set / put / assign in to {DataSet variable}
             Step: Get size of @element and set / put / assign in to {DataSet variable}
             Step: Get list of @element text and set / put / assign in to {DataSet variable}
             Step: Get page title and set / put / assign in to {DataSet variable}
             Step: Get current url and set / put / assign in to {DataSet variable}
             Step: Get attribute 'attribute name' of @element and set / put / assign in to {DataSet variable}
             Step: Get css value 'css value' of @element and set / put / assign in to {DataSet variable}

             Define local variable
             Step: define {local Variable} and set / put / assign 'value'
             Step: define {local Variable}
             Step: Get text of @element and set / put / assign in to {local variable}
             */

            dataDrivenParser.setValueInDataSetVariable(driver, test, step);

            String printStep = reportParser.dataSetStepReplaceValue(test, step);
            commonMethods.printStepInfo(printStep,log);
        }
    }

    public String sendKyeFunctionality(String step,WebDriver driver,JSONObject test,String testsFileName) {
        if (step.toLowerCase().contains("random")) {step= randomStepParse(driver,test,step); }
        else {
            if(step.toLowerCase().contains("clear")){
                cmd.findElement(driver, locator.getLocatorValue(testsFileName, parseElementName(step))).clear();
            }
            cmd.findElement(driver, locator.getLocatorValue(testsFileName, parseElementName(step))).sendKeys(parseTextToEnter(test, step));
        }
        return step;
    }

    public void windowFunctionality(String step,WebDriver driver){
        if (step.toLowerCase().contains("resize")) {
            /**
             * Step: Window Resize (x, y)
             */
            windowResize(step,driver);
        }
        if (step.toLowerCase().contains("minimize")) {
            /**
             * Step: Window Minimize
             */
            windowMinimize(driver);
        }
        if (step.toLowerCase().contains("maximize")) {
            /**
             * Step: Window Maximize
             */
            windowMaximize(driver);
        }
    }

    public String randomStepParse(WebDriver driver, JSONObject test, String step) {


        String testsFileName=test.get(testsFileNameText).toString();

        String textToEnter="";

        textToEnter=randomUserDetails(step);

        if(textToEnter.equals("")){
            textToEnter=randomUserAddressDetails(step);
        }
        if(textToEnter.equals("")){
            textToEnter=randomUserBankDetails(step);
        }
        if(textToEnter.equals("")){
            textToEnter=randomCommonDetails(step);
        }

        String stepTexts="";
        stepTexts=enterTextForRandomFunctionality(textToEnter,step,driver,testsFileName);

        return getRandomStepForReturn(step,stepTexts,textToEnter);
    }

    public String randomUserDetails(String step){
        String textToEnter="";
        if (step.toLowerCase().contains("firstname")) {textToEnter=randLibrary.firstName(); }

        else if (step.toLowerCase().contains("email")) {textToEnter=randLibrary.eMail(); }

        else if (step.toLowerCase().contains("username")){textToEnter=randLibrary.userName(); }

        else if (step.toLowerCase().contains("mobilenumber")){textToEnter=randLibrary.number(); }

        else if (step.toLowerCase().contains("lastname")){textToEnter=randLibrary.lastName(); }

        else if (step.toLowerCase().contains("age")){textToEnter=randLibrary.ageAdult(); }

        else if (step.toLowerCase().contains("birthday")){textToEnter= randLibrary.birthday(); }

        else if (step.toLowerCase().contains("gender")){textToEnter=randLibrary.gender(); }

        else if (step.toLowerCase().contains("fullname")){textToEnter=randLibrary.fullName(); }

        else if (step.toLowerCase().contains("password")){textToEnter=randLibrary.password(); }

        else if (step.toLowerCase().contains("passport")){textToEnter=randLibrary.passport(); }

        else if (step.toLowerCase().contains("maritalstatus")){textToEnter=randLibrary.maritalStatus(); }


        return textToEnter;
    }

    public String randomUserAddressDetails(String step){
        String textToEnter="";
        if (step.toLowerCase().contains("country")){textToEnter=randLibrary.country(); }

        else if (step.toLowerCase().contains("city")){textToEnter=randLibrary.city(); }

        else if (step.toLowerCase().contains("postcode")){textToEnter=randLibrary.postcode(); }

        else if (step.toLowerCase().contains("street")){textToEnter=randLibrary.street(); }

        else if (step.toLowerCase().contains("state")){textToEnter=randLibrary.state(); }

        else if (step.toLowerCase().contains("fulladdress")){textToEnter=randLibrary.fullAddress(); }

        else if (step.toLowerCase().contains("houseno")){textToEnter=randLibrary.houseNo(); }


        return textToEnter;
    }

    public String randomUserBankDetails(String step){
        String textToEnter="";
        if (step.toLowerCase().contains("debitcard")){textToEnter=randLibrary.debitCardNo(); }

        else if (step.toLowerCase().contains("expirydate")){textToEnter=randLibrary.expiryDate(); }

        else if (step.toLowerCase().contains("cvv")){textToEnter=randLibrary.cvvNo(); }

        else if (step.toLowerCase().contains("bankacno")){textToEnter=randLibrary.bankACNo(); }

        else if (step.toLowerCase().contains("cardtype")){textToEnter=randLibrary.cardname(); }

        return textToEnter;
    }

    public String randomCommonDetails(String step){
        String textToEnter="";
        if (step.toLowerCase().contains("emoji")){textToEnter=randLibrary.emoji(); }

        else if (step.toLowerCase().contains("lorem")){textToEnter=randLibrary.lorem(); }

        else if (step.toLowerCase().contains("domain")){textToEnter=randLibrary.internetDomain(); }

        else if (step.toLowerCase().contains("gstno")){textToEnter=randLibrary.gstNo(); }

        else if (step.toLowerCase().contains("panno")){textToEnter=randLibrary.panNo(); }

        else if (step.toLowerCase().contains("companyname")){textToEnter=randLibrary.companyName(); }

        else if (step.toLowerCase().contains("idno")){textToEnter=randLibrary.idNo(); }

        else if (step.toLowerCase().contains("mail.mailinator")) {textToEnter=randLibrary.randomEmailWithMailinator(); }

        else if (step.toLowerCase().contains("mail.yopmail")) {textToEnter=randLibrary.randomEmailWithYopmail(); }

        return textToEnter;
    }

    public String getRandomStepForReturn(String step,String stepTexts,String textToEnter){
        if(!stepTexts.equals("")) {
            return step.replace(stepTexts, textToEnter);
        }
        else {
            return step;
        }
    }

    public String enterTextForRandomFunctionality(String textToEnter,String step, WebDriver driver,String testsFileName){
        String stepTexts="";
        if(!textToEnter.equals("")){
            int startPoint = 0;
            int endPoint = 0;
            startPoint = step.indexOf('\'') + 1;
            endPoint = step.lastIndexOf('\'');
            stepTexts = step.substring(startPoint, endPoint);
            String logText=step.replace(stepTexts, textToEnter).replace("@","");
            commonMethods.printStepInfo(logText,log);
            if (step.toLowerCase().contains("birthday")){
                cmd.findElement(driver, locator.getLocatorValue(testsFileName, parseElementName(step))).sendKeys((CharSequence) textToEnter);
            }
            else {
                cmd.findElement(driver, locator.getLocatorValue(testsFileName, parseElementName(step))).sendKeys(textToEnter);
            }
        }
        return stepTexts;
    }


    public void switchFunction(WebDriver driver, JSONObject test, String step) {


        String testsFileName=test.get(testsFileNameText).toString();

        try {
            //Step :  switch to active Element
            if (step.toLowerCase().contains("active element")) {
                cmd.switchToActiveElement(driver);
            }

            /**
             * Switch to alert.
             */
            if (step.toLowerCase().contains("alert")) {
                switchToAlertFunctionality(driver,step,test);
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
                switchToFrameFunctionality(driver,step,test,testsFileName);
            }

            /**
             * Switch to window.
             */
            if (step.toLowerCase().contains("window")) {
                switchToWindowFunctionality(driver,step,test);
            }

        } catch (Exception e) {
            log.error("Step Failed");
            tesboLogger.testFailed("Step Failed");
            throw e;
        }
    }

    public void switchToAlertFunctionality(WebDriver driver,String step,JSONObject test){
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
                assertThat(alertText).containsIgnoringCase(parseTextToEnter(test, step));
            }
            /**
             * enter identify.
             * Step : Switch to alert then enter 'Text'.
             */

            else if (step.toLowerCase().contains(enterText)) {
                try {
                    cmd.switchAlertSendKey(driver, parseTextToEnter(test, step));
                }catch (Exception e){log.error(e.getMessage());}
            }
    }

    public void switchToFrameFunctionality(WebDriver driver,String step,JSONObject test,String testsFileName){

        if (step.toLowerCase().contains("using")) {
            //Step: Switch to frame using id 'FrameID'.
            //Step: Switch to frame using name 'FrameName'.

            if (step.toLowerCase().contains("id") || step.toLowerCase().contains("name")) {
                cmd.switchFrame(driver, parseTextToEnter(test, step));
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
                cmd.switchFrameElement(driver, cmd.findElement(driver, locator.getLocatorValue(testsFileName, parseElementName(step))));
            } catch (NullPointerException e) {
                log.info("No element found.");
                tesboLogger.errorLog("No element found.");
                throw e;
            }
        }
    }

    public void switchToWindowFunctionality(WebDriver driver,String step,JSONObject test){
        /**
         * Step : Switch to new window.
         */
        if (step.toLowerCase().contains("new")) {
            cmd.switchNewWindow(driver,test.get(browserText).toString().toLowerCase());
        }
        /**
         * Step : Switch to main window.
         * Step : Switch to parent window.
         */
        else if (step.toLowerCase().contains("main") || step.toLowerCase().contains("parent")) {
            cmd.switchMainWindow(driver);
        }

    }

    public void navigateFunction(WebDriver driver, String step) {
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
         * Step : Page Refresh
         * Step : Refresh Page
         */
        else if (step.toLowerCase().contains("refresh") && step.toLowerCase().contains("page")) {
            cmd.navigateRefresh(driver);
        }
    }

    public void scrollFunction(WebDriver driver, String testsFileName, String step) {


        /**
         * 'Bottom' identify.
         * Step: Scroll to bottom.
         */
        if (step.toLowerCase().contains("bottom")) {
            cmd.scrollBottom(driver);
        }
        /**
         * 'top' identify.
         * Step: Scroll to top.
         */
        else if (step.toLowerCase().contains("top")) {
            cmd.scrollTop(driver);
        }
        /**
         * 'top' identify.
         * Step: Scroll to Horizontal
         */
        else if (step.toLowerCase().contains("horizontal")) {
            cmd.scrollHorizontal(driver);
        }
        /**
         * number identify.
         * Step: Scroll to coordinate (50,100)
         */
        else if (step.toLowerCase().contains("coordinate")) {
            try {
                String x = parseNumverToEnter(step, 0);
                String y = parseNumverToEnter(step, 1);
                cmd.scrollToCoordinate(driver, x, y);
            } catch (NullPointerException e) {
                log.error("No coordinate found");
                tesboLogger.testFailed("No coordinate found");
            }
        }
        /**
         * element identify.
         * Step: Scroll to @element
         */
        else if (parseElementName(step).equals("")) {
            try {

                cmd.scrollToElement(driver, cmd.findElement(driver, locator.getLocatorValue(testsFileName, parseElementName(step))));
            } catch (NullPointerException e) {
                log.error("No element found");
                tesboLogger.testFailed("No element found");
                throw e;
            } catch (Exception e) {
                throw e;
            }
        }
    }

    public void pauseFunction(WebDriver driver, String testsFileName, String step) {


        /**
         * 'disappear' identify.
         * Step : pause until @element is disappear
         */
        if (step.toLowerCase().contains("disappear")) {
            try {
                cmd.pauseElementDisappear(driver, cmd.findElement(driver, locator.getLocatorValue(testsFileName, parseElementName(step))));
            } catch (Exception e) {
                log.error("");
            }
        }
        /**
         * 'clickable' identify.
         * Step : pause until element is clickable
         */
        else if (step.toLowerCase().contains("clickable")) {
            try {
                cmd.pauseElementClickable(driver, cmd.findElement(driver, locator.getLocatorValue(testsFileName, parseElementName(step))));
            } catch (Exception e) {
                log.error("");
            }
        }
        /**
         * 'display' identify.
         * Step : pause until @Submit_Btn is display
         */
        else if (step.toLowerCase().contains("display")) {
            try {
                cmd.pauseElementDisplay(driver, cmd.findElement(driver, locator.getLocatorValue(testsFileName, parseElementName(step))));
            } catch (Exception e) {log.error(""); }
        }
        /**
         * 'sec' identify.
         * Step : pause for 5sec
         */
        else if (step.toLowerCase().contains("sec")) {
            cmd.pause(Integer.parseInt(parseNumverToEnter(step, 0)));
        }
    }

    public void selectFunction(WebDriver driver, JSONObject test, String step) {

        String testsFileName=test.get(testsFileNameText).toString();

        if (step.toLowerCase().contains("deselect")) {
            /**
             * 'all' identify.
             * Step : Deselect all from @element
             */
            if (step.toLowerCase().contains("all")) {
                cmd.deselectAll(cmd.findElement(driver, locator.getLocatorValue(testsFileName, parseElementName(step))));
            }
            /**
             * 'text' identify.
             * Step : Deselect using text 'Text' from @element
             */
            else if (step.toLowerCase().contains("text")) {
                cmd.deselectText(cmd.findElement(driver, locator.getLocatorValue(testsFileName, parseElementName(step))), parseTextToEnter(test, step));
            }
            /**
             * 'index' identify.
             * Step : Deselect using index 1 from @element
             */
            else if (step.toLowerCase().contains("index")) {
                cmd.deselectIndex(cmd.findElement(driver, locator.getLocatorValue(testsFileName, parseElementName(step))), Integer.parseInt(parseNumverToEnter(step, 0)));
            }
            /**
             * 'value' identify.
             * Step : Deselect using value 'Text' from @element
             */
            else if (step.toLowerCase().contains("value")) {
                cmd.deselectValue(cmd.findElement(driver, locator.getLocatorValue(testsFileName, parseElementName(step))), parseTextToEnter(test, step));
            }
        } else {
            /**
             * 'text' identify.
             * Step : Select using text 'Text' from @element
             */
            if (step.toLowerCase().contains("text")) {
                cmd.selectText(cmd.findElement(driver, locator.getLocatorValue(testsFileName, parseElementName(step))), parseTextToEnter(test, step));
            }
            /**
             * 'index' identify.
             * Step : Select using index 1 from @element
             */
            else if (step.toLowerCase().contains("index")) {
                cmd.selectIndex(cmd.findElement(driver, locator.getLocatorValue(testsFileName, parseElementName(step))), Integer.parseInt(parseNumverToEnter(step, 0)));
            }
            /**
             * 'value' identify.
             * Step : Select using value 'Text' form @element
             */
            else if (step.toLowerCase().contains("value")) {
                cmd.selectValue(cmd.findElement(driver, locator.getLocatorValue(testsFileName, parseElementName(step))), parseTextToEnter(test, step));
            }
        }
    }

    public String parseElementName(String step) {

        String[] stepWordList = step.split(regexText);

        String elementName = "";

        for (String word : stepWordList) {
            if (word.contains("@")) {
                elementName = word.substring(1);
            }
        }
        return elementName;
    }

    /**
     * @param test
     * @param step
     * @return
     * @auther :
     * @lastModifiedBy: Ankit Mistry
     */
    public String parseTextToEnter(JSONObject test, String step) {
        String textToEnter = "";
        String replaceAllString="[{,}]";
        Object dataType=test.get("dataType");
        Object dataSetName=test.get("dataSetName");

        int startPoint = 0;
        int endPoint = 0;
        String testsFileName=test.get(testsFileNameText).toString();

        if (step.contains("{") && step.contains("}")
                && !step.toLowerCase().contains(" define ") && !step.toLowerCase().contains(" attribute ") && !step.toLowerCase().contains(" css value ")) {
            startPoint = step.indexOf('{') + 1;
            endPoint = step.lastIndexOf('}');
            String headerName = step.substring(startPoint, endPoint);
            boolean isDetaSet=false;
            if(TestExecutor.localVariable.containsKey(headerName)){
                return getParseTextToEnterWhenStepHasLocalVariable(headerName,step,replaceAllString);
            }
            else {
                try {
                    if (headerName.split("\\.").length==3) {
                        isDetaSet = true;
                        textToEnter = getParseTextToEnterWhenStepHasInLineDataSet(headerName,testsFileName,step,replaceAllString,"Text");
                    }
                } catch (Exception e) {
                    StringWriter sw = new StringWriter();
                    e.printStackTrace(new PrintWriter(sw));
                    tesboLogger.testFailed(sw.toString());
                    log.error(sw.toString());
                    throw e;
                }

                if (!isDetaSet) {
                    textToEnter =getParseTextToEnterWhenStepHasDataSet(dataType.toString(),headerName,testsFileName,step,replaceAllString,test,dataSetName.toString());
                }
            }
        }
        else {
            textToEnter=getParseTextToEnterWhenSimpleStep(step);
        }

        return textToEnter;
    }

    public String getParseTextToEnterWhenSimpleStep(String step){
        String textToEnter = "";
        int startPoint = step.indexOf('\'') + 1;
        int endPoint = step.lastIndexOf('\'');
        try {
            textToEnter = step.substring(startPoint, endPoint);
        } catch (StringIndexOutOfBoundsException e) {
            commonMethods.throwTesboException(noStringFoundMsg,log);
        }
        return textToEnter;
    }

    public String getParseTextToEnterWhenStepHasLocalVariable(String headerName,String step,String replaceAllString){
        String textToEnter = "";
        textToEnter = TestExecutor.localVariable.get(headerName).toString();
        String newStep=step.replace( headerName, textToEnter).replaceAll(replaceAllString, "'").replace("@", "");
        commonMethods.printStepInfo(newStep,log);
        return textToEnter;
    }

    public String getParseTextToEnterWhenStepHasDataSet(String dataType,String headerName,String testsFileName,String step,String replaceAllString,JSONObject test,String dataSetName){
        String textToEnter = "";
        try {
            if (dataType.equalsIgnoreCase("excel")) {
                textToEnter = getParseTextToEnterWhenStepHasExcelDataSet(headerName,testsFileName,step,replaceAllString,test,dataSetName,"Text");
            }
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            tesboLogger.testFailed(sw.toString());
            log.error(sw.toString());
        }
        if (dataType.equalsIgnoreCase("global")) {
            textToEnter = getParseTextToEnterWhenStepHasGlobalDataSet(headerName, testsFileName, step, replaceAllString, dataSetName);
        }
        if(dataType.equalsIgnoreCase("list")){
            textToEnter=getParseTextToEnterWhenStepHasListDataSet(headerName,step,replaceAllString,dataSetName,test);
        }
        return textToEnter;
    }

    public String getParseTextToEnterWhenStepHasInLineDataSet(String headerName,String testsFileName,String step,String replaceAllString,String use){
        String textToEnter = "";
        String[] dataSet = headerName.split("\\.");
        if (dataSet.length == 3) {
            textToEnter = dataDrivenParser.getGlobalDataValue(testsFileName, dataSet[0], dataSet[1], dataSet[2]).get(dataSet[2]).toString();
            if(use.equals("Text")) {
                step = step.replace("@", "");
                commonMethods.printStepInfo(step.replace(headerName, textToEnter).replaceAll(replaceAllString, "'"),log);
            }
        } else {
            commonMethods.throwTesboException("Please enter DataSet in: '" + step + "'",log);
        }

        return textToEnter;
    }

    public String getParseTextToEnterWhenStepHasExcelDataSet(String headerName,String testsFileName,String step,String replaceAllString,JSONObject test,String dataSetName,String use){
        String textToEnter = "";
        try {
            textToEnter = dataDrivenParser.getcellValuefromExcel(dataDrivenParser.getExcelUrl(dataSetName), headerName, (Integer) test.get("row"), Integer.parseInt(dataDrivenParser.sheetNumber(testsFileName, test.get(testNameText).toString())));
            if (textToEnter != null && use.equals("Text")) {
                step=step.replace("@","");
                commonMethods.printStepInfo(step.replace( "{"+headerName+"}", "{"+textToEnter+"}").replaceAll(replaceAllString, "'"),log);
            }

        } catch (StringIndexOutOfBoundsException e) {
            commonMethods.printStepInfo(step,log);
            tesboLogger.testFailed("no string to enter. Create a separate exeception here");
            log.error("no string to enter. Create a separate exeception here");
        }
        return textToEnter;
    }

    public String getParseTextToEnterWhenStepHasGlobalDataSet(String headerName,String testsFileName,String step,String replaceAllString,String dataSetName){
        String textToEnter = "";
        try {
            textToEnter = dataDrivenParser.getGlobalDataValue(testsFileName,null, dataSetName, headerName).get(headerName).toString();
            step=step.replace("@","");
            commonMethods.printStepInfo(step.replace(headerName, textToEnter).replaceAll(replaceAllString, "'"),log);
        } catch (Exception e) {
            commonMethods.throwTesboException("Key name " + headerName + " is not found in " + dataSetName + " data set",log);
        }
        return textToEnter;
    }

    public String getParseTextToEnterWhenStepHasListDataSet(String headerName,String step,String replaceAllString,String dataSetName,JSONObject test){
        String textToEnter = "";
        textToEnter=dataDrivenParser.getDataSetListValue(dataSetName, headerName,Integer.parseInt(test.get("row").toString()));
        step=step.replace("@","");
        commonMethods.printStepInfo(step.replace(headerName, textToEnter).replaceAll(replaceAllString, "'"),log);
        return textToEnter;
    }


    /**
     * @param test
     * @param step
     * @return
     * @auther :
     * @lastModifiedBy: Ankit Mistry
     */
    public String passArgsToCode(JSONObject test, String step) {
        String textToEnter = "";
        String dataType=test.get("dataType").toString();
        int startPoint = 0;
        int endPoint = 0;
        String testsFileName=test.get(testsFileNameText).toString();
        String dataSetName=test.get("dataSetName").toString();

        if (step.contains("{") && step.contains("}")) {
            startPoint = step.indexOf('{') + 1;
            endPoint = step.lastIndexOf('}');
            String headerName = step.substring(startPoint, endPoint);
            boolean isDataSet=false;
            try {
                if (headerName.split("\\.").length==3) {
                    isDataSet=true;
                    textToEnter = getParseTextToEnterWhenStepHasInLineDataSet(headerName,testsFileName,step,"","ARG");

                }
            } catch (Exception e) {
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                tesboLogger.testFailed(sw.toString());
                log.error(sw.toString());
                throw e;
            }

            if(!isDataSet) {
                textToEnter=passArgsToCodeWhenStepHasDataSet(dataType,headerName,testsFileName,step,dataSetName,test);
            }
        } else {
            textToEnter=getParseTextToEnterWhenSimpleStep(step);
        }

        return textToEnter;
    }

    public String passArgsToCodeWhenStepHasDataSet(String dataType,String headerName,String testsFileName,String step,String dataSetName,JSONObject test){
        String textToEnter = "";
        try {
            if (dataType.equalsIgnoreCase("excel")) {
                textToEnter = getParseTextToEnterWhenStepHasExcelDataSet(headerName,testsFileName,step,"",test,dataSetName,"ARG");
            }
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            tesboLogger.testFailed(sw.toString());
            log.error(sw.toString());
        }
        try {
            if (dataType.equalsIgnoreCase("global")) {
                textToEnter = dataDrivenParser.getGlobalDataValue(testsFileName, null, dataSetName, headerName).get(headerName).toString();
            }
        } catch (Exception e) {
            commonMethods.throwTesboException("Key name " + headerName + " is not found in " + dataSetName + " data set",log);
        }
        if(dataType.equalsIgnoreCase("list")){
            textToEnter=dataDrivenParser.getDataSetListValue(dataSetName, headerName,Integer.parseInt(test.get("row").toString()));
        }
        return  textToEnter;
    }

    /**
     * @param test
     * @param steps
     * @return
     * @auther :
     * @lastModifiedBy: Ankit Mistry
     */
    public String replaceArgsOfCodeStep(JSONObject test, String steps) {

        String tagVal=null;
        String[] arguments=null;
        String[] args=null;
        String step=steps;
        try {
            tagVal=steps.split(":")[1].trim();
            if(tagVal.split("\\(").length>1 && tagVal.contains("{") && tagVal.contains("}")) {
                args = tagVal.split("\\(")[1].trim().replaceAll("[()]", "").split(",");
                arguments = getValueToReplaceArgsOfCodeStep(args,test);
            }
        } catch (Exception e) {
            commonMethods.throwTesboException("Code step has no value",log);
        }
        if(arguments!=null) {
            for (int i = 0; i < arguments.length; i++) {
                step = step.replace(args[i], arguments[i]);
            }
        }
        return step;
    }

    public String[] getValueToReplaceArgsOfCodeStep(String[] args,JSONObject test){
        String[] arguments=null;
        StringBuilder argument=new StringBuilder();
        int i=0;
        for(String arg:args){
            if (arg.contains("{") && arg.contains("}")) {
                if(i==0){argument.append(passArgsToCode(test,arg));i++;}
                else {argument.append(","+passArgsToCode(test,arg));i++;}
            }
            else{
                if(i==0) {argument.append(arg);i++;}
                else{argument.append( ","+arg);i++;}
            }
        }
        if(argument.length() !=0) {
            arguments = argument.toString().split(",");
        }
        return arguments;
    }

    public String parseNumverToEnter(String step, int index) {
        String numbers;

        //extracting string
        numbers = step.replaceAll("[^-?0-9]+", " ");
        if(numbers.trim().equals("")){
            commonMethods.throwTesboException("Seconds to pause is not found in '"+step+"'",log);
        }
        try {
            return Arrays.asList(numbers.trim().split(" ")).get(index);
        }catch (Exception e){
            commonMethods.throwTesboException("Please add coordinate value (X, Y) in step '"+step+"'",log);
        }
        return null;
    }

    public void generateReportDir() {
        File htmlReportMainDir = new File("./screenshots");

        if (!htmlReportMainDir.exists()) {
            htmlReportMainDir.mkdir();
        }
    }


    public void pressKey(WebDriver driver, JSONObject test, String step) {
        String testsFileName=test.get(testsFileNameText).toString();

        Actions actions = new Actions(driver);
        if(step.toLowerCase().contains(enterText)) {
            pressEnterKey(step,driver,testsFileName);

        } else if (step.toLowerCase().contains("tab")) {
            actions.sendKeys(Keys.TAB).build().perform();
        } else if (step.toLowerCase().contains("plus")) {
            pressCtrlPlus(step,driver,testsFileName);

        } else {
            commonMethods.throwTesboException("Please enter valid step.",log);
        }
    }

    public void pressEnterKey(String step,WebDriver driver,String testsFileName){
        Actions actions = new Actions(driver);
        if(step.toLowerCase().contains("@")) {
            cmd.findElement(driver, locator.getLocatorValue(testsFileName, parseElementName(step))).sendKeys(Keys.ENTER);
        } else {
            actions.sendKeys(Keys.ENTER).build().perform();
        }
    }

    public void pressCtrlPlus(String step,WebDriver driver,String testsFileName){
        String[] steps = step.split(" ");
        boolean flag=false;
        for (int i = 0; i < steps.length; i++) {

            if(steps[i].equalsIgnoreCase("'ctrl'")) {
                flag=true;
                if(!(steps[i + 2].replace("'", "").equalsIgnoreCase("")) &&  steps[i + 2].contains("'")
                        && ( steps[i + 2].replace("'", "").equalsIgnoreCase("a") || steps[i + 2].replace("'", "").equalsIgnoreCase("c") || steps[i + 2].replace("'", "").equalsIgnoreCase("v"))) {
                    cmd.findElement(driver, locator.getLocatorValue(testsFileName, parseElementName(step))).sendKeys(Keys.chord(Keys.CONTROL, steps[i + 2].replace("'", "").toLowerCase()));
                }
                else {
                    commonMethods.throwTesboException("Please enter valid key",log);
                }
            }
        }
        if(!flag){
            commonMethods.throwTesboException("Please enter valid step.",log);
        }
    }

    /**
     * @param step
     * @return modified step that will looks good in report
     * @author Viral Patel
     */

    public String stepModifierForReport(String step) {

        String finalstep = removeStepKeywordFromStep(step);

        //enter 'viral@gmail.com' on @ElementName

        if (removeStepKeywordFromStep(step).contains("@")) {

            String subString = "";
            if (finalstep.contains("'")) {
                String replacedStep = "";
                subString = finalstep.substring(step.indexOf('\'') + 1, step.lastIndexOf('\''));
                if (finalstep.contains(subString)) {
                    replacedStep = finalstep.replace(subString, "SubString");
                }
                finalstep = (replacedStep.replace("@", "")).replace("SubString", subString);
            } else {
                finalstep = step.replace("@", "");
            }

        }

        return finalstep;
    }

    /**
     * @param step
     * @return return sentences that will remove step : or step: keyword
     * @author Viral Patel
     */
    public String removeStepKeywordFromStep(String step) {
        String finalStep = "";
        if (step.startsWith(stepText)) {
            finalStep = step.split("Step:")[1];
        }
        return finalStep;
    }

    /**
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     * @param step
     * @return
     *
     */
    public String getCollectionName(String step) {
        String textToEnter = null;
        try {
            textToEnter = step.split(":")[1].trim();
        } catch (Exception e) {
            commonMethods.throwTesboException("Pleas enter collection name",log);
        }
        return textToEnter;
    }

    /**
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     * @param step
     * @param driver
     */
    public void windowResize(String step,WebDriver driver) {
        String[] size = null;
        try {
            size = step.split("\\(")[1].trim().replaceAll("\\)","").split(",");
            Dimension dimension = new Dimension(Integer.parseInt(size[0]), Integer.parseInt(size[1]));
            driver.manage().window().setSize(dimension);
        } catch (Exception e) {
            commonMethods.throwTesboException("Pleas enter 'X' and 'Y' dimension for window resize",log);
        }
    }

    /**
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     * @param driver
     */
    public void windowMinimize(WebDriver driver) {
        driver.manage().window().setPosition(new Point(0, -1000));
    }

    /**
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     * @param driver
     */
    public void windowMaximize(WebDriver driver) {
        driver.manage().window().maximize();
    }

    /**
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     * @param driver
     * @param test
     * @param step
     * @throws Exception
     */
    public void dragAndDropElement(WebDriver driver,JSONObject test,String step) {

        String testsFileName=test.get(testsFileNameText).toString();
        String[] stepWordList = step.split(regexText);
        String elementTo = "";
        String elementFrom = "";

        for (int i=0;i<stepWordList.length;i++) {
            if (stepWordList[i].equalsIgnoreCase("to")) {
                elementTo = stepWordList[i+1].replace("@","");
                break;
            }
            if (stepWordList[i].contains("@")) { elementFrom = stepWordList[i].substring(1); }
        }
        if(!(elementTo.equals("") && elementFrom.equals(""))){
            // Not working
            cmd.dragAndDrop(driver, cmd.findElement(driver, locator.getLocatorValue(testsFileName, elementFrom)), cmd.findElement(driver, locator.getLocatorValue(testsFileName, elementTo)));
        }
        else{
            commonMethods.throwTesboException("Pleas enter valid step: '"+step+"'",log);
        }

    }


    /**
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     * @param driver
     * @param test
     * @param step
     * @throws Exception
     */
    public void clickOnElementFromList(WebDriver driver,JSONObject test,String step) {
        String errorMsg="No string found for click";
        List<WebElement> listOfElements =cmd.findElements(driver, locator.getLocatorValue(test.get(testsFileNameText).toString(), parseElementName(step)));

        //Click on first element from list
        if (step.toLowerCase().contains("first element")) {
         /*
            Step: Click on first element from List @elementName
          */
            listOfElements.get(0).click();

        }

        if (step.toLowerCase().contains("last element")) {
            /*
            Step: Click on last element from List @elementName
            */
            listOfElements.get(listOfElements.size()-1).click();
        }
        if (step.toLowerCase().contains("<") && step.toLowerCase().contains(">")) {
            /*
            Step: click on <any number> from List @elementName
            */
            String clickOnIndex="";
            int startPoint = step.indexOf('<') + 1;
            int endPoint = step.lastIndexOf('>');
            try {
                clickOnIndex = step.substring(startPoint, endPoint).trim();
            } catch (StringIndexOutOfBoundsException e) {
                commonMethods.throwTesboException(errorMsg,log);
            }
            listOfElements.get(Integer.parseInt(clickOnIndex)).click();
        }

        if (step.toLowerCase().contains("\"")) {
            /*
            Step: click on "AnyTextHere" from List @elementName
            */

            String clickOnText="";
            int startPoint = step.indexOf('\"') + 1;
            int endPoint = step.lastIndexOf('\"');
            try {
                clickOnText = step.substring(startPoint, endPoint);

            } catch (StringIndexOutOfBoundsException e) {
                commonMethods.throwTesboException(errorMsg,log);
            }
            for(WebElement element:listOfElements){
                if(element.getText().equals(clickOnText)) {
                    element.click();
                }

            }
        }

    }

    /**
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     * @param test
     * @throws Exception
     */
    public boolean isSeverityOrPriority(JSONObject test) {
        TestsFileParser testsFileParser=new TestsFileParser();
        boolean isSeverityOrPriority=false;
        JSONArray steps= testsFileParser.getSeverityAndPriority(test);
        if(!steps.isEmpty()){
            isSeverityOrPriority=true;
        }
        return isSeverityOrPriority;
    }

    /**
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     * @param step
     * @param driver
     * @param test
     * @return
     * @throws Exception
     */
    public String printStep(WebDriver driver,String step, JSONObject test) {
        String printStep;
        if(step.toLowerCase().contains("@")){
            printStep=stepText+ cmd.findElement(driver, locator.getLocatorValue(test.get(testsFileNameText).toString(), parseElementName(step))).getText();
        }
        else{
            int startPoint = 0;
            int endPoint = 0;
            startPoint = step.indexOf('\"') + 1;
            endPoint = step.lastIndexOf('\"');
            String printText = step.substring(startPoint, endPoint);
            printStep=stepText+printText;
        }
        return printStep.replace("@","");
    }

    /**
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     * @param driver
     * @param test
     * @param step
     * @throws Exception
     */
    public void clickOnOffset(WebDriver driver,JSONObject test,String step) {

        WebElement element =cmd.findElement(driver, locator.getLocatorValue(test.get(testsFileNameText).toString(), parseElementName(step)));
        int startPoint = 0;
        int endPoint = 0;
        startPoint = step.indexOf('(') + 1;
        endPoint = step.lastIndexOf(')');
        String offsetString = step.substring(startPoint, endPoint);
        String[] offsets=offsetString.trim().split(",");
        if(offsets.length!=2){
            commonMethods.throwTesboException("Enter X and Y offset",log);
        }
        /*
        * Step: Click on offset (offset  x, offset  y)
        * */
        cmd.clickOnOffset(driver,element,offsets);


    }

    /**
     * @param step
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     */
    public String[] listOfElementName(String step,int count) {
        String[] stepWordList = step.split(regexText);
        String[] elementName =new String[count];
        int x=0;
        for (String word : stepWordList) {
            if (word.contains("@")) {
                elementName[x] = word;
                x++;
            }
        }
        return elementName;
    }

    /**
     * @param step
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     */
    public String[] listOfSteps(String step, int count) {

        String[] stepWordList = step.split(regexText);
        String[] listOfElement= listOfElementName(step,count);
        String[] steps=new String[count];
        int x=0;
        for(String element:listOfElement){
            StringBuilder newStep=new StringBuilder();
            for (String word : stepWordList) {
                if (!(word.contains("@") || word.toLowerCase().contains("verify") || word.toLowerCase().contains("and"))) {
                    if(newStep.length() == 0){
                        newStep.append(word.trim());
                    }else {
                        newStep.append(" " + word.trim());
                    }
                }
            }
            steps[x]="Verify: "+element+" "+newStep;
            x++;
        }
        return steps;
    }

    /**
     * @param step
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     */
    public String removedVerificationTextFromSteps(String step){
        String textToEnter = "";
        int startPoint = 0;
        int endPoint = 0;
        int count=countOfCharacter(step);
        for (int i=0;i<count;i++){
            try {
                startPoint = step.indexOf('\'');
                endPoint = step.indexOf('\'', startPoint + 1) + 1;
                textToEnter = step.substring(startPoint, endPoint);
                step = step.replace(textToEnter, "");
            }catch (Exception e){log.error("");}
        }
        return step;
    }

    public int countOfCharacter(String step){

        int count = 0;

        //Counts each character except space
        for(int i = 0; i < step.length(); i++) {
            if(step.charAt(i) == '\'')
                count++;
        }
        return count;
    }

    /**
     * @param step
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     */
    public List<String> listOfStepWhoHasSameVerifier(String step, String condition) {
        List<String> stepList = new ArrayList<>();
        Pattern regex = Pattern.compile("[^\\s\"']+|\"[^\"]*\"|'[^']*'");
        Matcher regexMatcher = regex.matcher(step);
        int numberOfStep=0;
        StringBuilder newStep=new StringBuilder();
        int endStep=regexMatcher.regionEnd();
        while (regexMatcher.find()) {
            if(!(regexMatcher.group().equalsIgnoreCase(condition))){
                newStep.append(" "+ regexMatcher.group());
            }

            if(regexMatcher.group().equalsIgnoreCase(condition) || regexMatcher.end()==endStep) {
                if(numberOfStep==0){
                    stepList.add(newStep.toString().trim());
                    newStep=new StringBuilder();
                    numberOfStep++;
                }
                else {
                    stepList.add("Verify: "+newStep);
                    newStep=new StringBuilder();
                }
            }
        }
        return stepList;
    }

}
