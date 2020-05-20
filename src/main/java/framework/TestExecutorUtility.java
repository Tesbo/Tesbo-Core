package framework;

import CustomStep.ExternalCode;
import CustomStep.Step;
import Execution.SetCommandLineArgument;
import Execution.TestExecutionBuilder;
import Selenium.Commands;
import io.github.bonigarcia.wdm.WebDriverManager;
import logger.TesboLogger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import reportAPI.ReportAPIConfig;
import reportAPI.Reporter;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

public class TestExecutorUtility {

    Commands cmd=new Commands();
    GetConfiguration config=new GetConfiguration();
    TesboLogger tesboLogger =new TesboLogger();
    private static final TestExecutor testExecutor=new TestExecutor();
    private static final Logger log = LogManager.getLogger(TestExecutorUtility.class);
    StepParser stepParser=new StepParser();
    ReportParser reportParser=new ReportParser();
    CommonMethods commonMethods=new CommonMethods();
    TestsFileParser testsFileParser=new TestsFileParser();
    ExternalCode externalCode=new ExternalCode();


    String firefoxText="firefox";
    String chromeText="chrome";
    String browserNameText="browserName";
    String whiteSpace="\\s{2,}";
    String stepsText="steps";
    String startTimeText="startTime";
    String stepIndexText="stepIndex";
    String statusText="status";
    String passedText="passed";
    String failedText="failed";
    String failedTextMsg="Failed";
    String printText="print";
    String pauseText="pause";
    String randomText="random";
    String stepText="Step: ";
    String verifyText="Verify: ";
    String codeText="Code: ";
    String exceptionAsString = null;


    public String getSeleniumAddress(){
        String seleniumAddress = null;
        boolean isGridFromCommandLineArgument= Boolean.parseBoolean(SetCommandLineArgument.IsGrid);
        if(config.getIsGrid() || isGridFromCommandLineArgument) {
            seleniumAddress = cmd.getSeleniumAddress();
        }
        return seleniumAddress;
    }

    public JSONObject browserInitialization(WebDriver driver,String browserName,String seleniumAddress){

        DesiredCapabilities capability = new DesiredCapabilities();
        JSONObject browserData=new JSONObject();
        JSONObject capabilities = null;

        if(config.getBinaryPath(browserName+"Path")!=null){
            String binaryPathLog="Binary path of "+browserName+": "+config.getBinaryPath(browserName+"Path");
            log.info(binaryPathLog);
            log.info("Initialize browser using binary path");
            testExecutor.initializeBrowserFromBinaryPath(browserName);
        }
        else {
            if (cmd.IsCapabilities(browserName) && seleniumAddress != null) {
                capabilities = cmd.getCapabilities(browserName);
                if (capabilities != null){
                    capability = cmd.setCapabilities(capabilities,capability);
                }
            }

            if (browserName.equalsIgnoreCase(firefoxText)) {
                capability.setCapability(browserNameText,firefoxText);
                WebDriverManager.firefoxdriver().setup();
                System.setProperty(FirefoxDriver.SystemProperty.DRIVER_USE_MARIONETTE, "true");
                System.setProperty(FirefoxDriver.SystemProperty.BROWSER_LOGFILE, "/dev/null");
                if (seleniumAddress == null) {
                    driver = new FirefoxDriver();
                }
            }
            if (browserName.equalsIgnoreCase(chromeText)) {
                capability.setCapability(browserNameText,chromeText);
                WebDriverManager.chromedriver().setup();
                if (seleniumAddress == null) {
                    driver = new ChromeDriver();
                }
            }
            if (browserName.equalsIgnoreCase("ie")) {
                capability.setCapability(browserNameText,"internetExplorer");
                WebDriverManager.iedriver().setup();
                if (seleniumAddress == null) {
                    driver = new InternetExplorerDriver();
                }

            }
        }
        browserData.put("capability",capability);
        browserData.put("driver",driver);

        return browserData;

    }

    public Map<String, WebDriver> setSessionList(WebDriver driver,Object session, Map<String, WebDriver> sessionList){
        if (session != null) {
            sessionList.put(session.toString(), driver);
        }
        return sessionList;
    }

    public JSONObject setRemoteBrowser(Object session,WebDriver driver,String seleniumAddress, DesiredCapabilities capability,Map<String, WebDriver> sessionList){
        JSONObject remoteBrowserDetails=new JSONObject();
        if (seleniumAddress != null) {
            driver = cmd.openRemoteBrowser(driver, capability, seleniumAddress);
            if (session != null){ sessionList.put(session.toString(), driver);}
            String seleniumAddressLog="Start test with selenium address: "+seleniumAddress;
            log.info(seleniumAddressLog);
        }
        remoteBrowserDetails.put("driver",driver);
        remoteBrowserDetails.put("sessionList",sessionList);
        return remoteBrowserDetails;
    }

    public void openBaseURL(WebDriver driver){
        try {
            if (!config.getBaseUrl().equals("") || (config.getBaseUrl()!=null)) {
                driver.get(config.getBaseUrl());
                String startBrowserUrlLog="Start browser with '"+config.getBaseUrl()+"' URL";
                log.info(startBrowserUrlLog);
            }
        } catch (Exception e) { log.error(""); }
    }

    /**
     * @param step
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     */
    public WebDriver initializeSessionRunTime(WebDriver driver, Object step, Map<String, WebDriver> sessionList, JSONArray listOfSession) {
        if (step.toString().replaceAll(whiteSpace, " ").trim().contains("[") && step.toString().replaceAll(whiteSpace, " ").trim().contains("]")) {
            String testStep = step.toString().replace("[", "").replace("]", "");
            for (Object session : listOfSession) {
                if (testStep.equalsIgnoreCase(session.toString())) {
                    boolean isInSessionList = false;
                    for (Map.Entry map : sessionList.entrySet()) {
                        if (map.getKey().toString().equalsIgnoreCase(testStep)) {
                            isInSessionList = true;
                            driver = (WebDriver) map.getValue();
                        }
                    }
                    if (!isInSessionList) {
                        driver = testExecutor.initializeBrowser(session);
                        try {
                            Thread.sleep(1000);
                        } catch (Exception e) {
                            StringWriter sw = new StringWriter();
                            e.printStackTrace(new PrintWriter(sw));
                            tesboLogger.testFailed(sw.toString());
                            log.error(sw.toString());
                        }
                    }
                }
            }
            tesboLogger.stepLog(step.toString());
            log.info(step);
        }
        return driver;

    }

    public JSONObject addPrintAnsRandomStepForReport(WebDriver driver, String step, JSONObject stepReportObject, JSONObject test){
        StringWriter sw = new StringWriter();
        if (!(step.contains("{") && step.contains("}") && step.contains(printText) && step.contains(randomText)))  {
            stepReportObject.put(stepsText, step.replace("@",""));
        }
        if (step.contains(printText))  {
            try {
                stepReportObject.put(stepsText,stepParser.printStep(driver,step,test));
            } catch (Exception e) {
                e.printStackTrace(new PrintWriter(sw));
                tesboLogger.testFailed(sw.toString());
                log.error(sw.toString());
            }
        }
        return stepReportObject;
    }

    public JSONObject addStepForReport(WebDriver driver, String step, JSONObject stepReportObject, JSONObject test){
        if (step.replaceAll(whiteSpace, " ").trim().startsWith(stepText)) {
            if (step.toLowerCase().contains(printText)) {
                try {
                    stepReportObject.put(stepsText, stepParser.printStep(driver, step, test));
                } catch (Exception e) {log.error("");}
            }
            if (step.contains("{") && step.contains("}")) {

                stepReportObject.put(stepsText, reportParser.dataSetStepReplaceValue(test, step));
            }

            String newStep= "";
            try {
                newStep = stepParser.parseStep(driver, test, step);
            } catch (IOException e) { log.error("");}

            if (step.toLowerCase().contains(randomText)) {
                stepReportObject.put(stepsText,newStep.replace("@",""));
            }
        }

        if (step.replaceAll(whiteSpace, " ").trim().startsWith(verifyText)) {
            sendVerifyStep(driver,step,test);

        }
        return stepReportObject;
    }

    /**
     * @param step
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     */
    public void sendVerifyStep(WebDriver driver,String step,JSONObject test) {
        VerifyParser verifyParser = new VerifyParser();
        int count = (int) step.chars().filter(ch -> ch == '@').count();
        String verifyStep = stepParser.removedVerificationTextFromSteps(step);
        if (count >= 2) {
            if (!(verifyStep.toLowerCase().contains(" and ") || verifyStep.toLowerCase().contains(" or "))) {
                stepParser.listOfSteps(step, count);
                for (String newStep : stepParser.listOfSteps(step, count)) {
                    verifyParser.parseVerify(driver, test, newStep);
                }
            }else if (verifyStep.toLowerCase().contains(" and "))
            {
                for(String newStep:stepParser.listOfStepWhoHasSameVerifier(step,"and")){
                    verifyParser.parseVerify(driver, test,newStep);
                }
            }
            else if (verifyStep.toLowerCase().contains(" or "))
            {
                int failCount = 0;
                for (String orConditionStep : stepParser.listOfStepWhoHasSameVerifier(step,"or")) {
                    try {
                        verifyParser.parseVerify(driver, test, orConditionStep);
                    } catch (Exception e) {
                        failCount++;
                        if (failCount == stepParser.listOfStepWhoHasSameVerifier(step,"or").size()) {
                            commonMethods.throwAssertException("'" + step + "' step is not verified",log);
                        }
                    }
                }
            }
        }
        else {
            verifyParser.parseVerify(driver, test, step);
        }
    }

    public JSONObject getSeverityAndPriorityIfExist(JSONObject test,JSONObject testReportObject){
        String msgLogText="Get severity and priority for test is: "+stepParser.isSeverityOrPriority(test);
        log.info(msgLogText);
        if(stepParser.isSeverityOrPriority(test)){
            JSONArray severityAndPrioritySteps=testsFileParser.getSeverityAndPriority(test);
            for (int i = 0; i < severityAndPrioritySteps.size(); i++) {
                Object step = severityAndPrioritySteps.get(i);
                if(step.toString().replaceAll(whiteSpace, " ").trim().contains("Priority: ")) {

                    tesboLogger.stepLog(step.toString());
                    log.info(step);
                    testReportObject.put("Priority", step.toString().replaceAll(whiteSpace, " ").trim().split(":")[1].trim());
                }
                if(step.toString().replaceAll(whiteSpace, " ").trim().contains("Severity: ")) {
                    tesboLogger.stepLog(step.toString());
                    log.info(step);
                    testReportObject.put("Severity", step.toString().replaceAll(whiteSpace, " ").trim().split(":")[1].trim());
                }
            }
        }
        return testReportObject;
    }

    public JSONObject executeBeforeTestStepIfExistInTestsFile(WebDriver driver,String testsFileName,int stepIndex,JSONArray testStepArray){
        String beforeTextMsg="Before test functionality is exist or not in tests file: "+testsFileParser.isBeforeTestInTestsFile(testsFileName);
        log.info(beforeTextMsg);
        if(testsFileParser.isBeforeTestInTestsFile(testsFileName)){
            JSONArray annotationSteps = testsFileParser.getBeforeAndAfterTestStepByTestsFile(testsFileName, "BeforeTest");
            for (int i = 0; i < annotationSteps.size(); i++) {

                JSONObject stepReportObject = new JSONObject();
                long startTimeStep = System.currentTimeMillis();
                Object step = annotationSteps.get(i);

                if(step.toString().toLowerCase().contains(pauseText) )
                {
                    if(config.getPauseStepDisplay()){ stepReportObject.put(stepIndexText, ++stepIndex); }
                }
                else{ stepReportObject.put(stepIndexText, ++stepIndex); }

                stepReportObject.put(startTimeText, startTimeStep);

                stepReportObject=testExecutor.addStepExecutionOfAnnotation(driver,stepReportObject,step.toString());

                if(stepReportObject.size()!=0) {
                    if(step.toString().toLowerCase().contains(pauseText) )
                    {
                        if(config.getPauseStepDisplay()){ testStepArray.add(stepReportObject); }
                    }
                    else { testStepArray.add(stepReportObject); }
                }
                if (stepReportObject.get(statusText).equals(failedText)) {
                    break;
                }

            }
        }
        JSONObject testStepDetails=new JSONObject();
        testStepDetails.put("testSteps",testStepArray);
        testStepDetails.put(stepIndexText,stepIndex);
        return testStepDetails;
    }

    public JSONArray executeAfterTestStepIfExistInTestsFile(WebDriver driver,String testsFileName,int stepIndex,JSONArray testStepArray){
        String beforeTextMsg="Before test functionality is exist or not in tests file: "+testsFileParser.isBeforeTestInTestsFile(testsFileName);
        log.info(beforeTextMsg);

        if (testsFileParser.isAfterTestInTestsFile(testsFileName)) {
            JSONArray annotationSteps = testsFileParser.getBeforeAndAfterTestStepByTestsFile(testsFileName, "AfterTest");
            for (int i = 0; i < annotationSteps.size(); i++) {

                JSONObject stepReportObject = new JSONObject();
                long startTimeStep = System.currentTimeMillis();
                Object step = annotationSteps.get(i);

                if(step.toString().toLowerCase().contains(pauseText) )
                {
                    if(config.getPauseStepDisplay()){ stepReportObject.put(stepIndexText, ++stepIndex); }
                }
                else{ stepReportObject.put(stepIndexText, ++stepIndex); }
                stepReportObject.put(startTimeText, startTimeStep);

                stepReportObject = testExecutor.addStepExecutionOfAnnotation(driver, stepReportObject, step.toString());

                if (stepReportObject.size() != 0) {
                    if(step.toString().toLowerCase().contains(pauseText) )
                    {
                        if(config.getPauseStepDisplay()){ testStepArray.add(stepReportObject); }
                    }
                    else { testStepArray.add(stepReportObject); }
                }
                if (stepReportObject.get(statusText).equals(failedText)) {
                    break;
                }

            }
        }
        return testStepArray;
    }

    public JSONObject executeTestSteps(WebDriver driver,String testsFileName,int stepIndex,JSONArray testStepArray,String testName, JSONObject test,Map<String, WebDriver> sessionList,JSONArray listOfSession,boolean isSession){
        StringWriter sw = new StringWriter();

        /*Getting step using testsFileName and Testcase Name*/
        String stepLogInfo="Get steps for "+testName+" test from "+testsFileName+" tests file";
        log.info(stepLogInfo);
        JSONArray steps = testsFileParser.getTestStepByTestsFileandTestCaseName(testsFileName, testName);

        int j = 0;
        String testInfoMsg=testName+" test has "+steps.size()+" steps";
        log.info(testInfoMsg);
        IfStepParser ifStepParser=new IfStepParser();

        for(int i = 0; i < steps.size(); i++) {
            boolean stepPassed = true;
            JSONObject stepReportObject = new JSONObject();
            long startTimeStep = System.currentTimeMillis();
            Object step = steps.get(i);

            if((step.toString().toLowerCase().startsWith("else::") || step.toString().toLowerCase().startsWith("else if:: ") || step.toString().toLowerCase().startsWith("end::")))
            {
                try {
                    commonMethods.throwTesboException("If condition is not found for '" + step.toString() + "' step.",log);
                }catch (Exception e){
                    e.printStackTrace(new PrintWriter(sw));
                    exceptionAsString = sw.toString();
                    tesboLogger.testFailed(failedTextMsg);
                    tesboLogger.testFailed(exceptionAsString);
                    stepPassed = false;
                    log.error(failedTextMsg);
                    log.error(exceptionAsString);
                }
            }

            if(step.toString().startsWith("If:: ") && !(step.toString().toLowerCase().startsWith("else if:: "))){
                try{
                    steps= ifStepParser.getStepsOfTestWhoHasIfCondition(driver,test,steps);
                    try {
                        step = steps.get(i);
                        if (step.toString().startsWith("If:: ")) {
                            i--;
                            continue;
                        }
                    }catch (Exception e1){ continue; }
                } catch (Exception e) {
                    e.printStackTrace(new PrintWriter(sw));
                    exceptionAsString = sw.toString();
                    tesboLogger.testFailed(failedTextMsg);
                    tesboLogger.testFailed(exceptionAsString);
                    stepPassed = false;
                    log.error(failedTextMsg);
                    log.error(exceptionAsString);
                }

            }

            if (!step.toString().replaceAll(whiteSpace, " ").trim().startsWith("Collection: ")) {
                if(step.toString().toLowerCase().contains(pauseText) )
                {
                    if(config.getPauseStepDisplay()){ stepReportObject.put(stepIndexText, ++stepIndex); }
                }
                else{
                    stepReportObject.put(stepIndexText, ++stepIndex);
                }

                stepReportObject.put(startTimeText, startTimeStep);

                if ( !(step.toString().contains("{") && step.toString().contains("}") && step.toString().contains(printText) && step.toString().contains(randomText)))
                {
                    if(step.toString().toLowerCase().contains(pauseText) )
                    {
                        if(config.getPauseStepDisplay()){stepReportObject.put(stepsText, step.toString().replace("@", "")); }
                    }
                    else if(step.toString().contains("{") && step.toString().contains("}") && step.toString().contains("Code")){
                        stepReportObject.put(stepsText, stepParser.replaceArgsOfCodeStep(test,step.toString()));
                    }
                    else {
                        if(step.toString().contains("@")){
                            String removeContent=null;
                            String[] stepsWord=step.toString().split(" ");
                            boolean flag=false;
                            for(String word:stepsWord){
                                if(word.contains("@") && !(word.contains("'"))){
                                    removeContent= word.trim().replace("@","");
                                }
                                if(word.contains("@") && !(word.contains("'"))){flag=true;}
                            }
                            if(removeContent!=null && !flag) {
                                if (removeContent.contains(".")) {
                                    stepReportObject.put(stepsText, step.toString().replace("@" + removeContent, removeContent.split("\\.")[1]));
                                } else {
                                    stepReportObject.put(stepsText, step.toString().replace("@" + removeContent, removeContent));
                                }
                            }
                            else{
                                if(flag){
                                    stepReportObject.put(stepsText, step.toString().replace("@" + removeContent, removeContent));
                                }
                            }

                        }
                        else {
                            stepReportObject.put(stepsText, step.toString().replace("@", ""));
                        }
                    }
                }
                if (step.toString().toLowerCase().contains(printText)) {
                    try {
                        stepReportObject.put(stepsText, stepParser.printStep(driver, step.toString(), test));
                    } catch (Exception e) {log.error("");}
                }
            }

            if (isSession) {
                String startSessionLog="Start session for "+step;
                log.info(startSessionLog);
                driver =initializeSessionRunTime(driver, step,sessionList,listOfSession);
            }
            try {

                if (step.toString().replaceAll(whiteSpace, " ").trim().startsWith(stepText)) {
                    if (step.toString().contains("{") && step.toString().contains("}")) {
                        stepReportObject.put(stepsText, reportParser.dataSetStepReplaceValue(test, step.toString()));
                    }
                    String stepNew = stepParser.parseStep(driver, test, step.toString());

                    if (step.toString().toLowerCase().contains(randomText)) {
                        stepReportObject.put(stepsText, stepNew.replace("@", ""));
                    }
                }

                if (step.toString().replaceAll(whiteSpace, " ").trim().startsWith(verifyText)) {
                    if (step.toString().contains("{") && step.toString().contains("}")) {
                        stepReportObject.put(stepsText, reportParser.dataSetStepReplaceValue(test, step.toString()));
                    }
                    sendVerifyStep(driver,step.toString(),test);

                }
            } catch (Exception ae)
            {
                if (step.toString().contains("{") && step.toString().contains("}")) {
                    stepReportObject.put(stepsText, step.toString().replaceAll("[{,}]", "'").replace("@", ""));
                }
                ae.printStackTrace(new PrintWriter(sw));
                exceptionAsString = sw.toString();
                tesboLogger.testFailed(failedTextMsg);
                tesboLogger.testFailed(exceptionAsString);
                stepPassed = false;
                log.error(failedTextMsg);
                log.error(exceptionAsString);
            }

            if (step.toString().replaceAll(whiteSpace, " ").trim().contains("Close:"))
            {
                String sessionName = step.toString().split(":")[1].trim().replace("]", "");
                boolean isSessions = false;
                for (Map.Entry session : sessionList.entrySet()) {
                    if (session.getKey().toString().equals(sessionName)) {
                        isSessions = true;
                        break;
                    }
                }
                if (isSessions) {
                    testExecutor.afterTest(sessionName);
                    String sessionClosedLog=sessionName+" session is closed";
                    log.info(sessionClosedLog);
                }

            } else if (step.toString().replaceAll(whiteSpace, " ").trim().startsWith(codeText))
            {
                try {
                    if (step.toString().contains("{") && step.toString().contains("}")) {
                        String replaceStepArgsLog=stepParser.replaceArgsOfCodeStep(test,step.toString());
                        tesboLogger.stepLog(replaceStepArgsLog);
                        log.info(replaceStepArgsLog);
                    }else {
                        tesboLogger.stepLog(step.toString());
                        log.info(step);
                    }
                    externalCode.runAllAnnotatedWith(Step.class, step.toString(),test, driver);
                } catch (Exception e) {
                    e.printStackTrace(new PrintWriter(sw));
                    exceptionAsString = sw.toString();
                    tesboLogger.testFailed(failedTextMsg);
                    tesboLogger.testFailed(sw.toString());
                    log.error(failedTextMsg);
                    log.error(sw.toString());
                    stepPassed = false;

                }

            } else if (step.toString().replaceAll(whiteSpace, " ").trim().startsWith("Collection: ")) {
                String startStepsLog="Start "+step.toString();
                log.info(startStepsLog);
                JSONArray groupSteps = new JSONArray();
                try {
                    String stepInfoLog="Get steps for "+step;
                    log.info(stepInfoLog);
                    groupSteps = testsFileParser.getGroupTestStepByTestFileandTestCaseName(stepParser.getCollectionName(step.toString()));
                } catch (Exception e)
                {
                    if (groupSteps.isEmpty())
                        throw e;
                    j++;
                    e.printStackTrace(new PrintWriter(sw));
                    exceptionAsString = sw.toString();
                    tesboLogger.testFailed(failedTextMsg);
                    tesboLogger.testFailed(sw.toString());
                    log.error(failedTextMsg);
                    log.error(sw.toString());
                    stepPassed = false;
                }
                for (int s = 0; s <= groupSteps.size() - 1; s++) {
                    Object groupStep = groupSteps.get(s);

                    startTimeStep = System.currentTimeMillis();
                    step = steps.get(i);
                    stepReportObject.put(stepIndexText, ++stepIndex);
                    stepReportObject.put(startTimeText, startTimeStep);
                    stepReportObject.put(stepsText, groupStep.toString().replace("@", ""));

                    if (groupStep.toString().startsWith(stepText)) {
                        try {
                            stepParser.parseStep(driver, test, groupStep.toString());
                        } catch (Exception ae) {
                            j++;
                            ae.printStackTrace(new PrintWriter(sw));
                            exceptionAsString = sw.toString();
                            tesboLogger.testFailed(failedTextMsg);
                            tesboLogger.testFailed(sw.toString());
                            log.error(failedTextMsg);
                            log.error(sw.toString());
                            stepPassed = false;
                        }
                    } else if (groupStep.toString().startsWith(verifyText)) {
                        try {
                            sendVerifyStep(driver,groupStep.toString(),test);
                        } catch (Exception ne) {
                            j++;
                            ne.printStackTrace(new PrintWriter(sw));
                            exceptionAsString = sw.toString();
                            tesboLogger.testFailed(failedTextMsg);
                            tesboLogger.testFailed(sw.toString());
                            log.error(failedTextMsg);
                            log.error(sw.toString());
                            stepPassed = false;
                        }
                    }
                    else if (groupStep.toString().replaceAll(whiteSpace, " ").trim().startsWith(codeText)) {

                        try {
                            if (step.toString().contains("{") && step.toString().contains("}")) {
                                String stepInfoLog=stepParser.replaceArgsOfCodeStep(test,groupStep.toString());
                                tesboLogger.stepLog(stepInfoLog);
                                log.info(stepInfoLog);
                            }else {
                                tesboLogger.stepLog(groupStep.toString());
                                log.info(groupStep);
                            }
                            externalCode.runAllAnnotatedWith(Step.class, groupStep.toString(),test, driver);
                        } catch (Exception e) {
                            e.printStackTrace(new PrintWriter(sw));
                            exceptionAsString = sw.toString();
                            tesboLogger.testFailed(failedTextMsg);
                            tesboLogger.testFailed(sw.toString());
                            log.error(failedTextMsg);
                            log.error(sw.toString());
                            stepPassed = false;
                        }
                    }
                    reportParser.addScreenshotUrlInReport(stepReportObject, step.toString());
                    stepReportObject = testExecutor.addStepResultInReport(driver, stepReportObject, stepPassed);
                    testStepArray.add(stepReportObject);
                    stepReportObject = new JSONObject();
                }
            }

            reportParser.addScreenshotUrlInReport(stepReportObject, step.toString());

            if (stepReportObject.size() != 0) {
                stepReportObject = testExecutor.addStepResultInReport(driver, stepReportObject, stepPassed);

                if(step.toString().toLowerCase().contains(pauseText) )
                {
                    if(config.getPauseStepDisplay()){ testStepArray.add(stepReportObject); }
                }
                else { testStepArray.add(stepReportObject); }
            }
            if (!stepPassed) {
                break;
            }

            if (step.toString().replaceAll(whiteSpace, " ").trim().startsWith(codeText) && (!Reporter.printStepReportObject.isEmpty())) {
                for (int k = 0; k < Reporter.printStepReportObject.size(); k++){
                    JSONObject extStep;
                    JSONObject printExtStep = new JSONObject();
                    extStep= (JSONObject) Reporter.printStepReportObject.get(j);
                    printExtStep.put(stepIndexText, ++stepIndex);
                    printExtStep.put(stepsText,extStep.get(stepsText));
                    printExtStep.put(statusText,passedText);
                    tesboLogger.stepLog(extStep.get(stepsText).toString());
                    log.info(extStep.get(stepsText));
                    testStepArray.add(printExtStep);
                }

                Reporter.printStepReportObject = new JSONArray();
            }

        }

        JSONObject testStepDetails=new JSONObject();
        testStepDetails.put("testSteps",testStepArray);
        testStepDetails.put(stepIndexText,stepIndex);
        testStepDetails.put("exceptionAsString",exceptionAsString);

        return testStepDetails;
    }

    public void addReportOnCloud(String testName,String testsFileName,JSONObject testReportObject,String testResult){
        ReportAPIConfig reportAPIConfig = new ReportAPIConfig();
        if(config.getIsCloudIntegration()) {
            boolean isAddOnCloud=false;
            if(testsFileParser.isRetry(testsFileName, testName).equalsIgnoreCase("null") || testsFileParser.isRetry(testsFileName, testName).equalsIgnoreCase("false")){
                if(!(Integer.parseInt(config.getRetryAnalyser())>0) || testsFileParser.isRetry(testsFileName, testName).equalsIgnoreCase("false")){
                    isAddOnCloud=true;
                }
                else {
                    if(TestExecutionBuilder.failTest == Integer.parseInt(config.getRetryAnalyser()) || (testResult.equalsIgnoreCase(passedText))){
                        isAddOnCloud=true;
                    }
                }
            }
            else {
                if(TestExecutionBuilder.failTest == Integer.parseInt(config.getRetryAnalyser()) || testResult.equalsIgnoreCase(passedText)){
                    isAddOnCloud=true;
                }
            }
            if (isAddOnCloud) {reportAPIConfig.organiazeDataForCloudReport(testReportObject);}
        }
    }

    public void manageFailTestExecutionQueue(String testResult,String testName,String testsFileName,JSONObject test,JSONObject classTest){
        TestExecutionBuilder testExecutionBuilder=new TestExecutionBuilder();
        if(testResult.equalsIgnoreCase(failedText)){
            if(testsFileParser.isRetry(testsFileName, testName).equalsIgnoreCase("null")){
                testExecutionBuilder.failTestExecutionQueue(test);
            }
        }
        else {
            Object removeTest=null;
            if(!TestExecutionBuilder.failTestQueue.isEmpty()){
                for(Object failTest:TestExecutionBuilder.failTestQueue){
                    if(failTest.equals(classTest)){
                        removeTest=failTest;
                    }
                }
                if(removeTest!=null){
                    TestExecutionBuilder.failTestQueue.remove(test);
                }
            }
        }
    }

    public JSONArray addTestFirstStepOfOpenBaseUrlToReportObject(JSONArray testStepArray,int stepIndex){
        if(stepIndex==0){
            JSONObject stepReportObject = new JSONObject();
            String logStepInfoMsg="Step: Open "+config.getBaseUrl();
            tesboLogger.testLog(logStepInfoMsg);
            log.info(logStepInfoMsg);
            stepReportObject.put(stepIndexText, ++stepIndex);
            stepReportObject.put(stepsText, logStepInfoMsg);
            stepReportObject.put(statusText, passedText);
            testStepArray.add(stepReportObject);

        }
        return testStepArray;
    }

}
