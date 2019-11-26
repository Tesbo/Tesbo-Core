package framework;

import Selenium.Commands;
import logger.TesboLogger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.openqa.selenium.WebDriver;
import Exception.*;

import java.io.PrintWriter;
import java.io.StringWriter;


/**
 * Created by Ankit Mistry on 9/23/2019.
 */
public class IfStepParser {
    public static boolean isIfError = false;
    TesboLogger tesboLogger = new TesboLogger();
    private static final Logger log = LogManager.getLogger(TestExecutor.class);
    StringWriter sw = new StringWriter();
    String exceptionAsString = null;

    public boolean parseIfStep(WebDriver driver, JSONObject test, String step) throws Exception {
        Commands cmd = new Commands();
        GetLocator locator = new GetLocator();
        StepParser stepParser = new StepParser();
        boolean isIfCondition = false;
        String conditionType = checkStepHasAndOrCondition(step);
        if (conditionType.equals("and") | conditionType.equals("or")) {
            String[] Steps = getListOfSteps(step);
            for (String newStep : Steps) {
                boolean isIf = false;
                try {
                    isIf = parseIfStep(driver, test, newStep);
                } catch (Exception e) {
                }
                if (conditionType.equals("and")) {
                    if (isIf) {
                        isIfCondition = isIf;
                    } else {
                        isIfCondition = isIf;
                        break;
                    }
                } else {
                    if (isIf) {
                        isIfCondition = isIf;
                        break;
                    }
                }
            }
        } else {
            if (step.toLowerCase().contains("displayed") || step.toLowerCase().contains("present")) {
                /*
                 * If:: @element is displayed
                 * If:: @element is present
                 * */
                if (cmd.findElement(driver, locator.getLocatorValue(test.get("testsFileName").toString(), parseElementName(step, 1)) + "_IF").isDisplayed()) {
                    isIfCondition = true;
                }
            } else if (step.toLowerCase().contains("grater then") || step.toLowerCase().contains("less then")) {
                int number;
                int elementNumber;

                String textOfStep = stepParser.parseTextToEnter(test, step);

                try {
                    number = Integer.parseInt(textOfStep);
                } catch (Exception e) {
                    isIfError = true;
                    throw new TesboException("Please enter numeric value for verification in if condition");
                }

                try {
                    elementNumber = Integer.parseInt(cmd.findElement(driver, locator.getLocatorValue(test.get("testsFileName").toString(), parseElementName(step, 1)) + "_IF").getText());
                } catch (Exception e) {
                    isIfError = true;
                    throw new TesboException("Given element has not numeric value: " + parseElementName(step, 1));
                }

                /*
                 * If:: @element text number is greater than equal to 'any number'
                 * */
                if (step.toLowerCase().contains("greater than") && step.toLowerCase().contains("equal to")) {
                    if (elementNumber >= number) {
                        isIfCondition = true;
                    }
                } else if (step.toLowerCase().contains("greater than")) {
                    /*
                     * If:: @element text number is greater than 'any number'
                     * */
                    if (elementNumber > number) {
                        isIfCondition = true;
                    }
                } else if (step.toLowerCase().contains("less than") && step.toLowerCase().contains("equal to")) {
                    /*
                     * If:: @element text number is less than equal to 'any number'
                     * */
                    if (elementNumber <= number) {
                        isIfCondition = true;
                    }
                } else if (step.toLowerCase().contains("less than")) {
                    /*
                     * If:: @element text number is less than 'any number'
                     * */
                    if (elementNumber < number) {
                        isIfCondition = true;
                    }
                }
            } else if (step.toLowerCase().contains("text")) {
                if (step.toLowerCase().contains("equal")) {
                    if (elementCountInStep(step) == 2) {
                        String firstElementText = cmd.findElement(driver, locator.getLocatorValue(test.get("testsFileName").toString(), parseElementName(step, 1)) + "_IF").getText();
                        String SecondElementText = cmd.findElement(driver, locator.getLocatorValue(test.get("testsFileName").toString(), parseElementName(step, 2)) + "_IF").getText();

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
                    } else {
                        String textOfStep = stepParser.parseTextToEnter(test, step);
                        if (step.toLowerCase().contains("ignore case")) {
                            /*
                             * If:: @element text is equal to ignore case 'any text'
                             * */
                            if (cmd.findElement(driver, locator.getLocatorValue(test.get("testsFileName").toString(), parseElementName(step, 1)) + "_IF").getText().equalsIgnoreCase(textOfStep)) {
                                isIfCondition = true;
                            }
                        } else {
                            /*
                             * If:: @element text is equal to 'any text'
                             * */
                            if (cmd.findElement(driver, locator.getLocatorValue(test.get("testsFileName").toString(), parseElementName(step, 1)) + "_IF").getText().equals(textOfStep)) {
                                isIfCondition = true;
                            }
                        }
                    }
                } else if (step.toLowerCase().contains("contains")) {
                    String textOfStep = stepParser.parseTextToEnter(test, step);
                    /*
                     * If:: @element text contains is 'any text'
                     * */
                    if (cmd.findElement(driver, locator.getLocatorValue(test.get("testsFileName").toString(), parseElementName(step, 1)) + "_IF").getText().contains(textOfStep)) {
                        isIfCondition = true;
                    }
                }
            } else if (step.toLowerCase().contains("is") && (step.toLowerCase().contains("checked") | step.toLowerCase().contains("selected"))) {
                /*
                 * If:: @element is checked
                 * If:: @element is selected
                 * */
                if (cmd.findElement(driver, locator.getLocatorValue(test.get("testsFileName").toString(), parseElementName(step, 1)) + "_IF").isSelected()) {
                    isIfCondition = true;
                }
            } else if (step.toLowerCase().contains("is") && step.toLowerCase().contains("enabled")) {
                /*
                 * If:: @element is enabled
                 * */
                if (cmd.findElement(driver, locator.getLocatorValue(test.get("testsFileName").toString(), parseElementName(step, 1)) + "_IF").isEnabled()) {
                    isIfCondition = true;
                }
            }

        }
        return isIfCondition;
    }

    public String parseElementName(String step, int elementNumber) {
        String[] stepWordList = step.split("::|\\s+");

        String elementName = "";
        int count = 1;
        for (String word : stepWordList) {
            if (word.contains("@")) {
                if (count == elementNumber) {
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
        int count = 0;
        for (String word : stepWordList) {
            if (word.contains("@")) {
                count++;
            }
        }
        return count;
    }

    public String checkStepHasAndOrCondition(String step) {
        String conditionType = "";
        StepParser stepParser = new StepParser();
        boolean flag = false;
        if (step.contains("'")) {
            step = stepParser.RemovedVerificationTextFromSteps(step);
        }
        if (step.toLowerCase().contains(" and ")) {
            conditionType = "and";
            flag = true;
        }
        if (step.toLowerCase().contains(" or ")) {
            if (flag) {
                isIfError = true;
                throw new TesboException("You can not use 'And' 'Or' condition in same if condition");
            }
            conditionType = "or";
        }
        return conditionType;
    }

    public String[] getListOfSteps(String step) {

        String[] stepWordList = step.split(" And | AND | and | OR | or | Or | oR ");
        String steps[] = new String[stepWordList.length];
        for (int i = 0; i < stepWordList.length; i++) {
            if (i == 0) {
                steps[i] = stepWordList[i];
            } else {
                steps[i] = "If:: " + stepWordList[i];
            }
        }
        return steps;
    }

    public boolean isTestsHasIFCondition(JSONArray steps) {

        for (Object step : steps) {
            if (step.toString().contains("Else If::") && !step.toString().contains("If::")) {
                log.info("If:: condition is not found for '" + step + "' step.");
                throw new TesboException("If:: condition is not found for '" + step + "' step.");
            }
            if (step.toString().contains("If::")) {
                return true;
            }
        }
        return false;
    }

    public void isEndStepForIfCondition(JSONArray steps) {
        int countForIf = 0;
        int countForEnd = 0;
        for (Object step : steps) {
            if (step.toString().contains("If::") && !step.toString().contains("Else If::")) {
                countForIf++;
            }
            if (step.toString().contains("End::") && !step.toString().contains("End")) {
                countForEnd++;
            }
        }
        if (countForIf != countForEnd) {
            log.info("End:: step is not found for If:: condition.");
            throw new TesboException("End:: step is not found for If:: condition.");
        }
    }

    String ifCondition = "";
    String elseCondition="";
    String elseIFCondition="";

    public JSONArray getStepsOfTestWhoHasIfCondition(WebDriver driver, JSONObject test, JSONArray steps) {
        JSONArray newStep = new JSONArray();
        ifCondition = "";
        elseCondition="";
        elseIFCondition="";
        boolean nestedIf=false;
        for (int i = 0; i < steps.size(); i++) {
            if (steps.get(i).toString().contains("If::") && !(steps.get(i).toString().contains("Else If::"))) {
                try {
                    if (parseIfStep(driver, test, steps.get(i).toString())) {
                        ifCondition = "pass";
                        elseCondition="fail";
                        elseIFCondition="fail";
                        for (int j = i + 1; j < steps.size(); j++) {

                            if(nestedIf){
                                nestedIf=isNestedIF(steps.get(j).toString(),nestedIf);
                                newStep.add(steps.get(j));
                                continue;
                            }
                            else{
                                nestedIf=isNestedIF(steps.get(j).toString(),nestedIf);
                                if(nestedIf) {
                                    newStep.add(steps.get(j));
                                    continue;
                                }
                            }

                            if (!(steps.get(j).toString().contains("End::") | steps.get(j).toString().contains("Else::") | steps.get(j).toString().contains("Else If::"))) {
                                newStep.add(steps.get(j));
                            } else {
                                i = j;
                                break;
                            }
                        }
                    }
                    else{
                        ifCondition = "fail";
                        i=skipIfConditionStep(steps,i+1);
                        continue;
                    }
                } catch (Exception e) {
                    ifCondition = "fail";
                    if (isIfError) {
                        tesboLogger.testLog(steps.get(i).toString());
                        e.printStackTrace(new PrintWriter(sw));
                        exceptionAsString = sw.toString();
                        tesboLogger.testFailed("Failed");
                        tesboLogger.testFailed(exceptionAsString);
                        log.error(steps.get(i).toString());
                        log.error("Failed");
                        log.error(exceptionAsString);
                        break;
                    }
                    i=skipIfConditionStep(steps,i+1);
                    continue;
                }

            }
            if (steps.get(i).toString().contains("Else If::") && ifCondition.equals("fail") && !(elseIFCondition.equals("pass"))) {
                try {
                    if (parseIfStep(driver, test, steps.get(i).toString())) {
                        elseIFCondition = "pass";
                        elseCondition="fail";
                        for (int j = i + 1; j < steps.size(); j++) {

                            if(nestedIf){
                                nestedIf=isNestedIF(steps.get(j).toString(),nestedIf);
                                newStep.add(steps.get(j));
                                continue;
                            }
                            else{
                                nestedIf=isNestedIF(steps.get(j).toString(),nestedIf);
                                if(nestedIf) {
                                    newStep.add(steps.get(j));
                                    continue;
                                }
                            }

                            if (!(steps.get(j).toString().contains("End::") | steps.get(j).toString().contains("Else::") | steps.get(j).toString().contains("Else If::"))) {
                                newStep.add(steps.get(j));
                            } else {
                                i = j;
                                break;
                            }
                        }
                    }
                    else{
                        elseIFCondition = "fail";
                        i=skipIfConditionStep(steps,i+1);
                        continue;
                    }
                } catch (Exception e) {
                    elseIFCondition = "fail";
                    if (isIfError) {
                        tesboLogger.testLog(steps.get(i).toString());
                        e.printStackTrace(new PrintWriter(sw));
                        exceptionAsString = sw.toString();
                        tesboLogger.testFailed("Failed");
                        tesboLogger.testFailed(exceptionAsString);
                        log.error(steps.get(i).toString());
                        log.error("Failed");
                        log.error(exceptionAsString);
                        break;
                    }
                    i=skipIfConditionStep(steps,i+1);
                    continue;
                }
            }

            if (steps.get(i).toString().contains("Else::") && !(steps.get(i).toString().contains("Else If::")) && (ifCondition.equals("fail") | elseIFCondition.equals("fail"))) {
                elseCondition = "pass";
                for (int j = i + 1; j < steps.size(); j++) {
                    if (!steps.get(j).toString().contains("End::")) {
                        newStep.add(steps.get(j));
                    } else {
                        i = j;
                        break;
                    }
                }
            }

            if (steps.get(i).toString().contains("End::")) {
                ifCondition ="";
                elseCondition="";
                elseIFCondition="";
                for (int j = i + 1; j < steps.size(); j++) {
                    newStep.add(steps.get(j));
                }
                break;
            }

            if (!(ifCondition.equals("fail") | elseCondition.equals("fail") | elseIFCondition.equals("fail"))) {
                newStep.add(steps.get(i));
            }
        }
        //System.out.println("=====> "+newStep);
        return newStep;
    }
    int countif=0;
    public boolean isNestedIF(String step, boolean nestedIf){
        //boolean nestedIf=false;

        if(step.contains("If::") && !(step.contains("Else If::")))
        {
            nestedIf= true;
            countif++;
        }
        if(nestedIf){
            if(step.contains("End::"))
            {
                countif--;
                if(countif==0) {
                    nestedIf = false;
                }
            }

        }
        return nestedIf;
    }

    public int skipIfConditionStep(JSONArray steps, int startingStep){
        int countIf=0;
        for (int j = startingStep; j < steps.size(); j++) {
            if(steps.get(j).toString().contains("If::") && !(steps.get(j).toString().contains("Else If::"))){
                countIf++;
                continue;
            }
            if(steps.get(j).toString().contains("End::")){
                if(countIf==0) {
                    ifCondition ="";
                    elseCondition="";
                    elseIFCondition="";
                    startingStep = j;
                    break;
                }
                else {
                    countIf--;
                }
                continue;
            }
            if(steps.get(j).toString().contains("Else::") | steps.get(j).toString().contains("Else If::")){
                if(countIf==0) { startingStep = j-1; break; }
            }
        }
        return startingStep;
    }


    public int getEndStepForCondition ( int startIndex, int endIndex, JSONArray steps){
            int endStepCondition=0;
            //JSONArray newStep=new JSONArray();
            boolean flag = false;
            int ifCount = 0;
            int endCount = 0;

            for (int i = startIndex; i <= endIndex; i++) {
                if (steps.get(i).toString().contains("If::")) {
                    ifCount++;
                }
                if (steps.get(i).toString().contains("End::")) {
                    endCount++;
                    //if(endCount>ifCount)
                }


            }

            return endStepCondition;
        }


}