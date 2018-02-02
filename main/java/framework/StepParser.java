package framework;

import Selenium.Commands;
import org.apache.commons.lang3.ObjectUtils;
import org.openqa.selenium.WebDriver;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

public class StepParser {


    public static void main(String[] args) {
        StepParser parser = new StepParser();

        parser.parseTextToEnter("Verify that @email was received");
    }

    public void parseStep(WebDriver driver, String suiteName, String step)

    {
        Commands cmd = new Commands();
        GetLocator locator = new GetLocator();


        //Clicks
        if (step.toLowerCase().contains("click")) {
            System.out.println(parseElementName(step));

            try {
                cmd.findElement(driver, locator.getLocatorValue(suiteName, parseElementName(step))).click();
                System.out.println("Step Passed");
            } catch (Exception e) {
                System.out.println("Step Failed");
                e.printStackTrace();
            }
        }

        //Sendkeys
        if (step.toLowerCase().contains("enter")) {
            try {
                cmd.findElement(driver, locator.getLocatorValue(suiteName, parseElementName(step))).sendKeys(parseTextToEnter(step));
                System.out.println("Step Passed");
            } catch (Exception e) {
                System.out.println("Step Failed");
                e.printStackTrace();
            }
        }

        // Get URL
        if (step.toLowerCase().contains("url")) {
            try {
                driver.get(parseTextToEnter(step));
                System.out.println("Step Passed");
            } catch (Exception e) {
                System.out.println("Step Failed");
                e.printStackTrace();

            }
        }

        //Switch
        if (step.toLowerCase().contains("switch")) {
            switchFunction(driver, suiteName, step);
        }

        //navigate
        if (step.toLowerCase().contains("navigate")) {
            navigateFunction(driver, step);
        }

        //scroll
        if (step.toLowerCase().contains("scroll")) {
            scrollFunction(driver, suiteName, step);
        }

        //pause
        if (step.toLowerCase().contains("pause")) {
            pauseFunction(driver, suiteName, step);
        }

        //select
        if (step.toLowerCase().contains("select")) {
            selectFunction(driver, suiteName, step);
        }
    }


    public void switchFunction(WebDriver driver, String suiteName, String step) {

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
                    assertThat(alertText).containsIgnoringCase(parseTextToEnter(step));
                }
                /**
                 * enter identify.
                 * Step : Switch to alert then enter 'Text'.
                 */
                else if (step.toLowerCase().contains("enter")) {
                    cmd.switchAlertSendKey(driver, parseTextToEnter(step));
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
                        System.out.println("id : " + parseTextToEnter(step));
                        cmd.switchFrame(driver, parseTextToEnter(step));
                    }
                    //Step : Switch to frame using name 'FrameName'.
                    else if (step.toLowerCase().contains("name")) {
                        System.out.println("name : " + parseTextToEnter(step));
                        cmd.switchFrame(driver, parseTextToEnter(step));
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
                        cmd.switchFrameElement(driver, cmd.findElement(driver, locator.getLocatorValue(suiteName, parseElementName(step))));
                    } catch (NullPointerException e) {
                        System.out.println("No element find.");
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
            System.out.println("Step Failed");
            e.printStackTrace();
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

    public void scrollFunction(WebDriver driver, String suiteName, String step) {
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
        else if (!(parseNumverToEnter(step, 0) == null)) {
            try {
                String x = parseNumverToEnter(step, 0);
                String y = parseNumverToEnter(step, 1);
                cmd.scrollToCoordinate(driver, x, y);
            } catch (NullPointerException e) {
                System.out.println("No coordinate find.");
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
                System.out.println("No element find.");
            }
        }
    }

    public void pauseFunction(WebDriver driver, String suiteName, String step) {
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
                System.out.println("No element find.");
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
                System.out.println("No element find.");
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
                System.out.println("No element find.");
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

    public void selectFunction(WebDriver driver, String suiteName, String step) {
        Commands cmd = new Commands();
        GetLocator locator = new GetLocator();

        if (step.toLowerCase().contains("deselect")) {
            /**
             * 'all' identify.
             * Step : Deselect all from @element
             */
            if (step.toLowerCase().contains("all")) {
                cmd.deselectAll(cmd.findElement(driver, locator.getLocatorValue(suiteName, parseElementName(step))));
            }
            /**
             * 'text' identify.
             * Step : Deselect using text 'Text' from @element
             */
            else if (step.toLowerCase().contains("text")) {
                cmd.deselectText(cmd.findElement(driver, locator.getLocatorValue(suiteName, parseElementName(step))), parseTextToEnter(step));
            }
            /**
             * 'index' identify.
             * Step : Deselect using index 1 from @element
             */
            else if (step.toLowerCase().contains("index")) {
                cmd.deselectIndex(cmd.findElement(driver, locator.getLocatorValue(suiteName, parseElementName(step))), Integer.parseInt(parseNumverToEnter(step, 0)));
            }
            /**
             * 'value' identify.
             * Step : Deselect using value 'Text' from @element
             */
            else if (step.toLowerCase().contains("value")) {
                cmd.deselectValue(cmd.findElement(driver, locator.getLocatorValue(suiteName, parseElementName(step))), parseTextToEnter(step));
            }
        } else {
            /**
             * 'text' identify.
             * Step : Select using text 'Text' from @element
             */
            if (step.toLowerCase().contains("text")) {
                cmd.selectText(cmd.findElement(driver, locator.getLocatorValue(suiteName, parseElementName(step))), parseTextToEnter(step));
            }
            /**
             * 'index' identify.
             * Step : Select using index 1 from @element
             */
            else if (step.toLowerCase().contains("index")) {
                cmd.selectIndex(cmd.findElement(driver, locator.getLocatorValue(suiteName, parseElementName(step))), Integer.parseInt(parseNumverToEnter(step, 0)));
            }
            /**
             * 'value' identify.
             * Step : Select using value 'Text' form @element
             */
            else if (step.toLowerCase().contains("value")) {
                cmd.selectValue(cmd.findElement(driver, locator.getLocatorValue(suiteName, parseElementName(step))), parseTextToEnter(step));
            }
        }
    }

    public String parseElementName(String step) {

        String[] stepWordList = step.split("\\s+");

        String elementName = "";

        for (String word : stepWordList) {
            if (word.contains("@")) {
                elementName = word.substring(1);
            }

        }
        return elementName;
    }

    public String parseTextToEnter(String step) {
        String textToEnter = "";

        String[] stepWordList = step.split("\\s+");

        for (String word : stepWordList) {

            if (word.contains("'")) {
                int length = word.length() - 1;
                textToEnter = word.substring(1, length);
            }

        }

        return textToEnter;
    }

    public String parseNumverToEnter(String step, int index) {
        String numbers;

        //extracting string
        numbers = step.replaceAll("[^-?0-9]+", " ");

        System.out.println("Number is: " + Arrays.asList(numbers.trim().split(" ")).get(index));

        return Arrays.asList(numbers.trim().split(" ")).get(index);
    }

}
