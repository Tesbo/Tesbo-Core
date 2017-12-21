package framework;

import Selenium.Commands;
import org.openqa.selenium.WebDriver;
import sun.applet.Main;

public class StepParser {


    public static void main(String[] args) {
        StepParser parser = new StepParser();

        parser.parseTextToEnter("Verify that @email was received");
    }

    public void parseStep(WebDriver driver, String step)

    {
        Commands cmd = new Commands();

        System.out.println("ele " + parseElementName(step));
        if (step.contains("click")) {
            cmd.findElement(driver, parseElementName(step)).click();
        }

        if (step.contains("enter")) {
           cmd.findElement(driver,parseElementName(step)).sendKeys(parseTextToEnter(step));
        }


    }


    public String parseElementName(String step) {

        String[] stepWordList = step.split("\\s+");

        String elementName = "";

        for (String word : stepWordList) {
            if (word.contains("@")) {
                elementName = word.substring(1);
            }
            System.out.println(elementName);
        }
        return elementName;
    }

    public String parseTextToEnter(String step) {
        String textToEnter = "";

        String[] stepWordList = step.split("\\s+");

        for (String word : stepWordList) {

            if (word.contains("'")) {
                int length = word.length()-1;
                textToEnter = word.substring(1,length);
            }

        }
        System.out.println("Text to enter " +textToEnter);
        return textToEnter;
    }


}
