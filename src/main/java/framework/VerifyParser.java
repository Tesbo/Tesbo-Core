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
        logger.stepLog(verify);

        //Text verification.
        if (verify.toLowerCase().contains("text")) {
            try {
                //equal
                if (verify.toLowerCase().contains("equal")) {
                    /**
                     * Verify: @element text is equal ignore case "Text"
                     */
                    if (verify.toLowerCase().contains("ignore case")) {
                        //assertThat(cmd.findElement(driver, locator.getLocatorValue(test.get("suiteName").toString(), stepParser.parseElementName(verify))).getText()).isEqualToIgnoringCase(stepParser.parseTextToEnter(test,verify));
                        if(!cmd.findElement(driver, locator.getLocatorValue(test.get("suiteName").toString(), stepParser.parseElementName(verify))).getText().equalsIgnoreCase(stepParser.parseTextToEnter(test,verify))) {
                            throw new AssertException("Expecting:<\""+cmd.findElement(driver, locator.getLocatorValue(test.get("suiteName").toString(), stepParser.parseElementName(verify))).getText()+"\"> to be equal to: <\""+stepParser.parseTextToEnter(test,verify)+"\"> ignoring case considerations");
                        }
                        flag=true;
                    }
                    /**
                     * Verify: @element text is equal "Text"
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
                     * Verify: @element text is contains ignore case "Text".
                     */
                    if (verify.toLowerCase().contains("ignore case")) {
                        //assertThat(cmd.findElement(driver, locator.getLocatorValue(test.get("suiteName").toString(), stepParser.parseElementName(verify))).getText()).containsIgnoringCase(stepParser.parseTextToEnter(test,verify));
                        if(!cmd.findElement(driver, locator.getLocatorValue(test.get("suiteName").toString(), stepParser.parseElementName(verify))).getText().toLowerCase().contains(stepParser.parseTextToEnter(test,verify).toLowerCase())) {
                            throw new AssertException("Expecting:<\""+cmd.findElement(driver, locator.getLocatorValue(test.get("suiteName").toString(), stepParser.parseElementName(verify))).getText()+"\"> to contain: <\""+stepParser.parseTextToEnter(test,verify)+"\"> (ignoring case)");
                        }
                        flag=true;
                    }
                    /**
                     * Verify: @element text is contains "Text".
                     */
                    else {
                        //assertThat(cmd.findElement(driver, locator.getLocatorValue(test.get("suiteName").toString(), stepParser.parseElementName(verify))).getText()).contains(stepParser.parseTextToEnter(test,verify));
                        if(!cmd.findElement(driver, locator.getLocatorValue(test.get("suiteName").toString(), stepParser.parseElementName(verify))).getText().contains(stepParser.parseTextToEnter(test,verify))) {
                            throw new AssertException("Expecting:<\""+cmd.findElement(driver, locator.getLocatorValue(test.get("suiteName").toString(), stepParser.parseElementName(verify))).getText()+"\"> to contain: <\""+stepParser.parseTextToEnter(test,verify)+"\">");
                        }

                        flag=true;
                    }
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
        if(!flag) {
            throw new TesboException("Step is not define properly.");
        }
        logger.testPassed("Passed");
    }

}

