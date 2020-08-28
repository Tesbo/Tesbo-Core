package framework;

import selenium.Commands;
import logger.TesboLogger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.openqa.selenium.WebDriver;
import exception.*;

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
    Commands cmd = new Commands();
    GetLocator locator = new GetLocator();
    StepParser stepParser = new StepParser();
    String isContinueText="isContinue";
    String newStepText="newStep";
    String nestedIfText="nestedIf";

    /**
     *
     * @param driver
     * @param test
     * @param step
     * @return
     */
    public boolean parseIfStep(WebDriver driver, JSONObject test, String step) {

        String testsFileName=test.get("testsFileName").toString();
        boolean isIfCondition = false;
        String greaterThan="greater than";
        String lessThan="less than";

        String conditionType = checkStepHasAndOrCondition(step);
        if (conditionType.equals("and") || conditionType.equals("or")) {
            isIfCondition=executeStepWhenIfConditionHasAndOrCondition(driver,test,step,isIfCondition,conditionType);
        } else {
            if (step.toLowerCase().contains("displayed") || step.toLowerCase().contains("present")) {
                /*
                 * If:: @element is displayed
                 * If:: @element is present
                 * */
                isIfCondition=verifyWhenIfConditionHasDisplayedOrPresent(isIfCondition,driver,testsFileName,step);
            }
            else if (step.toLowerCase().contains(greaterThan) || step.toLowerCase().contains(lessThan)) {
                isIfCondition=verifyNumericValueOnIfCondition(driver,test,step,testsFileName,isIfCondition,greaterThan,lessThan);
            }
            else if (step.toLowerCase().contains("text")) {
                isIfCondition=verifyElementTextOnIfCondition(driver,test,step,testsFileName,isIfCondition);
            } else if (step.toLowerCase().contains("is") && (step.toLowerCase().contains("checked") || step.toLowerCase().contains("selected"))) {
                /*
                 * If:: @element is checked
                 * If:: @element is selected
                 * */
                isIfCondition=verifyElementISSelectedOrCheckedOnIfCondition(driver,step,testsFileName,isIfCondition);
            } else if (step.toLowerCase().contains("is") && step.toLowerCase().contains("enabled") && cmd.findElement(driver, locator.getLocatorValue(testsFileName, parseElementName(step, 1)) + "_IF").isEnabled()) {
                /*
                 * If:: @element is enabled
                 * */
                isIfCondition = true;
            }

        }
        return isIfCondition;
    }

    /**
     *
     * @param driver
     * @param step
     * @param testsFileName
     * @param isIfCondition
     * @return
     */
    public boolean verifyElementISSelectedOrCheckedOnIfCondition(WebDriver driver,String step,String testsFileName,boolean isIfCondition){
        if (cmd.findElement(driver, locator.getLocatorValue(testsFileName, parseElementName(step, 1)) + "_IF").isSelected()) {
            isIfCondition = true;
        }
        return isIfCondition;
    }

    /**
     *
     * @param driver
     * @param test
     * @param step
     * @param testsFileName
     * @param isIfCondition
     * @return
     */
    public boolean verifyElementTextOnIfCondition(WebDriver driver,JSONObject test,String step,String testsFileName,boolean isIfCondition){
        if (step.toLowerCase().contains("equal")) {
            if (elementCountInStep(step) == 2) {
                isIfCondition=verifyTwoElementTextInIfCondition(driver,step,testsFileName,isIfCondition);
            } else {
                isIfCondition=verifySingleElementTextInIfCondition(driver,test,step,testsFileName,isIfCondition);
            }
        }
        return isIfCondition;

    }

    /**
     *
     * @param driver
     * @param step
     * @param testsFileName
     * @param isIfCondition
     * @return
     */
    public boolean verifyTwoElementTextInIfCondition(WebDriver driver,String step,String testsFileName,boolean isIfCondition){
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
        return isIfCondition;
    }

    /**
     *
     * @param driver
     * @param test
     * @param step
     * @param testsFileName
     * @param isIfCondition
     * @return
     */
    public boolean verifySingleElementTextInIfCondition(WebDriver driver,JSONObject test,String step,String testsFileName,boolean isIfCondition){
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
        return isIfCondition;
    }

    /**
     *
     * @param driver
     * @param test
     * @param step
     * @param testsFileName
     * @param isIfCondition
     * @param greaterThan
     * @param lessThan
     * @return
     */
    public boolean verifyNumericValueOnIfCondition(WebDriver driver,JSONObject test,String step,String testsFileName,boolean isIfCondition,String greaterThan,String lessThan){
        int number;
        int elementNumber;
        String textOfStep = stepParser.parseTextToEnter(test, step);
        try {
            number = Integer.parseInt(textOfStep);
        } catch (Exception e) {


            synchronized (this){
                isIfError = true;
            }
            throw new TesboException("Please enter numeric value for verification in if condition");
        }

        try {
            elementNumber = Integer.parseInt(cmd.findElement(driver, locator.getLocatorValue(testsFileName, parseElementName(step, 1)) + "_IF").getText());
        } catch (Exception e) {
            synchronized (this){
                isIfError = true;
            }
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
        return isIfCondition;
    }

    /**
     *
     * @param isIfCondition
     * @param driver
     * @param testsFileName
     * @param step
     * @return
     */
    public boolean verifyWhenIfConditionHasDisplayedOrPresent(boolean isIfCondition,WebDriver driver,String testsFileName,String step) {
        try {
            if (cmd.findElement(driver, locator.getLocatorValue(testsFileName, parseElementName(step, 1)) + "_IF").isDisplayed()) {
                isIfCondition = true;
            }
        }catch (Exception e){log.error(""); }
        return isIfCondition;

    }

    /**
     *
     * @param driver
     * @param test
     * @param step
     * @param isIfCondition
     * @param conditionType
     * @return
     */

    public boolean executeStepWhenIfConditionHasAndOrCondition(WebDriver driver,JSONObject test,String step,boolean isIfCondition,String conditionType){
        String[] steps = getListOfSteps(step);
        for (String newStep : steps) {
            boolean isIf = false;
            try {
                isIf = parseIfStep(driver, test, newStep);
            } catch (Exception e) {
                    e.printStackTrace(new PrintWriter(sw));
                    exceptionAsString = sw.toString();
                    throwErrorWhenIsIfErrorTrue(exceptionAsString,isIfError);
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
        return isIfCondition;
    }

    /**
     *
     * @param exceptionAsString
     * @param isIfError
     */
    public void throwErrorWhenIsIfErrorTrue(String exceptionAsString,boolean isIfError){
        if (isIfError) {
            throw new TesboException(exceptionAsString.split("\\n")[0].replaceAll(exception,""));
        }
    }

    /**
     *
     * @param step
     * @param elementNumber
     * @return
     */
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

    /**
     *
     * @param step
     * @return
     */
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

    /**
     *
     * @param step
     * @return
     */
    public String checkStepHasAndOrCondition(String step) {
        String conditionType = "";
        boolean flag = false;
        if (step.contains("'")) {
            step = stepParser.removedVerificationTextFromSteps(step);
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

    /**
     *
     * @param step
     * @return
     */
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

    /**
     *
     * @param steps
     * @return
     */
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


    /**
     *
     * @param steps
     * @param test
     */
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


    /**
     *
     * @param driver
     * @param test
     * @param steps
     * @return
     */
    public JSONArray getStepsOfTestWhoHasIfCondition(WebDriver driver, JSONObject test, JSONArray steps) {
        JSONArray newStep = new JSONArray();
        ifCondition = "";
        elseCondition="";
        elseIFCondition="";
        boolean nestedIf=false;
        for (int i = 0; i < steps.size(); i++) {
            if (steps.get(i).toString().startsWith(ifCon) && !(steps.get(i).toString().startsWith(elseIfCon))) {
                JSONObject ifStepsDetails=addStepForIfCondition(driver,test,newStep,steps,i,nestedIf);
                newStep= (JSONArray) ifStepsDetails.get(newStepText);
                i= (int) ifStepsDetails.get("i");
                nestedIf= (boolean) ifStepsDetails.get(nestedIfText);
                if((boolean) ifStepsDetails.get(isContinueText)){
                    continue;
                }
            }
            if (steps.get(i).toString().startsWith(elseIfCon) && ifCondition.equals("fail") && !(elseIFCondition.equals("pass"))) {
                JSONObject ifElseStepsDetails=addStepForIfElseCondition(driver,test,newStep,steps,i,nestedIf);
                newStep= (JSONArray) ifElseStepsDetails.get(newStepText);
                i= (int) ifElseStepsDetails.get("i");
                nestedIf= (boolean) ifElseStepsDetails.get(nestedIfText);
                if((boolean) ifElseStepsDetails.get(isContinueText)){
                    continue;
                }
            }

            JSONObject elseStepDetails= executeWhenElseConditionExistInIfCondition(steps,i,nestedIf,newStep);
            newStep= (JSONArray) elseStepDetails.get(newStepText);
            i= (int) elseStepDetails.get("i");
            nestedIf= (boolean) elseStepDetails.get(nestedIfText);

            JSONObject endStepsDetails=addStepWhenIfConditionHasEndStep(newStep,steps,i);
            newStep= (JSONArray) endStepsDetails.get(newStepText);
            if((boolean)endStepsDetails.get("isBreak")){
                break;
            }


            newStep= addStepWhenAllConditionFail(newStep,steps,i);
        }
        return newStep;
    }

    /**
     *
     * @param steps
     * @param i
     * @param nestedIf
     * @param newStep
     * @return
     */
    public JSONObject executeWhenElseConditionExistInIfCondition(JSONArray steps,int i,boolean nestedIf,JSONArray newStep){
        if (steps.get(i).toString().startsWith(elseCon) && !(steps.get(i).toString().startsWith(elseIfCon)) && !(elseCondition.equals("fail"))) {
            elseCondition = "pass";
            JSONObject ifStepsDetails=addStepToStepArray(steps,i,nestedIf,newStep);
            newStep= (JSONArray) ifStepsDetails.get(newStepText);
            i= (int) ifStepsDetails.get("i");
            nestedIf= (boolean) ifStepsDetails.get(nestedIfText);

        }
        JSONObject ifElseStepsDetails=new JSONObject();
        ifElseStepsDetails.put(newStepText,newStep);
        ifElseStepsDetails.put("i",i);
        ifElseStepsDetails.put(nestedIfText,nestedIf);

        return ifElseStepsDetails;
    }


    /**
     *
     * @param newStep
     * @param steps
     * @param i
     * @return
     */
    public JSONArray addStepWhenAllConditionFail(JSONArray newStep,JSONArray steps,int i){
        if (!(ifCondition.equals("fail") || elseCondition.equals("fail") || elseIFCondition.equals("fail"))) {
            newStep.add(steps.get(i));
        }
        return newStep;
    }

    /**
     *
     * @param newStep
     * @param steps
     * @param i
     * @return
     */
    public JSONObject addStepWhenIfConditionHasEndStep(JSONArray newStep,JSONArray steps,int i){
        JSONObject endStepsDetails=new JSONObject();
        boolean isBreak=false;
        if (steps.get(i).toString().contains(end)) {
            ifCondition ="";
            elseCondition="";
            elseIFCondition="";
            for (int j = i + 1; j < steps.size(); j++) {
                newStep.add(steps.get(j));
            }
            isBreak=true;
        }
        endStepsDetails.put("isBreak",isBreak);
        endStepsDetails.put(newStepText,newStep);
        return endStepsDetails;
    }


    /**
     *
     * @param driver
     * @param test
     * @param newStep
     * @param steps
     * @param i
     * @param nestedIf
     * @return
     */
    public JSONObject addStepForIfElseCondition(WebDriver driver,JSONObject test,JSONArray newStep,JSONArray steps,int i,boolean nestedIf){
        boolean isContinue=false;

        if(steps.get(i).toString().trim().split("::").length != 2){
            log.info("Condition is not found for Else If:: step OR something wrong in Else If condition");
            throw new TesboException("Condition is not found for Else If:: step OR something wrong in Else If condition");
        }
        try {
            if (parseIfStep(driver, test, steps.get(i).toString())) {
                elseIFCondition = "pass";
                elseCondition="fail";
                JSONObject ifStepsDetails=addStepToStepArray(steps,i,nestedIf,newStep);
                newStep= (JSONArray) ifStepsDetails.get(newStepText);
                i= (int) ifStepsDetails.get("i");
                nestedIf= (boolean) ifStepsDetails.get(nestedIfText);
            }
            else{
                elseIFCondition = "fail";
                i=skipIfConditionStep(steps,i+1);
                isContinue=true;
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
            isContinue=true;
        }

        JSONObject ifElseStepsDetails=new JSONObject();
        ifElseStepsDetails.put(newStepText,newStep);
        ifElseStepsDetails.put("i",i);
        ifElseStepsDetails.put(nestedIfText,nestedIf);
        ifElseStepsDetails.put(isContinueText,isContinue);

        return ifElseStepsDetails;
    }

    /**
     *
     * @param driver
     * @param test
     * @param newStep
     * @param steps
     * @param i
     * @param nestedIf
     * @return
     */
    public JSONObject addStepForIfCondition(WebDriver driver,JSONObject test,JSONArray newStep,JSONArray steps,int i,boolean nestedIf){
        boolean isContinue=false;
        if(steps.get(i).toString().trim().split("::").length != 2){
            log.info("Condition is not found for If:: step OR something wrong in If condition");
            throw new TesboException("Condition is not found for If:: step OR something wrong in If condition");
        }
        try {
            if (parseIfStep(driver, test, steps.get(i).toString())) {
                ifCondition = "pass";
                elseCondition="fail";
                elseIFCondition="fail";
                JSONObject ifStepsDetails=addStepToStepArray(steps,i,nestedIf,newStep);
                newStep= (JSONArray) ifStepsDetails.get(newStepText);
                i= (int) ifStepsDetails.get("i");
                nestedIf= (boolean) ifStepsDetails.get(nestedIfText);
            }
            else{
                ifCondition = "fail";
                i=skipIfConditionStep(steps,i+1);
                nestedIf=false;
                isContinue=true;
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
            isContinue=true;
        }
        JSONObject ifStepsDetails=new JSONObject();
        ifStepsDetails.put(newStepText,newStep);
        ifStepsDetails.put("i",i);
        ifStepsDetails.put(nestedIfText,nestedIf);
        ifStepsDetails.put(isContinueText,isContinue);

        return ifStepsDetails;
    }

    /**
     *
     * @param steps
     * @param i
     * @param nestedIf
     * @param newStep
     * @return
     */

    public JSONObject addStepToStepArray(JSONArray steps,int i,boolean nestedIf,JSONArray newStep){
        boolean isContinue=false;
        for (int j = i + 1; j < steps.size(); j++) {
            if(j==steps.size()-1){ i=j; }
            if(nestedIf){
                nestedIf=isNestedIF(steps.get(j).toString(),nestedIf);
                newStep.add(steps.get(j));
                isContinue=true;
            }
            else{
                nestedIf=isNestedIF(steps.get(j).toString(),nestedIf);
                if(nestedIf) {
                    newStep.add(steps.get(j));
                    isContinue=true;
                }
            }
            if(isContinue){continue;}

            if (!(steps.get(j).toString().startsWith(end) || steps.get(j).toString().startsWith(elseCon) || steps.get(j).toString().startsWith(elseIfCon))) {
                newStep.add(steps.get(j));
            } else {
                i = j;
                break;
            }
        }
        JSONObject stepsDetails=new JSONObject();
        stepsDetails.put(newStepText,newStep);
        stepsDetails.put("i",i);
        stepsDetails.put(nestedIfText,nestedIf);

        return stepsDetails;
    }

    int countif=0;

    /**
     *
     * @param step
     * @param nestedIf
     * @return
     */
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

    /**
     *
     * @param steps
     * @param startingStep
     * @return
     */
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
            if((steps.get(j).toString().startsWith("Else::") || steps.get(j).toString().startsWith(elseIfCon)) && countIf==0){
                startingStep = j-1;
                 break;
            }
        }
        return startingStep;
    }

}