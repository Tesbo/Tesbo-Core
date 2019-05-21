package framework;

import Selenium.Commands;
import org.json.simple.JSONObject;
import logger.Logger;
import org.openqa.selenium.WebDriver;
import Exception.*;

import static org.assertj.core.api.Assertions.assertThat;

public class VerifyParser {

    public void parseVerify(WebDriver driver, JSONObject test, String verify) throws Exception {
        Commands cmd = new Commands();
        GetLocator locator = new GetLocator();
        StepParser stepParser = new StepParser();
        Logger logger = new Logger();
        boolean flag=false;
        logger.stepLog(verify.replace("@",""));

        //Is list size
        if (verify.toLowerCase().contains("size")) {
            try {
                /**
                 * Verify: @element has size of '10'
                 */

                if(cmd.findElements(driver, locator.getLocatorValue(test.get("suiteName").toString(), stepParser.parseElementName(verify))).size()!= Integer.parseInt(stepParser.parseTextToEnter(test,verify))) {
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
                    //assertThat(cmd.findElement(driver, locator.getLocatorValue(test.get("suiteName").toString(), stepParser.parseElementName(verify))).getText()).isNotEqualTo(stepParser.parseTextToEnter(test,verify));
                    if(cmd.findElement(driver, locator.getLocatorValue(test.get("suiteName").toString(), stepParser.parseElementName(verify))).getText().equals(stepParser.parseTextToEnter(test,verify))) {
                        throw new AssertException("Expecting:<\""+cmd.findElement(driver, locator.getLocatorValue(test.get("suiteName").toString(), stepParser.parseElementName(verify))).getText()+"\"> not to be equal to:<\""+stepParser.parseTextToEnter(test,verify)+"\">");
                    }
                    flag=true;
                }
                else if(verify.toLowerCase().contains("equal")) {
                    /**
                     * Verify: @element text is equal ignore case 'Text'
                     */
                    if (verify.toLowerCase().contains("ignore case")) {
                        //assertThat(cmd.findElement(driver, locator.getLocatorValue(test.get("suiteName").toString(), stepParser.parseElementName(verify))).getText()).isEqualToIgnoringCase(stepParser.parseTextToEnter(test,verify));
                        if(!cmd.findElement(driver, locator.getLocatorValue(test.get("suiteName").toString(), stepParser.parseElementName(verify))).getText().equalsIgnoreCase(stepParser.parseTextToEnter(test,verify))) {
                            throw new AssertException("Expecting:<\""+cmd.findElement(driver, locator.getLocatorValue(test.get("suiteName").toString(), stepParser.parseElementName(verify))).getText()+"\"> to be equal to: <\""+stepParser.parseTextToEnter(test,verify)+"\"> ignoring case considerations");
                        }
                        flag=true;
                    }
                    /**
                     * Verify: @element text is equal 'Text'
                     */
                    else {
                        //assertThat(cmd.findElement(driver, locator.getLocatorValue(test.get("suiteName").toString(), stepParser.parseElementName(verify))).getText()).isEqualTo(stepParser.parseTextToEnter(test,verify));
                        if(!cmd.findElement(driver, locator.getLocatorValue(test.get("suiteName").toString(), stepParser.parseElementName(verify))).getText().equals(stepParser.parseTextToEnter(test,verify))) {
                            throw new AssertException("ComparisonFailure: expected:<\""+cmd.findElement(driver, locator.getLocatorValue(test.get("suiteName").toString(), stepParser.parseElementName(verify))).getText()+"\"> but was:<\""+stepParser.parseTextToEnter(test,verify)+"\">");
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
                        //assertThat(cmd.findElement(driver, locator.getLocatorValue(test.get("suiteName").toString(), stepParser.parseElementName(verify))).getText()).containsIgnoringCase(stepParser.parseTextToEnter(test,verify));
                        if(!cmd.findElement(driver, locator.getLocatorValue(test.get("suiteName").toString(), stepParser.parseElementName(verify))).getText().toLowerCase().contains(stepParser.parseTextToEnter(test,verify).toLowerCase())) {
                            throw new AssertException("Expecting:<\""+cmd.findElement(driver, locator.getLocatorValue(test.get("suiteName").toString(), stepParser.parseElementName(verify))).getText()+"\"> to contain: <\""+stepParser.parseTextToEnter(test,verify)+"\"> (ignoring case)");
                        }
                        flag=true;
                    }
                    /**
                     * Verify: @element text is contains 'Text'.
                     */
                    else {
                        //assertThat(cmd.findElement(driver, locator.getLocatorValue(test.get("suiteName").toString(), stepParser.parseElementName(verify))).getText()).contains(stepParser.parseTextToEnter(test,verify));
                        if(!cmd.findElement(driver, locator.getLocatorValue(test.get("suiteName").toString(), stepParser.parseElementName(verify))).getText().contains(stepParser.parseTextToEnter(test,verify))) {
                            throw new AssertException("Expecting:<\""+cmd.findElement(driver, locator.getLocatorValue(test.get("suiteName").toString(), stepParser.parseElementName(verify))).getText()+"\"> to contain: <\""+stepParser.parseTextToEnter(test,verify)+"\">");
                        }

                        flag=true;
                    }
                }
                else if(verify.toLowerCase().contains("start with")){
                    /**
                     * Verify: @element text is start with 'Text'.
                     */
                    assertThat(cmd.findElement(driver, locator.getLocatorValue(test.get("suiteName").toString(), stepParser.parseElementName(verify))).getText()).startsWith(stepParser.parseTextToEnter(test,verify));
                    flag=true;
                }
                else if(verify.toLowerCase().contains("end with")){
                    /**
                     * Verify: @element text is end with 'Text'.
                     */
                    assertThat(cmd.findElement(driver, locator.getLocatorValue(test.get("suiteName").toString(), stepParser.parseElementName(verify))).getText()).endsWith(stepParser.parseTextToEnter(test,verify));
                    flag=true;

                }
                else if(verify.toLowerCase().contains("number")){
                    /**
                     * Verify: @element text should be number.
                     */
                    //assertThat(isNumeric(54).isTrue();
                    if(!isNumeric(cmd.findElement(driver, locator.getLocatorValue(test.get("suiteName").toString(), stepParser.parseElementName(verify))).getText())) {
                        throw new AssertException("ComparisonFailure: expected:<[tru]e> but was:<[fals]e>");
                    }
                    flag=true;

                }
            } catch (Exception e) {
                throw e;
            }
        }

        //Is displayed
        if (verify.toLowerCase().contains("displayed")) {
            try {
                /**
                 * Verify: @element is displayed
                 * Verify: @element should displayed
                 */
                //assertThat(cmd.findElement(driver, locator.getLocatorValue(test.get("suiteName").toString(), stepParser.parseElementName(verify))).isDisplayed()).isEqualTo(true);
                if(!cmd.findElement(driver, locator.getLocatorValue(test.get("suiteName").toString(), stepParser.parseElementName(verify))).isDisplayed()) {
                    throw new AssertException("Element is not displayed");
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
                        //assertThat(driver.getTitle()).isEqualToIgnoringCase(stepParser.parseTextToEnter(test,verify));
                        if(!driver.getTitle().equalsIgnoreCase(stepParser.parseTextToEnter(test,verify))) {
                            throw new AssertException("ComparisonFailure: expected:<\""+driver.getTitle()+"\"> but was:<\""+stepParser.parseTextToEnter(test,verify)+"\">");
                        }
                        flag=true;
                    }
                    /**
                     * Verify : Page Title is equal to 'Google search'
                     */
                    else {
                        //assertThat(driver.getTitle()).isEqualTo(stepParser.parseTextToEnter(test,verify));
                        if(!driver.getTitle().equals(stepParser.parseTextToEnter(test,verify))) {
                            throw new AssertException("ComparisonFailure: expected:<\""+driver.getTitle()+"\"> but was:<\""+stepParser.parseTextToEnter(test,verify)+"\">");
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

                    throw new AssertException("current url is not match with '" + stepParser.parseTextToEnter(test, verify) + "'");
                }
                flag = true;
            }
            if(verify.toLowerCase().contains("is contains")) {

                /**
                 * Verify: current url is contains 'https://tesbo10.atlassian.net'
                 */
                if (!cmd.verifyCurrentUrlContains(driver,stepParser.parseTextToEnter(test, verify))) {

                    throw new AssertException("current url contains is not match with '" + stepParser.parseTextToEnter(test, verify) + "'");
                }
                flag = true;
            }
        }
        if(!flag) {
            throw new TesboException("Step is not define properly.");
        }
        logger.testPassed("Passed");
    }
    public static boolean isNumeric(String strNum) {
        try {
            double d = Double.parseDouble(strNum);
        } catch (NumberFormatException | NullPointerException nfe) {
            return false;
        }
        return true;
    }
}

