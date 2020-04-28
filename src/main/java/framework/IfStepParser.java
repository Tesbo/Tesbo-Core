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
    private static final Logger log = LogManager.getLogger(IfStepParser.class);
    StringWriter sw = new StringWriter();
    String exceptionAsString = null;
    String exception="Exception.TesboException: ";
    String ifCon="If:: ";
    String elseIfCon="Else If:: ";
    String end="End::";
    String elseCon="Else::";

    public boolean parseIfStep(WebDriver driver, JSONObject test, String step) throws Exception {
        Commands cmd = new Commands();
        GetLocator locator = new GetLocator();
        StepParser stepParser = new StepParser();
        String testsFileName=test.get("testsFileName").toString();
        boolean isIfCondition = false;
        String greaterThan="greater than";
        String lessThan="less than";

        String conditionType = checkStepHasAndOrCondition(step);
        if (conditionType.equals("and") || conditionType.equals("or")) {
            String[] steps = getListOfSteps(step);
            for (String newStep : steps) {
                boolean isIf = false;
                try {
                    isIf = parseIfStep(driver, test, newStep);
                } catch (Exception e) {
                    if (isIfError) {
                        e.printStackTrace(new PrintWriter(sw));
                        exceptionAsString = sw.toString();
                        throw new TesboException(exceptionAsString.split("\\n")[0].replaceAll(exception,""));
                    }
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
                    }
                }
            }
        } else {
            if (step.toLowerCase().contains("displayed") || step.toLowerCase().contains("present")) {
                /*
                 * If:: @element is displayed
                 * If:: @element is present
                 * */
                if (cmd.findElement(driver, locator.getLocatorValue(testsFileName, parseElementName(step, 1)) + "_IF").isDisplayed()) {
                    isIfCondition = true;
                }
            }
            else if (step.toLowerCase().contains(greaterThan) || step.toLowerCase().contains(lessThan)) {
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
                    elementNumber = Integer.parseInt(cmd.findElement(driver, locator.getLocatorValue(testsFileName, parseElementName(step, 1)) + "_IF").getText());
                } catch (Exception e) {
                    isIfError = true;
                    throw new TesboException("Given element has not numeric value: " + parseElementName(step, 1));
                }

                /*
                 * If:: @element text number is greater than equal to 'any number'
                 * */
                if (step.toLowerCase().contains(greaterThan) && step.toLowerCase().contains("equal to")) {
                    if (elementNumber >= number) {
                        isIfCondition = true;
                    }
                } else if (step.toLowerCase().contains(greaterThan)) {
                    /*
                     * If:: @element text number is greater than 'any number'
                     * */
                    if (elementNumber > number) {
                        isIfCondition = true;
                    }
                } else if (step.toLowerCase().contains(lessThan) && step.toLowerCase().contains("equal to")) {
                    /*
                     * If:: @element text number is less than equal to 'any number'
                     * */
                    if (elementNumber <= number) {
                        isIfCondition = true;
                    }
                } else if (step.toLowerCase().contains(lessThan) && elementNumber < number) {
                    /*
                     * If:: @element text number is less than 'any number'
                     * */
                        isIfCondition = true;
                }
            }
            else if (step.toLowerCase().contains("text")) {
                if (step.toLowerCase().contains("equal")) {
                    if (elementCountInStep(step) == 2) {
                        String firstElementText = cmd.findElement(driver, locator.getLocatorValue(testsFileName, parseElementName(step, 1)) + "_IF").getText();
                        String secondElementText = cmd.findElement(driver, locator.getLocatorValue(testsFileName, parseElementName(step, 2)) + "_IF").getText();
                        if (step.toLowerCase().contains("ignore case")) {
                            /*
                             * If:: @element text is equal to ignore case @element2 text
                             * */
                            if (firstElementText.equalsIgnoreCase(secondElementText)) {
                                isIfCondition = true;
                            }
                        } else {

                            /*
                             * If:: @element text is equal to @element2 text
                             * */
                            if (firstElementText.equals(secondElementText)) {
                                isIfCondition = true;
                            }
                        }
                    } else {
                        String textOfStep = stepParser.parseTextToEnter(test, step);
                        if (step.toLowerCase().contains("ignore case")) {
                            /*
                             * If:: @element text is equal to ignore case 'any text'
                             * */
                            if (cmd.findElement(driver, locator.getLocatorValue(testsFileName, parseElementName(step, 1)) + "_IF").getText().equalsIgnoreCase(textOfStep)) {
                                isIfCondition = true;
                            }
                        } else {
                            /*
                             * If:: @element text is equal to 'any text'
                             * */
                            if (cmd.findElement(driver, locator.getLocatorValue(testsFileName, parseElementName(step, 1)) + "_IF").getText().equals(textOfStep)) {
                                isIfCondition = true;
                            }
                        }
                    }
                } else if (step.toLowerCase().contains("contains")) {
                    String textOfStep = stepParser.parseTextToEnter(test, step);
                    /*
                     * If:: @element text contains is 'any text'
                     * */
                    if (cmd.findElement(driver, locator.getLocatorValue(testsFileName, parseElementName(step, 1)) + "_IF").getText().contains(textOfStep)) {
                        isIfCondition = true;
                    }
                }
            } else if (step.toLowerCase().contains("is") && (step.toLowerCase().contains("checked") || step.toLowerCase().contains("selected"))) {
                /*
                 * If:: @element is checked
                 * If:: @element is selected
                 * */
                if (cmd.findElement(driver, locator.getLocatorValue(testsFileName, parseElementName(step, 1)) + "_IF").isSelected()) {
                    isIfCondition = true;
                }
            } else if (step.toLowerCase().contains("is") && step.toLowerCase().contains("enabled") && cmd.findElement(driver, locator.getLocatorValue(testsFileName, parseElementName(step, 1)) + "_IF").isEnabled()) {
                /*
                 * If:: @element is enabled
                 * */
                isIfCondition = true;
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
        String[] steps = new String[stepWordList.length];
        for (int i = 0; i < stepWordList.length; i++) {
            if (i == 0) {
                steps[i] = stepWordList[i];
            } else {
                steps[i] = ifCon + stepWordList[i];
            }
        }
        return steps;
    }

    public boolean isTestsHasIFCondition(JSONArray steps) {

        for (Object step : steps) {
            if (step.toString().startsWith(elseIfCon) && !step.toString().startsWith(ifCon)) {
                String errorMsg="If:: condition is not found for '" + step + "' step.";
                log.info(errorMsg);
                throw new TesboException(errorMsg);
            }
            if (step.toString().startsWith(ifCon)) {
                return true;
            }
        }
        return false;
    }

    public void isEndStepForIfCondition(JSONArray steps, String test) {
        int countForIf = 0;
        int countForEnd = 0;
        for (Object step : steps) {
            if (step.toString().startsWith(ifCon) && !step.toString().startsWith(elseIfCon)) {
                countForIf++;
            }
            if (step.toString().contains(end)) {
                countForEnd++;
            }
        }
        if(countForIf>1 && (!(countForIf-1 == countForEnd || countForIf == countForEnd))) {
            String errorMsg="End:: step not found for If:: condition of 'Test: "+test+"'";
            log.info(errorMsg);
            throw new TesboException(errorMsg);
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
            if (steps.get(i).toString().startsWith(ifCon) && !(steps.get(i).toString().startsWith(elseIfCon))) {
                if(steps.get(i).toString().trim().split("::").length != 2){
                    log.info("Condition is not found for If:: step OR something wrong in If condition");
                    throw new TesboException("Condition is not found for If:: step OR something wrong in If condition");
                }
                try {
                    if (parseIfStep(driver, test, steps.get(i).toString())) {
                        ifCondition = "pass";
                        elseCondition="fail";
                        elseIFCondition="fail";
                        for (int j = i + 1; j < steps.size(); j++) {
                            if(j==steps.size()-1){ i=j; }
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

                            if (!(steps.get(j).toString().startsWith(end) || steps.get(j).toString().startsWith(elseCon) || steps.get(j).toString().startsWith(elseIfCon))) {
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
                        nestedIf=false;
                        continue;
                    }
                } catch (Exception e) {
                    ifCondition = "fail";
                    if (isIfError) {
                        tesboLogger.testLog(steps.get(i).toString());
                        e.printStackTrace(new PrintWriter(sw));
                        exceptionAsString = sw.toString();
                        log.error(steps.get(i).toString());
                        throw new TesboException(exceptionAsString.split("\\n")[0].replaceAll(exception,""));
                    }
                    i=skipIfConditionStep(steps,i+1);
                    nestedIf=false;
                    continue;
                }

            }
            if (steps.get(i).toString().startsWith(elseIfCon) && ifCondition.equals("fail") && !(elseIFCondition.equals("pass"))) {
                if(steps.get(i).toString().trim().split("::").length != 2){
                    log.info("Condition is not found for Else If:: step OR something wrong in Else If condition");
                    throw new TesboException("Condition is not found for Else If:: step OR something wrong in Else If condition");
                }
                try {
                    if (parseIfStep(driver, test, steps.get(i).toString())) {
                        elseIFCondition = "pass";
                        elseCondition="fail";
                        for (int j = i + 1; j < steps.size(); j++) {
                            if(j==steps.size()-1){ i=j; }
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

                            if (!(steps.get(j).toString().startsWith(end) || steps.get(j).toString().startsWith(elseCon) || steps.get(j).toString().startsWith(elseIfCon))) {
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
                        log.error(steps.get(i).toString());
                        throw new TesboException(exceptionAsString.split("\\n")[0].replaceAll(exception,""));
                    }
                    i=skipIfConditionStep(steps,i+1);
                    continue;
                }
            }

            if (steps.get(i).toString().startsWith(elseCon) && !(steps.get(i).toString().startsWith(elseIfCon)) && !(elseCondition.equals("fail"))) {
                elseCondition = "pass";
                for (int j = i + 1; j < steps.size(); j++) {
                    if(j==steps.size()-1){ i=j; }

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
                    if (!steps.get(j).toString().contains(end)) {
                        newStep.add(steps.get(j));
                    } else {
                        i = j;
                        break;
                    }
                }
            }

            if (steps.get(i).toString().contains(end)) {
                ifCondition ="";
                elseCondition="";
                elseIFCondition="";
                for (int j = i + 1; j < steps.size(); j++) {
                    newStep.add(steps.get(j));
                }
                break;
            }

            if (!(ifCondition.equals("fail") || elseCondition.equals("fail") || elseIFCondition.equals("fail"))) {
                newStep.add(steps.get(i));
            }
        }
        return newStep;
    }
    int countif=0;
    public boolean isNestedIF(String step, boolean nestedIf){

        if(step.startsWith(ifCon) && !(step.startsWith(elseIfCon)))
        {
            nestedIf= true;
            countif++;
        }
        if(nestedIf && step.contains(end)){

            countif--;
            if(countif==0) {
                nestedIf = false;
            }
        }
        return nestedIf;
    }

    public int skipIfConditionStep(JSONArray steps, int startingStep){
        int countIf=0;
        for (int j = startingStep; j < steps.size(); j++) {
            if(steps.get(j).toString().startsWith(ifCon) && !(steps.get(j).toString().startsWith(elseIfCon))){
                countIf++;
                continue;
            }
            if(steps.get(j).toString().contains(end)){
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
            if((steps.get(j).toString().startsWith("Else:: ") || steps.get(j).toString().startsWith(elseIfCon)) && countIf==0){
                 startingStep = j-1;
                 break;
            }
        }
        return startingStep;
    }

}