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

import static org.assertj.core.api.Assertions.assertThat;

public class VerifyParser {

    private static final Logger log = LogManager.getLogger(Validation.class);
    public void parseVerify(WebDriver driver, JSONObject test, String verify) throws Exception {
        Commands cmd = new Commands();
        GetLocator locator = new GetLocator();
        StepParser stepParser = new StepParser();
        TesboLogger tesboLogger = new TesboLogger();
        boolean flag=false;
        tesboLogger.stepLog(verify.replace("@",""));
        log.info(verify.replace("@",""));

        WebElement element=null;
        String textOfStep=null;
        if(verify.contains("@")) {
            element = cmd.findElement(driver, locator.getLocatorValue(test.get("suiteName").toString(), stepParser.parseElementName(verify)));
        }
        if(verify.contains("'")) {
             textOfStep = stepParser.parseTextToEnter(test, verify);
        }
        //Is list size
        if (verify.toLowerCase().contains("size")) {
            try {
                /**
                 * Verify: @element has size of '10'
                 */

                if(cmd.findElements(driver, locator.getLocatorValue(test.get("suiteName").toString(), stepParser.parseElementName(verify))).size()!= Integer.parseInt(stepParser.parseTextToEnter(test,verify))) {
                    log.error("Element list size not equal to '"+stepParser.parseTextToEnter(test,verify)+"'");
                    throw new AssertException("Element list size not equal to '"+stepParser.parseTextToEnter(test,verify)+"'");
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
                    /**
                     * Verify: @element text is not equal 'Text'
                     */
                    //assertThat(element.getText()).isNotEqualTo(textOfStep);
                    if(element.getText().equals(textOfStep)) {
                        log.error("Expecting:<\""+element.getText()+"\"> not to be equal to:<\""+textOfStep+"\">");
                        throw new AssertException("Expecting:<\""+element.getText()+"\"> not to be equal to:<\""+textOfStep+"\">");
                    }
                    flag=true;
                }
                else if(verify.toLowerCase().contains("equal")) {
                    /**
                     * Verify: @element text is equal ignore case 'Text'
                     */
                    if (verify.toLowerCase().contains("ignore case")) {
                        //assertThat(element.getText()).isEqualToIgnoringCase(textOfStep);
                        if(!element.getText().equalsIgnoreCase(textOfStep)) {
                            log.error("Expecting:<\""+element.getText()+"\"> to be equal to: <\""+textOfStep+"\"> ignoring case considerations");
                            throw new AssertException("Expecting:<\""+element.getText()+"\"> to be equal to: <\""+textOfStep+"\"> ignoring case considerations");
                        }
                        flag=true;
                    }
                    /**
                     * Verify: @element text is equal 'Text'
                     */
                    else {
                        //assertThat(element.getText()).isEqualTo(textOfStep);
                        if(!element.getText().equals(textOfStep)) {
                            log.error("ComparisonFailure: expected:<\""+element.getText()+"\"> but was:<\""+textOfStep+"\">");
                            throw new AssertException("ComparisonFailure: expected:<\""+element.getText()+"\"> but was:<\""+textOfStep+"\">");
                        }
                        flag=true;
                    }
                }
                //contains
                else if (verify.toLowerCase().contains("contains")) {
                    /**
                     * Verify: @element text is contains ignore case 'Text'.
                     */
                    if (verify.toLowerCase().contains("ignore case")) {
                        //assertThat(element.getText()).containsIgnoringCase(textOfStep);
                        if(!element.getText().toLowerCase().contains(textOfStep.toLowerCase())) {
                            log.error("Expecting:<\""+element.getText()+"\"> to contain: <\""+textOfStep+"\"> (ignoring case)");
                            throw new AssertException("Expecting:<\""+element.getText()+"\"> to contain: <\""+textOfStep+"\"> (ignoring case)");
                        }
                        flag=true;
                    }
                    /**
                     * Verify: @element text is contains 'Text'.
                     */
                    else {
                        //assertThat(element.getText()).contains(textOfStep);
                        if(!element.getText().contains(textOfStep)) {
                            log.error("Expecting:<\""+element.getText()+"\"> to contain: <\""+textOfStep+"\">");
                            throw new AssertException("Expecting:<\""+element.getText()+"\"> to contain: <\""+textOfStep+"\">");
                        }

                        flag=true;
                    }
                }
                else if(verify.toLowerCase().contains("start with")){
                    /**
                     * Verify: @element text is start with 'Text'.
                     */

                    //assertThat(element.getText()).startsWith(textOfStep);
                    if(!element.getText().startsWith(textOfStep)){
                        log.error("Expecting:<\""+element.getText()+"\"> to start with: <\""+textOfStep+"\">");
                        throw new AssertException("Expecting:<\""+element.getText()+"\"> to start with: <\""+textOfStep+"\">");
                    }
                    flag=true;
                }
                else if(verify.toLowerCase().contains("end with")){
                    /**
                     * Verify: @element text is end with 'Text'.
                     */
                    //assertThat(cmd.findElement(driver, locator.getLocatorValue(test.get("suiteName").toString(), stepParser.parseElementName(verify))).getText()).endsWith(stepParser.parseTextToEnter(test,verify));

                    if(!element.getText().endsWith(textOfStep)){
                        log.error("Expecting:<\""+element.getText()+"\"> to end with: <\""+textOfStep+"\">");
                        throw new AssertException("Expecting:<\""+element.getText()+"\"> to end with: <\""+textOfStep+"\">");
                    }
                    flag=true;

                }
                else if(verify.toLowerCase().contains("number")){
                    /**
                     * Verify: @element text should be number.
                     */
                    //assertThat(isNumeric(element.getText())).isTrue();
                    if(!isNumeric(element.getText())) {
                        log.error("ComparisonFailure: expected:<[tru]e> but was:<[fals]e>");
                        throw new AssertException("ComparisonFailure: expected:<[tru]e> but was:<[fals]e>");
                    }
                    flag=true;

                }
                else if(verify.toLowerCase().contains("alphanumeric")){
                    /**
                     * Verify: @element text should be Alphanumeric.
                     */
                    if(!element.getText().matches("[a-zA-Z0-9 ]+")) {
                        log.error("AlphanumericComparisonFailure: expected:<[tru]e> but was:<[fals]e>");
                        throw new AssertException("AlphanumericComparisonFailure: expected:<[tru]e> but was:<[fals]e>");
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
                /**
                 * Verify: @element is displayed
                 * Verify: @element should displayed
                 * Verify: @element is present
                 */
                //assertThat(element.isDisplayed()).isEqualTo(true);
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
                /**
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
                    /**
                     * Verify : Page Title is equal to ignore case 'Google search'
                     */
                    if (verify.toLowerCase().contains("ignore case")) {
                        //assertThat(driver.getTitle()).isEqualToIgnoringCase(textOfStep);
                        if(!driver.getTitle().equalsIgnoreCase(textOfStep)) {
                            log.error("ComparisonFailure: expected:<\""+driver.getTitle()+"\"> but was:<\""+textOfStep+"\">");
                            throw new AssertException("ComparisonFailure: expected:<\""+driver.getTitle()+"\"> but was:<\""+textOfStep+"\">");
                        }
                        flag=true;
                    }
                    /**
                     * Verify : Page Title is equal to 'Google search'
                     */
                    else {
                        //assertThat(driver.getTitle()).isEqualTo(textOfStep);
                        if(!driver.getTitle().equals(textOfStep)) {
                            log.error("ComparisonFailure: expected:<\""+driver.getTitle()+"\"> but was:<\""+textOfStep+"\">");
                            throw new AssertException("ComparisonFailure: expected:<\""+driver.getTitle()+"\"> but was:<\""+textOfStep+"\">");
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

                /**
                 * Step: Get cookies and check 'any cookie name' is available
                 */
                if (!cmd.isCookieAvailable(driver, stepParser.parseTextToEnter(test, verify))) {
                    log.error("'" + stepParser.parseTextToEnter(test, verify) + "' cookie is not found");
                    throw new AssertException("'" + stepParser.parseTextToEnter(test, verify) + "' cookie is not found");
                }
                flag = true;
            }
        }
        if (verify.toLowerCase().contains("current url")) {

            if(verify.toLowerCase().contains("is equal to")) {

                /**
                 * Verify: current url is equal to 'https://tesbo10.atlassian.net'
                 */
                if (!cmd.getCurrentUrl(driver,stepParser.parseTextToEnter(test, verify))) {
                    log.error("current url is not match with '" + stepParser.parseTextToEnter(test, verify) + "'");
                    throw new AssertException("current url is not match with '" + stepParser.parseTextToEnter(test, verify) + "'");
                }
                flag = true;
            }
            if(verify.toLowerCase().contains("is contains")) {

                /**
                 * Verify: current url is contains 'https://tesbo10.atlassian.net'
                 */
                if (!cmd.verifyCurrentUrlContains(driver,stepParser.parseTextToEnter(test, verify))) {
                    log.error("current url contains is not match with '" + stepParser.parseTextToEnter(test, verify) + "'");
                    throw new AssertException("current url contains is not match with '" + stepParser.parseTextToEnter(test, verify) + "'");
                }
                flag = true;
            }
        }
        if(!flag) {
            log.error("'"+verify+"' Step is not define properly.");
            throw new TesboException("'"+verify+"' Step is not define properly.");
        }
        tesboLogger.testPassed("Passed");
    }

    public static boolean isNumeric(String strNum) {
        try {
            double d = Double.parseDouble(strNum);
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
}

