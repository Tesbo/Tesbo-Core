package framework;

import Selenium.Commands;
import logger.Logger;
import org.openqa.selenium.WebDriver;

import static org.assertj.core.api.Assertions.assertThat;

public class VerifyParser {

    public void parseVerify(WebDriver driver, String suiteName, String verify) throws Exception {
        Commands cmd = new Commands();
        GetLocator locator = new GetLocator();
        StepParser stepParser = new StepParser();
        Logger logger = new Logger();

        //Text verification.
        if (verify.toLowerCase().contains("text")) {
            try {
                //equal
                if (verify.toLowerCase().contains("equal")) {
                    /**
                     * Verify: @element text is equal ignore case "Text"
                     */
                    if (verify.toLowerCase().contains("ignore case")) {
                        assertThat(cmd.findElement(driver, locator.getLocatorValue(suiteName, stepParser.parseElementName(verify))).getText()).isEqualToIgnoringCase(stepParser.parseTextToEnter(verify));
                    }
                    /**
                     * Verify: @element text is equal "Text"
                     */
                    else {
                        assertThat(cmd.findElement(driver, locator.getLocatorValue(suiteName, stepParser.parseElementName(verify))).getText()).isEqualTo(stepParser.parseTextToEnter(verify));
                    }
                }
                //contains
                else if (verify.toLowerCase().contains("contains")) {
                    /**
                     * Verify: @element text is contains ignore case "Text".
                     */
                    if (verify.toLowerCase().contains("ignore case")) {
                        assertThat(cmd.findElement(driver, locator.getLocatorValue(suiteName, stepParser.parseElementName(verify))).getText()).containsIgnoringCase(stepParser.parseTextToEnter(verify));
                    }
                    /**
                     * Verify: @element text is contains "Text".
                     */
                    else {
                        assertThat(cmd.findElement(driver, locator.getLocatorValue(suiteName, stepParser.parseElementName(verify))).getText()).contains(stepParser.parseTextToEnter(verify));
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
                    assertThat(cmd.findElement(driver, locator.getLocatorValue(suiteName, stepParser.parseElementName(verify))).isDisplayed()).isEqualTo(true);
            } catch (Exception e) {
                logger.testFailed("Step Failed");
                throw e;
            }

        }
    }
}

