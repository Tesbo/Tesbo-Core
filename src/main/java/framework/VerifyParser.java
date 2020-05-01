package framework;

import Selenium.Commands;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import logger.TesboLogger;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import Exception.*;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebElement;

public class VerifyParser {

    private static final Logger log = LogManager.getLogger(VerifyParser.class);

    String toContainText="\"> to contain: <\"";
    String butWasText="\"> but was:<\"";
    String attributeOrCssText="attributeOrCSS";
    String toBeEqualText="\"> to be equal to: <\"";
    String notToBeEqualText="\"> not to be equal to:<\"";
    String testsFileNameText="testsFileName";
    String comparisonFailureText="ComparisonFailure: expected:<\"";
    String expectingText="Expecting:<\"";
    String ignoreCaseText="ignore case";

    public void parseVerify(WebDriver driver, JSONObject test, String verify) throws Exception {
        Commands cmd = new Commands();
        GetLocator locator = new GetLocator();
        StepParser stepParser = new StepParser();
        TesboLogger tesboLogger = new TesboLogger();
        boolean flag=false;
        if(!(verify.contains("{") && verify.contains("}"))) {
            String verifyStepLog=verify.replace("@", "");
            tesboLogger.stepLog(verifyStepLog);
            log.info(verifyStepLog);
        }
        WebElement element=null;
        String textOfStep=null;
        if(verify.contains("@")) {
            element = cmd.findElement(driver, locator.getLocatorValue(test.get(testsFileNameText).toString(), stepParser.parseElementName(verify)));
        }
        if(verify.contains("'") || (verify.contains("{") && verify.contains("}"))) {
             textOfStep = stepParser.parseTextToEnter(test, verify);
        }
        //Is list size
        if (verify.toLowerCase().contains(" size ")) {
            try {
                /*
                 * Verify: @element has size of '10'
                 */

                if(cmd.findElements(driver, locator.getLocatorValue(test.get(testsFileNameText).toString(), stepParser.parseElementName(verify))).size()!= Integer.parseInt(stepParser.parseTextToEnter(test,verify))) {
                    String errorMsg="Element list size not equal to '"+stepParser.parseTextToEnter(test,verify)+"'";
                    log.error(errorMsg);
                    throw new AssertException(errorMsg);
                }
                flag=true;
            } catch (Exception e) {
                throw e;
            }

        }

        //Text verification.
        if (verify.toLowerCase().contains("text")) {
            try {
                //equal
                if (verify.toLowerCase().contains("not equal")) {
                    /*
                     * Verify: @element text is not equal 'Text'
                     */
                    if(element.getText().equals(textOfStep)) {
                        String errorMsg=expectingText+element.getText()+notToBeEqualText+textOfStep+"\">";
                        log.error(errorMsg);
                        throw new AssertException(errorMsg);
                    }
                    flag=true;
                }
                else if(verify.toLowerCase().contains("equal")) {
                    /*
                     * Verify: @element text is equal ignore case 'Text'
                     */
                    if (verify.toLowerCase().contains(ignoreCaseText)) {
                        if(!element.getText().equalsIgnoreCase(textOfStep)) {
                            String errorMsg=expectingText+element.getText()+toBeEqualText+textOfStep+"\"> ignoring case considerations";
                            log.error(errorMsg);
                            throw new AssertException(errorMsg);
                        }
                        flag=true;
                    }
                    /*
                     * Verify: @element text is equal 'Text'
                     */
                    else {
                        if(!element.getText().equals(textOfStep)) {
                            String errorMsg=comparisonFailureText+element.getText()+butWasText+textOfStep+"\">";
                            log.error(errorMsg);
                            throw new AssertException(errorMsg);
                        }
                        flag=true;
                    }
                }
                //contains
                else if (verify.toLowerCase().contains("contains")) {
                    /*
                     * Verify: @element text is contains ignore case 'Text'.
                     */
                    if (verify.toLowerCase().contains(ignoreCaseText)) {
                        if(!element.getText().toLowerCase().contains(textOfStep.toLowerCase())) {
                            String errorMsg=expectingText+element.getText()+toContainText+textOfStep+"\"> (ignoring case)";
                            log.error(errorMsg);
                            throw new AssertException(errorMsg);
                        }
                        flag=true;
                    }
                    /*
                     * Verify: @element text is contains 'Text'.
                     */
                    else {
                        if(!element.getText().contains(textOfStep)) {
                            String errorMsg=expectingText+element.getText()+toContainText+textOfStep+"\">";
                            log.error(errorMsg);
                            throw new AssertException(errorMsg);
                        }

                        flag=true;
                    }
                }
                else if(verify.toLowerCase().contains("start with")){
                    /*
                     * Verify: @element text is start with 'Text'.
                     */

                    if(!element.getText().startsWith(textOfStep)){
                        String errorMsg=expectingText+element.getText()+"\"> to start with: <\""+textOfStep+"\">";
                        log.error(errorMsg);
                        throw new AssertException(errorMsg);
                    }
                    flag=true;
                }
                else if(verify.toLowerCase().contains("end with")){
                    /*
                     * Verify: @element text is end with 'Text'.
                     */

                    if(!element.getText().endsWith(textOfStep)){
                        String errorMsg=expectingText+element.getText()+"\"> to end with: <\""+textOfStep+"\">";
                        log.error(errorMsg);
                        throw new AssertException(errorMsg);
                    }
                    flag=true;

                }
                else if(verify.toLowerCase().contains("number")){
                    /*
                     * Verify: @element text should be number.
                     */
                    if(!isNumeric(element.getText())) {
                        String errorMsg="ComparisonFailure: expected:<[tru]e> but was:<[fals]e>";
                        log.error(errorMsg);
                        throw new AssertException(errorMsg);
                    }
                    flag=true;

                }
                else if(verify.toLowerCase().contains("alphanumeric")){
                    /*
                     * Verify: @element text should be Alphanumeric.
                     */
                    if(!element.getText().matches("[a-zA-Z0-9 ]+")) {
                        String errorMsg="AlphanumericComparisonFailure: expected:<[tru]e> but was:<[fals]e>";
                        log.error(errorMsg);
                        throw new AssertException(errorMsg);
                    }
                    flag=true;

                }
            } catch (Exception e) {
                throw e;
            }
        }

        //Is displayed
        if (verify.toLowerCase().contains("displayed") || verify.toLowerCase().contains("present")) {
            try {
                /*
                 * Verify: @element is displayed
                 * Verify: @element should displayed
                 * Verify: @element is present
                 */
                if(!element.isDisplayed()) {
                    log.error("Element is not displayed");
                    throw new AssertException("Element is not displayed");
                }
                flag=true;
            } catch (Exception e) {
                throw e;
            }

        }

        //Is Visible
        if (verify.toLowerCase().contains("visible")) {
            try {
                /*
                 * Verify: @element is Visible
                 */
                if(!isVisibleInViewport(element)) {
                    log.error("Element is not Visible");
                    throw new AssertException("Element is not Visible");
                }
                flag=true;
            } catch (Exception e) {
                throw e;
            }

        }

        if (verify.toLowerCase().contains("page title")) {

            //equal
            try {
                if (verify.toLowerCase().contains("equal")) {
                    /*
                     * Verify : Page Title is equal to ignore case 'Google search'
                     */
                    if (verify.toLowerCase().contains(ignoreCaseText)) {
                        if(!driver.getTitle().equalsIgnoreCase(textOfStep)) {
                            String errorMsg=comparisonFailureText+driver.getTitle()+butWasText+textOfStep+"\">";
                            log.error(errorMsg);
                            throw new AssertException(errorMsg);
                        }
                        flag=true;
                    }
                    /*
                     * Verify : Page Title is equal to 'Google search'
                     */
                    else {
                        if(!driver.getTitle().equals(textOfStep)) {
                            String errorMsg=comparisonFailureText+driver.getTitle()+butWasText+textOfStep+"\">";
                            log.error(errorMsg);
                            throw new AssertException(errorMsg);
                        }
                        flag=true;
                    }
                }
            } catch (Exception e) {
                throw e;
            }
        }
        if (verify.toLowerCase().contains("get cookies")) {

            if(verify.toLowerCase().contains("check") && verify.toLowerCase().contains("is available")) {

                /*
                 * Step: Get cookies and check 'any cookie name' is available
                 */
                if (!cmd.isCookieAvailable(driver, stepParser.parseTextToEnter(test, verify))) {
                    String errorMsg="'" + stepParser.parseTextToEnter(test, verify) + "' cookie is not found";
                    log.error(errorMsg);
                    throw new AssertException(errorMsg);
                }
                flag = true;
            }
        }
        if (verify.toLowerCase().contains("current url")) {

            if(verify.toLowerCase().contains("is equal to")) {

                /*
                 * Verify: current url is equal to 'https://tesbo10.atlassian.net'
                 */
                if (!cmd.getCurrentUrl(driver,stepParser.parseTextToEnter(test, verify))) {
                    String errorMsg="current url is not match with '" + stepParser.parseTextToEnter(test, verify) + "'";
                    log.error(errorMsg);
                    throw new AssertException(errorMsg);
                }
                flag = true;
            }
            if(verify.toLowerCase().contains("is contains")) {

                /*
                 * Verify: current url is contains 'https://tesbo10.atlassian.net'
                 */
                if (!cmd.verifyCurrentUrlContains(driver,stepParser.parseTextToEnter(test, verify))) {
                    String errorMsg="current url contains is not match with '" + stepParser.parseTextToEnter(test, verify) + "'";
                    log.error(errorMsg);
                    throw new AssertException(errorMsg);
                }
                flag = true;
            }
        }

        // verify attribute value
        if(verify.toLowerCase().contains(" get attribute ")){
            element=cmd.findElement(driver, locator.getLocatorValue(test.get(testsFileNameText).toString(), stepParser.parseElementName(verify)));

            if(element.getAttribute(getAttributeAndCSSNameAndVerifyText(verify,attributeOrCssText))==null){
                String errorMsg="'"+getAttributeAndCSSNameAndVerifyText(verify,attributeOrCssText)+"' attribute is not fount.";
                log.info(errorMsg);
                throw new TesboException(errorMsg);
            }
            String attributeValue=element.getAttribute(getAttributeAndCSSNameAndVerifyText(verify,attributeOrCssText));
            String verifyText=null;
            if(verify.contains("{") && verify.contains("}")){
                String[] stepWord=verify.split(" ");
                for(String word:stepWord){
                    if(word.equalsIgnoreCase("attribute")){
                        verifyText=stepParser.parseTextToEnter(test, verify.replace(" "+word+" "," "));
                        break;
                    }
                }
            }
            else{
                verifyText=getAttributeAndCSSNameAndVerifyText(verify,"text") ;
            }

            if(verify.toLowerCase().contains(" not equal to ")){
                /*
                * Verify: Get attribute 'attribute name' of @element is not equal to 'attribute value'
                * */
                if(attributeValue.equals(verifyText)){
                    String errorMsg=expectingText+attributeValue+notToBeEqualText+verifyText+"\">";
                    log.error(errorMsg);
                    throw new AssertException(errorMsg);
                }
                flag=true;
            }
            else if(verify.toLowerCase().contains(" is equal to ")){

                if(verify.toLowerCase().contains(" is equal to ignore case ")){
                    /*
                     * Verify: Get attribute 'attribute name' of @element is equal to ignore case 'attribute value'
                     * */
                    if(!attributeValue.equalsIgnoreCase(verifyText)){
                        String errorMsg=expectingText+attributeValue+toBeEqualText+verifyText+"\">";
                        log.error(errorMsg);
                        throw new AssertException(errorMsg);
                    }
                    flag=true;
                }
                else{
                    /*
                     * Verify: Get attribute 'attribute name' of @element is equal to 'attribute value'
                     * */
                    if(!attributeValue.equals(verifyText)){
                        String errorMsg=comparisonFailureText+attributeValue+butWasText+verifyText+"\">";
                        log.error(errorMsg);
                        throw new AssertException(errorMsg);
                    }
                    flag=true;
                }
            }
            else if(verify.toLowerCase().contains(" contains ")){
                /*
                 * Verify: Get attribute 'attribute name' of @element contains is 'attribute value'
                 * */
                if(!attributeValue.contains(verifyText)){
                    String errorMsg=expectingText+attributeValue+toContainText+verifyText+"\">";
                    log.error(errorMsg);
                    throw new AssertException(errorMsg);
                }
                flag=true;
            }
        }

        // verify CSS value
        if(verify.toLowerCase().contains(" get css value ")){
            element=cmd.findElement(driver, locator.getLocatorValue(test.get(testsFileNameText).toString(), stepParser.parseElementName(verify)));

            if(element.getCssValue(getAttributeAndCSSNameAndVerifyText(verify,attributeOrCssText))==null){
                String errorMsg="'"+getAttributeAndCSSNameAndVerifyText(verify,attributeOrCssText)+"' CSS attribute is not fount.";
                log.info(errorMsg);
                throw new TesboException(errorMsg);
            }
            String cssValue=element.getCssValue(getAttributeAndCSSNameAndVerifyText(verify,attributeOrCssText));
            String verifyText=null;
            if(verify.contains("{") && verify.contains("}")){
                String[] stepWord=verify.split(" ");
                for(String word:stepWord){
                    if(word.equalsIgnoreCase("css")){
                        verifyText=stepParser.parseTextToEnter(test, verify.replace(" "+word+" "," "));
                        break;
                    }
                }
            }
            else{
                verifyText=getAttributeAndCSSNameAndVerifyText(verify,"text") ;
            }

            if(verify.toLowerCase().contains(" not equal to ")){
                /*
                 * Verify: Get css value 'css name' of @element is not equal to 'css value'
                 * */
                if(cssValue.equals(verifyText)){
                    String errorMsg=expectingText+cssValue+notToBeEqualText+verifyText+"\">";
                    log.error(errorMsg);
                    throw new AssertException(errorMsg);
                }
                flag=true;
            }
            else if(verify.toLowerCase().contains(" is equal to ")){

                if(verify.toLowerCase().contains(" is equal to ignore case ")){
                    /*
                     * Verify: Get css value 'css name' of @element is equal to ignore case 'css value'
                     * */
                    if(!cssValue.equalsIgnoreCase(verifyText)){
                        String errorMsg=expectingText+cssValue+toBeEqualText+verifyText+"\">";
                        log.error(errorMsg);
                        throw new AssertException(errorMsg);
                    }
                    flag=true;
                }
                else{
                    if(!cssValue.equals(verifyText)){
                        /*
                         * Verify: Get css value 'css name' of @element is equal to 'css value'
                         * */
                        String errorMsg=comparisonFailureText+cssValue+butWasText+verifyText+"\">";
                        log.error(errorMsg);
                        throw new AssertException(errorMsg);
                    }
                    flag=true;
                }
            }
            else if(verify.toLowerCase().contains(" contains ")){
                /*
                 * Verify: Get css value 'css name' of @element contains is 'css value'
                 * */
                if(!cssValue.contains(verifyText)){
                    String errorMsg=expectingText+cssValue+toContainText+verifyText+"\">";
                    log.error(errorMsg);
                    throw new AssertException(errorMsg);
                }
                flag=true;
            }
        }

        if(!flag) {
            String errorMsg="'"+verify+"' Step is not define properly.";
            log.error(errorMsg);
            throw new TesboException(errorMsg);
        }
        tesboLogger.testPassed("Passed");
    }

    public static boolean isNumeric(String strNum) {
        try {
            Double.parseDouble(strNum);
        } catch (NumberFormatException | NullPointerException nfe) {
            return false;
        }
        return true;
    }

    public static Boolean isVisibleInViewport(WebElement element) {
        WebDriver driver = ((RemoteWebElement)element).getWrappedDriver();

        return (Boolean)((JavascriptExecutor)driver).executeScript(
                "var elem = arguments[0],                 " +
                        "  box = elem.getBoundingClientRect(),    " +
                        "  cx = box.left + box.width / 2,         " +
                        "  cy = box.top + box.height / 2,         " +
                        "  e = document.elementFromPoint(cx, cy); " +
                        "for (; e; e = e.parentElement) {         " +
                        "  if (e === elem)                        " +
                        "    return true;                         " +
                        "}                                        " +
                        "return false;                            "
                , element);
    }

    public String getAttributeAndCSSNameAndVerifyText(String step, String attributeOrText){
        String attributeNameOrText=null;
        String[] stepsWord=step.split("'");

        if(attributeOrText.equals(attributeOrCssText)){
            attributeNameOrText= stepsWord[1];
        }
        else if(attributeOrText.equals("text")){
            attributeNameOrText= stepsWord[3];
        }
        return attributeNameOrText;
    }
}

