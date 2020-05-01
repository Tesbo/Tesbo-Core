package framework;

import DataCollector.BuildReportDataObject;
import Execution.SetCommandLineArgument;
import Execution.TestExecutionBuilder;
import CustomStep.*;
import Exception.*;
import Selenium.Commands;


import logger.TesboLogger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import io.github.bonigarcia.wdm.WebDriverManager;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.opera.OperaDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import reportAPI.ReportAPIConfig;
import reportAPI.Reporter;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

public class TestExecutor implements Runnable {

    static TesboLogger tesboLogger = new TesboLogger();
    ReportParser reportParser=new ReportParser();
    public WebDriver driver;
    public Map<String, WebDriver> sessionList = new HashMap<>();
    JSONObject test;
    JSONArray listOfSession;
    boolean isSession = false;
    TestExecutionBuilder testExecutionBuilder=new TestExecutionBuilder();
    String screenShotPath = null;
    String testResult = "";
    Commands cmd=new Commands();
    String exceptionAsString = null;
    GetConfiguration config=new GetConfiguration();
    static JSONObject localVariable=new JSONObject();
    private static final Logger log = LogManager.getLogger(TestExecutor.class);

    String testsFileName;
    String testName;
    String browser;
    String stepsText="steps";
    String browserNameText="browserName";
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
    String firefoxText="firefox";
    String chromeText="chrome";

    String whiteSpace="\\s{2,}";


    public TestExecutor() { }
    public TestExecutor(JSONObject test) {
        this.test = test;
        testsFileName=test.get("testsFileName").toString();
        testName=test.get("testName").toString();
        browser=test.get("browser").toString();
    }

    public static void main(String[] args) throws Exception {
        TestExecutionBuilder builder = new TestExecutionBuilder();
        ReportParser report = new ReportParser();

        builder.buildExecution();

        report.generateReportDir();
    }


    /**
     * @auther :
     * @lastModifiedBy: Ankit Mistry
     */
    public void beforeTest() {
        TestsFileParser testsFileParser = new TestsFileParser();
        listOfSession = testsFileParser.getSessionListFromTest(testsFileName, testName);
        if (!listOfSession.isEmpty()) {
            isSession = true;
            log.info("Test is run with multiple session");
        } else {
            log.info("Test is run with single session");
            initializeBrowser(null);
        }
    }


    public void afterTest(String sessionName) {
        if (sessionName != null) {
            for (Map.Entry session : sessionList.entrySet()) {
                if (sessionName.equals(session.getKey().toString())) {
                    String logInfoText="Close browser for "+sessionName+" session";
                    log.info(logInfoText);
                    driver = (WebDriver) session.getValue();
                    driver.quit();
                    sessionList.remove(session.getKey());
                    log.info("Remove session from list");
                    break;
                }
            }
        } else {
            if (isSession) {
                for (Map.Entry session : sessionList.entrySet()) {
                    driver = (WebDriver) session.getValue();
                    driver.quit();
                }
                log.info("Close all session browser");
            } else {
                driver.quit();
                log.info("Close browser");
            }
        }
    }


    public JSONObject runTest() {
        StepParser stepParser = new StepParser();
        TestsFileParser testsFileParser = new TestsFileParser();
        ExternalCode externalCode=new ExternalCode();
        BuildReportDataObject buildReport = new BuildReportDataObject();
        TesboLogger tesboLogger =new TesboLogger();
        StringWriter sw = new StringWriter();
        localVariable=new JSONObject();
        testResult = "";

        JSONObject testReportObject = new JSONObject();
        long startTime = System.currentTimeMillis();

        /*Adding data into the report*/
        testReportObject.put(startTimeText, startTime);
        testReportObject.put(browserNameText, browser);
        testReportObject.put("testName", testName);
        testReportObject.put("testsFileName", testsFileName);
        testReportObject.put("suiteName", test.get("suiteName").toString());
        testReportObject.put("tagName", test.get("tagName").toString());
        String logInfoMsg="Test: "+testName;
        tesboLogger.testLog(logInfoMsg);
        log.info(logInfoMsg);


        int stepIndex=0;
        JSONArray testStepArray = new JSONArray();
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
        screenShotPath = null;

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

                stepReportObject=addStepExecutonOfannotation(driver,stepReportObject,step.toString());

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
                    String errorMsg="If condition is not found for '" + step.toString() + "' step.";
                    log.error(errorMsg);
                    throw new TesboException(errorMsg);
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
                    } catch (Exception e) {}
                }
            }

            if (isSession) {
                String startSessionLog="Start session for "+step;
                log.info(startSessionLog);
                initializeSessionRunTime(step);
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
                    sendVerifyStep(step.toString());

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
                    afterTest(sessionName);
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
                            sendVerifyStep(groupStep.toString());
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
                    stepReportObject = addStepResultInReport(driver, stepReportObject, stepPassed);
                    testStepArray.add(stepReportObject);
                    stepReportObject = new JSONObject();
                }
            }

            reportParser.addScreenshotUrlInReport(stepReportObject, step.toString());

            if (stepReportObject.size() != 0) {
                stepReportObject = addStepResultInReport(driver, stepReportObject, stepPassed);

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

                stepReportObject = addStepExecutonOfannotation(driver, stepReportObject, step.toString());

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

        Capabilities caps = ((RemoteWebDriver) driver).getCapabilities();

        testReportObject.put("browserVersion", caps.getVersion());
        String osName= caps.getPlatform().toString();
        if(osName.equalsIgnoreCase("xp")) { osName = "windows"; }
        testReportObject.put("osName", osName);

        long stopTimeTest = System.currentTimeMillis();
        testReportObject.put("testStep", testStepArray);

        if (testResult.equals(failedText)) {
            testReportObject.put("fullStackTrace", exceptionAsString);
            testReportObject.put("screenShot", screenShotPath);
        }
        testReportObject.put("totalTime", stopTimeTest - startTime);
        testReportObject.put(statusText, testResult);

        buildReport.addDataInMainObject(browser, testsFileName, testName, testReportObject);

        ReportAPIConfig reportAPIConfig = new ReportAPIConfig();
        if(config.getIsCloudIntegration()) {
            boolean isAddOnCloud=false;
            if(testsFileParser.isRetry(testsFileName, testName).equalsIgnoreCase("null") || testsFileParser.isRetry(testsFileName, testName).equalsIgnoreCase("false")){
                if(!(Integer.parseInt(config.getRetryAnalyser())>0) || testsFileParser.isRetry(testsFileName, testName).equalsIgnoreCase("false")){
                    isAddOnCloud=true;
                }
                else {
                    if(TestExecutionBuilder.failTest == Integer.parseInt(config.getRetryAnalyser())){
                        isAddOnCloud=true;
                    }
                    else if(testResult.equalsIgnoreCase(passedText)){
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
        if(testResult.equalsIgnoreCase(failedText)){
            if(testsFileParser.isRetry(testsFileName, testName).equalsIgnoreCase("null")){
                testExecutionBuilder.failTestExecutionQueue(test);
            }
        }
        else {
            Object removeTest=null;
            if(!TestExecutionBuilder.failTestQueue.isEmpty()){
                for(Object failTest:TestExecutionBuilder.failTestQueue){
                    if(failTest.equals(this.test)){
                        removeTest=failTest;
                    }
                }
                if(removeTest!=null){
                    TestExecutionBuilder.failTestQueue.remove(test);
                }
            }
        }
        return testReportObject;
    }

    @Override
    public void run() {
        try {
            if(TestExecutionBuilder.isSingleWindow) {
                TestExecutionBuilder.isSingleWindow=false;
                beforeTest();
                TestExecutionBuilder.driver=driver;
            }
            if(! TestExecutionBuilder.isSingleWindow && TestExecutionBuilder.singleWindowRun) {
                driver=TestExecutionBuilder.driver;
            }

            if(! TestExecutionBuilder.isSingleWindow && !TestExecutionBuilder.singleWindowRun) {
                beforeTest();
            }

            runTest();

            if(! TestExecutionBuilder.isSingleWindow && !TestExecutionBuilder.singleWindowRun) {
                if(!(!config.getBrowserClose() && testResult.equalsIgnoreCase(failedText))) {
                    afterTest(null);
                }
            }

        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            tesboLogger.testFailed(sw.toString());
            log.error(sw.toString());
        }

    }


    /**
     * @param session
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     */
    public WebDriver initializeBrowser(Object session) {
        String seleniumAddress = null;
        if(config.getIsGrid() || Boolean.valueOf(SetCommandLineArgument.IsGrid)) {
            seleniumAddress = cmd.getSeleniumAddress();
        }
        String browserName = browser;
        String startBrowserLog="Start Browser: "+browserName;
        log.info(startBrowserLog);
        DesiredCapabilities capability = new DesiredCapabilities();
        JSONObject capabilities = null;
        try {

            if(config.getBinaryPath(browserName+"Path")!=null){
                String binaryPathLog="Binary path of "+browserName+": "+config.getBinaryPath(browserName+"Path");
                log.info(binaryPathLog);
                log.info("Initialize browser using binary path");
                initializeBrowserFromBinaryPath(browserName);
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
            if (session != null) {
                sessionList.put(session.toString(), driver);
            }

            if (seleniumAddress != null) {
                driver = cmd.openRemoteBrowser(driver, capability, seleniumAddress);
                if (session != null){ sessionList.put(session.toString(), driver);}
                String seleniumAddressLog="Start test with selenium address: "+seleniumAddress;
                log.info(seleniumAddressLog);
            }

            driver.manage().window().maximize();

            try {
                if (!config.getBaseUrl().equals("") || !config.getBaseUrl().equals(null)) {
                    driver.get(config.getBaseUrl());
                    String startBrowserUrlLog="Start browser with '"+config.getBaseUrl()+"' URL";
                    log.info(startBrowserUrlLog);
                }
            } catch (org.openqa.selenium.WebDriverException e) {
            }
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            tesboLogger.testFailed(sw.toString());
            log.error(sw.toString());
        }
        return driver;
    }

    /**
     * @param step
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     */
    public void initializeSessionRunTime(Object step) {
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
                        driver = initializeBrowser(session);
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
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

    }

    /**
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     * @param browserName
     * @return
     */
    public WebDriver initializeBrowserFromBinaryPath(String browserName) {
        if(config.getBinaryPath(browserName+"Path")!=null){
            if (browserName.equalsIgnoreCase(firefoxText)) {
                System.setProperty("webdriver.gecko.driver", config.getBinaryPath(browserName+"Path"));
                driver = new FirefoxDriver();
            }
            if (browserName.equalsIgnoreCase(chromeText)) {
                System.setProperty("webdriver.chrome.driver", config.getBinaryPath(browserName+"Path"));
                driver = new ChromeDriver();
            }
            if (browserName.equalsIgnoreCase("ie")) {
                System.setProperty("webdriver.ie.driver", config.getBinaryPath(browserName+"Path"));
                driver = new InternetExplorerDriver();
            }
            if (browserName.equalsIgnoreCase("opera")) {
                System.setProperty("webdriver.opera.driver", config.getBinaryPath(browserName+"Path"));
                driver = new OperaDriver();
            }
        }


        return driver;
    }


    /**
     * @auther : Ankit Mistry
     * @param driver
     * @param stepReportObject
     * @param stepPassed
     * @return
     */
    public JSONObject addStepResultInReport(WebDriver driver, JSONObject stepReportObject, boolean stepPassed)  {
        if(stepReportObject.size()!=0) {
            if (!stepPassed) {
                stepReportObject.put(statusText, failedText);
                testResult = failedText;
                screenShotPath = cmd.captureScreenshot(driver, testsFileName, testName);
                String screenshotMsg="Capture screenshot: "+screenShotPath;
                log.error(screenshotMsg);
            } else {
                testResult = passedText;
                stepReportObject.put(statusText, passedText);
            }
            long stepEndTime = System.currentTimeMillis();
            stepReportObject.put("endTime", stepEndTime);
        }
        return stepReportObject;
    }

    /**
     * @auther : Ankit Mistry
     * @param driver
     * @param stepReportObject
     * @param step
     * @return
     */
    public JSONObject addStepExecutonOfannotation(WebDriver driver, JSONObject stepReportObject,String step)  {

        StepParser stepParser=new StepParser();
        StringWriter sw = new StringWriter();
        ExternalCode externalCode=new ExternalCode();

        boolean stepPassed = true;

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

        try {

            if (step.replaceAll(whiteSpace, " ").trim().startsWith(stepText)) {
                if (step.toLowerCase().contains(printText)) {
                    try {
                        stepReportObject.put(stepsText, stepParser.printStep(driver, step, test));
                    } catch (Exception e) {}
                }
                if (step.contains("{") && step.contains("}")) {

                    stepReportObject.put(stepsText, reportParser.dataSetStepReplaceValue(test, step));
                }
                String newStep=stepParser.parseStep(driver, test, step);

                if (step.toLowerCase().contains(randomText)) {
                    stepReportObject.put(stepsText,newStep.replace("@",""));
                }
            }

            if (step.replaceAll(whiteSpace, " ").trim().startsWith(verifyText)) {
                sendVerifyStep(step);

            }
        } catch (Exception ae) {
            if (step.contains("{") && step.contains("}")) {
                stepReportObject.put(stepsText, step.replaceAll("[{,}]","'").replace("@",""));
            }
            ae.printStackTrace(new PrintWriter(sw));
            exceptionAsString = sw.toString();
            tesboLogger.testFailed(failedTextMsg);
            tesboLogger.testFailed(exceptionAsString);
            log.error(failedTextMsg);
            log.error(exceptionAsString);
            stepPassed = false;
        }

        if (step.replaceAll(whiteSpace, " ").trim().startsWith(codeText)) {
            try {
                externalCode.runAllAnnotatedWith(Step.class, step,test, driver);
            }catch (Exception e){
                e.printStackTrace(new PrintWriter(sw));
                exceptionAsString = sw.toString();
                tesboLogger.testFailed(failedTextMsg);
                tesboLogger.testFailed(sw.toString());
                log.error(failedTextMsg);
                log.error(sw.toString());
                stepPassed = false;
            }
        }
        reportParser.addScreenshotUrlInReport(stepReportObject, step);
        if(stepReportObject.size()!=0) {
            stepReportObject = addStepResultInReport(driver, stepReportObject, stepPassed);
        }
        return stepReportObject;
    }

    /**
     * @param step
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     */
    public void sendVerifyStep(String step) throws Exception {
        VerifyParser verifyParser = new VerifyParser();
        StepParser stepParser = new StepParser();
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
                            String erroeMsg="'" + step + "' step is not verified";
                            log.error(erroeMsg);
                            throw new AssertException(erroeMsg);
                        }
                    }
                }
            }
        }
        else {
            verifyParser.parseVerify(driver, test, step);
        }
    }

}
