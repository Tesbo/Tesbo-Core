package framework;

import Selenium.Commands;
import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;
import org.json.simple.JSONObject;
import org.openqa.selenium.WebDriver;
import Exception.*;

import javax.swing.plaf.synth.SynthOptionPaneUI;

/**
 * Created by QAble on 9/23/2019.
 */
public class IfStepParser {

    public boolean parseIfStep(WebDriver driver, JSONObject test, String step) throws Exception {
        Commands cmd = new Commands();
        GetLocator locator = new GetLocator();
        StepParser stepParser=new StepParser();
        boolean isIfCondition=false;
        String conditionType=checkStepHasAndOrCondition(step);
        if(conditionType.equals("and") | conditionType.equals("or")){
            String[] Steps=getListOfSteps(step);
            for(String newStep:Steps){
                boolean isIf=false;
                try {isIf = parseIfStep(driver, test, newStep);}catch (Exception e){}
                if(conditionType.equals("and")){
                    if(isIf){isIfCondition= isIf;}
                    else{
                        isIfCondition=isIf;
                        break;
                    }
                }
                else{
                    if(isIf){
                        isIfCondition=isIf;
                        break;
                    }
                }
            }
        }
        else {
            if(step.toLowerCase().contains("displayed") || step.toLowerCase().contains("present")){
            /*
            * If:: @element is displayed
            * If:: @element is present
            * */
                if(cmd.findElement(driver, locator.getLocatorValue(test.get("suiteName").toString(), parseElementName(step,1))+"_IF").isDisplayed()){
                    isIfCondition=true;
                }
            }
            else if(step.toLowerCase().contains("grater then") || step.toLowerCase().contains("less then")){
                int number;
                int elementNumber;

                String textOfStep = stepParser.parseTextToEnter(test, step);

                try{number=Integer.parseInt(textOfStep);}
                catch (Exception e){throw new TesboException("Please enter numeric value in if condition");}

                try{elementNumber= Integer.parseInt(cmd.findElement(driver, locator.getLocatorValue(test.get("suiteName").toString(), parseElementName(step, 1))+"_IF").getText());}
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
            else if(step.toLowerCase().contains("text")){
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
                        String textOfStep = stepParser.parseTextToEnter(test, step);
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
                            if (cmd.findElement(driver, locator.getLocatorValue(test.get("suiteName").toString(), parseElementName(step, 1)) + "_IF").getText().equals(textOfStep)) {
                                isIfCondition = true;
                            }
                        }
                    }
                }
                else if(step.toLowerCase().contains("contains")){
                    String textOfStep = stepParser.parseTextToEnter(test, step);
                /*
                * If:: @element text contains is 'any text'
                * */
                    if (cmd.findElement(driver, locator.getLocatorValue(test.get("suiteName").toString(), parseElementName(step, 1))+"_IF").getText().contains(textOfStep)) {
                        isIfCondition = true;
                    }
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

    public String checkStepHasAndOrCondition(String step) {
        String conditionType="";
        StepParser stepParser=new StepParser();
        boolean flag=false;
        if(step.contains("'")) {
            /*int startPoint = step.indexOf("'") + 1;
            int endPoint = step.lastIndexOf("'");
            String text = step.substring(startPoint, endPoint);
            step = step.replace(text, "");*/

           step= stepParser.RemovedVerificationTextFromSteps(step);
        }
       if(step.toLowerCase().contains(" and ")){
           conditionType="and";
           flag=true;
       }
       if(step.toLowerCase().contains(" or ")){
           if(flag){
               throw new TesboException("You can not use 'And' 'Or' condition in same if condition");
           }
           conditionType="or";
       }

        return conditionType;
    }

    public String[] getListOfSteps(String step) {

        String[] stepWordList = step.split(" And | AND | and | OR | or | Or | oR ");
        String steps[]=new String[stepWordList.length];
        for(int i=0; i<stepWordList.length;i++){
            if(i==0){steps[i]= stepWordList[i];}
            else{
                steps[i]= "If:: "+stepWordList[i];
            }
        }

        return steps;
    }

}
