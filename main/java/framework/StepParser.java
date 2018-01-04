package framework;

import Selenium.Commands;
import org.openqa.selenium.WebDriver;

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

        //Is displayed

        if (step.toLowerCase().contains("displayed")) {
            try {
                if (step.toLowerCase().contains("should") || step.toLowerCase().contains("is")) {
                    assertThat(cmd.findElement(driver, locator.getLocatorValue(suiteName, parseElementName(step))).isDisplayed()).isEqualTo(true);
                }

            } catch (Exception e) {
                System.out.println("Step Failed");
                e.printStackTrace();
            }
        }


        //Switch
        if (step.toLowerCase().contains("switch")) {
            switchFunction(driver, suiteName, step);
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


            if (step.toLowerCase().contains("alert")) {
                //develop new alert method
            }


            //Step :  switch to default content

            if (step.toLowerCase().contains("default content")) {
                cmd.switchToDefaultContent(driver);
            }

            if (step.toLowerCase().contains("active element")) {

            }

/*
Switch to frame
 */
            if (step.toLowerCase().contains("frame")) {


            }



/*
Switch to new Window
 */


            if (step.toLowerCase().contains("window")) {


            }


        } catch (Exception e) {
            System.out.println("Step Failed");
            e.printStackTrace();
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


}
