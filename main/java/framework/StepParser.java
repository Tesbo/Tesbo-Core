package framework;

import Selenium.Commands;
import org.openqa.selenium.WebDriver;

public class StepParser {


    public void parseStep(WebDriver driver, String step)

    {
        Commands cmd = new Commands();

        System.out.println("ele "+parseElementName(step));
        if (step.contains("click")) {

            cmd.findElement(driver,parseElementName(step)).click();

        }

    }


    public String parseElementName(String step) {

        String[] stepWordList = step.split("[\\r\\n]+");

        String elementName = "";

        for (String word : stepWordList) {
            if (word.contains("@")) {
                elementName = word.substring(1);
            }
            System.out.println(elementName);
        }
        return elementName;
    }


}
