package framework;

import Selenium.Commands;
import org.json.simple.JSONObject;
import org.openqa.selenium.WebDriver;
import Exception.*;

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
            /*
            * If:: @element is displayed
            * If:: @element is present
            * */
            if(cmd.findElement(driver, locator.getLocatorValue(test.get("suiteName").toString(), parseElementName(step,1))+"_IF").isDisplayed()){
              isIfCondition=true;
          }
        }
        else if(step.toLowerCase().contains("text")){
            String textOfStep = stepParser.parseTextToEnter(test, step);
            if(step.toLowerCase().contains("equal")){
                if(elementCountInStep(step)==2){
                    String firstElementText=cmd.findElement(driver, locator.getLocatorValue(test.get("suiteName").toString(), parseElementName(step, 1))+"_IF").getText();
                    String SecondElementText=cmd.findElement(driver, locator.getLocatorValue(test.get("suiteName").toString(), parseElementName(step, 2))+"_IF").getText();

                    if (step.toLowerCase().contains("ignore case")) {
                        /*
                        * If:: @element text is equal to ignore case @element2 text
                        * */
                        if (firstElementText.equalsIgnoreCase(SecondElementText)) {
                            isIfCondition = true;
                        }
                    } else {

                        /*
                        * If:: @element text is equal to @element2 text
                        * */
                        if (firstElementText.equals(SecondElementText)) {
                            isIfCondition = true;
                        }
                    }
                }else {
                    if (step.toLowerCase().contains("ignore case")) {
                        /*
                        * If:: @element text is equal to ignore case 'any text'
                        * */
                        if (cmd.findElement(driver, locator.getLocatorValue(test.get("suiteName").toString(), parseElementName(step, 1))+"_IF").getText().equalsIgnoreCase(textOfStep)) {
                            isIfCondition = true;
                        }
                    } else {
                        /*
                        * If:: @element text is equal to 'any text'
                        * */
                        if (cmd.findElement(driver, locator.getLocatorValue(test.get("suiteName").toString(), parseElementName(step, 1))+"_IF").getText().equals(textOfStep)) {
                            isIfCondition = true;
                        }
                    }
                }
            }
            else if(step.toLowerCase().contains("contains")){
                /*
                * If:: @element text contains is 'any text'
                * */
                if (cmd.findElement(driver, locator.getLocatorValue(test.get("suiteName").toString(), parseElementName(step, 1))+"_IF").getText().contains(textOfStep)) {
                    isIfCondition = true;
                }
            }
            else if(step.toLowerCase().contains("grater then") || step.toLowerCase().contains("lass then")){
                int number;
                int elementNumber;

                try{number=Integer.parseInt(textOfStep);}
                catch (Exception e){throw new TesboException("Please enter numeric value in if condition");}

                try{elementNumber= Integer.parseInt(locator.getLocatorValue(test.get("suiteName").toString(), parseElementName(step, 1))+"_IF");}
                catch (Exception e) {throw new TesboException("Given element has not numeric value: "+parseElementName(step, 1));}

                /*
                * If:: @element text number is grater then equal to 'any number'
                * */
                if(step.toLowerCase().contains("grater then") && step.toLowerCase().contains("equal to")){
                    if(elementNumber>=number){isIfCondition=true;}
                }
                else if(step.toLowerCase().contains("grater then")){
                    /*
                    * If:: @element text number is grater then 'any number'
                    * */
                    if(elementNumber>number){isIfCondition=true;}
                }
                else if(step.toLowerCase().contains("less then") && step.toLowerCase().contains("equal to")){
                    /*
                    * If:: @element text number is less then equal to 'any number'
                    * */
                    if(elementNumber<=number){isIfCondition=true;}
                }
                else if(step.toLowerCase().contains("less then")){
                    /*
                    * If:: @element text number is less then 'any number'
                    * */
                    if(elementNumber<number){isIfCondition=true;}
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
