package framework;

import Selenium.Commands;
import org.json.simple.JSONObject;
import org.openqa.selenium.WebDriver;

import static org.assertj.core.api.Assertions.assertThat;

public class VerifyParser {

    public void parseVerify(WebDriver driver, JSONObject test, String verify) throws Exception {
        Commands cmd = new Commands();
        GetLocator locator = new GetLocator();
        StepParser stepParser = new StepParser();

        //Text verification.
        if (verify.toLowerCase().contains("text")) {
            try {
                //equal
                if (verify.toLowerCase().contains("equal")) {
                    /**
                     * Verify: @element text is equal ignore case "Text"
                     */
                    if (verify.toLowerCase().contains("ignore case")) {
                        assertThat(cmd.findElement(driver, locator.getLocatorValue(test.get("suiteName").toString(), stepParser.parseElementName(verify))).getText()).isEqualToIgnoringCase(stepParser.parseTextToEnter(test,verify));
                    }
                    /**
                     * Verify: @element text is equal "Text"
                     */
                    else {
                        assertThat(cmd.findElement(driver, locator.getLocatorValue(test.get("suiteName").toString(), stepParser.parseElementName(verify))).getText()).isEqualTo(stepParser.parseTextToEnter(test,verify));
                    }
                }
                //contains
                else if (verify.toLowerCase().contains("contains")) {
                    /**
                     * Verify: @element text is contains ignore case "Text".
                     */
                    if (verify.toLowerCase().contains("ignore case")) {
                        assertThat(cmd.findElement(driver, locator.getLocatorValue(test.get("suiteName").toString(), stepParser.parseElementName(verify))).getText()).containsIgnoringCase(stepParser.parseTextToEnter(test,verify));
                    }
                    /**
                     * Verify: @element text is contains "Text".
                     */
                    else {
                        assertThat(cmd.findElement(driver, locator.getLocatorValue(test.get("suiteName").toString(), stepParser.parseElementName(verify))).getText()).contains(stepParser.parseTextToEnter(test,verify));
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
                    assertThat(cmd.findElement(driver, locator.getLocatorValue(test.get("suiteName").toString(), stepParser.parseElementName(verify))).isDisplayed()).isEqualTo(true);
            } catch (Exception e) {
                System.out.println("Step Failed");
                throw e;
            }

        }
    }
}

