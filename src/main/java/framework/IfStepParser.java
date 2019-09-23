package framework;

import Selenium.Commands;
import org.json.simple.JSONObject;
import org.openqa.selenium.WebDriver;

/**
 * Created by QAble on 9/23/2019.
 */
public class IfStepParser {

    public boolean parseIfStep(WebDriver driver, JSONObject test, String step) throws Exception {
        Commands cmd = new Commands();
        GetLocator locator = new GetLocator();
        StepParser stepParser=new StepParser();
        boolean isIfCondition=false;

        if(step.toLowerCase().contains("displayed") || step.toLowerCase().contains("present")){
          if(cmd.findElement(driver, locator.getLocatorValue(test.get("suiteName").toString(), parseElementName(step,1))).isDisplayed()){
              isIfCondition=true;
          }
        }
        else if(step.toLowerCase().contains("text")){
            String textOfStep = stepParser.parseTextToEnter(test, step);
            if(step.toLowerCase().contains("equal")){
                if(elementCountInStep(step)==2){
                    String firstElementText=cmd.findElement(driver, locator.getLocatorValue(test.get("suiteName").toString(), parseElementName(step, 1))).getText();
                    String SecondElementText=cmd.findElement(driver, locator.getLocatorValue(test.get("suiteName").toString(), parseElementName(step, 2))).getText();


                    if (step.toLowerCase().contains("ignore case")) {
                        if (firstElementText.equalsIgnoreCase(SecondElementText)) {
                            isIfCondition = true;
                        }
                    } else {
                        if (firstElementText.equals(SecondElementText)) {
                            isIfCondition = true;
                        }
                    }
                }else {
                    if (step.toLowerCase().contains("ignore case")) {
                        if (cmd.findElement(driver, locator.getLocatorValue(test.get("suiteName").toString(), parseElementName(step, 1))).getText().equalsIgnoreCase(textOfStep)) {
                            isIfCondition = true;
                        }
                    } else {
                        if (cmd.findElement(driver, locator.getLocatorValue(test.get("suiteName").toString(), parseElementName(step, 1))).getText().equals(textOfStep)) {
                            isIfCondition = true;
                        }
                    }
                }
            }
            else if(step.toLowerCase().contains("contains")){
                if (cmd.findElement(driver, locator.getLocatorValue(test.get("suiteName").toString(), parseElementName(step, 1))).getText().contains(textOfStep)) {
                    isIfCondition = true;
                }
            }

        }


        return isIfCondition;

    }

    public String parseElementName(String step, int elementNumber) {

        String[] stepWordList = step.split("::|\\s+");

        String elementName = "";
        int count=1;
        for (String word : stepWordList) {
            if (word.contains("@")) {
                if(count==elementNumber) {
                    elementName = word.substring(1);
                    break;
                }
                count++;
            }
        }
        return elementName;
    }

    public int elementCountInStep(String step) {
        String[] stepWordList = step.split("::|\\s+");
        int count=0;
        for (String word : stepWordList) {
            if (word.contains("@")) {
                count++;
            }
        }
        return count;
    }
}
