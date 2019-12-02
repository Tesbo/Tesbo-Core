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
import java.time.format.DateTimeFormatter;
import java.util.*;

public class TestExecutor implements Runnable {

    static TesboLogger tesboLogger = new TesboLogger();
    public boolean dataSetVariable;
    ReportParser reportParser=new ReportParser();
    public WebDriver driver;
    public Map<String, WebDriver> sessionList = new HashMap<String, WebDriver>();
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

    public TestExecutor() { }
    public TestExecutor(JSONObject test) {
        this.test = test;
    }

    public static void main(String[] args) throws Exception {
        TestExecutionBuilder builder = new TestExecutionBuilder();
        ReportParser report = new ReportParser();
        long startTimeTestFile = System.currentTimeMillis();

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy|MM|dd HH:mm:ss");

        builder.buildExecution();

        long stopTimeTestFile = System.currentTimeMillis();
        long elapsedTimeTestFile = stopTimeTestFile - startTimeTestFile;

        report.generateReportDir();
        //report.writeJsonFile(builder.reportObj, builder.getbuildReportName());
    }


    /**
     * @param browserName
     * @param browserName
     * @auther :
     * @lastModifiedBy: Ankit Mistry
     */
    public void beforeTest(String browserName) {
        TestsFileParser testsFileParser = new TestsFileParser();
        listOfSession = testsFileParser.getSessionListFromTest(test.get("testsFileName").toString(), test.get("testName").toString());
        if (listOfSession.size() > 0) {
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
                    log.info("Close browser for "+sessionName+" session");
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
        ReportParser reportParser = new ReportParser();
        BuildReportDataObject buildReport = new BuildReportDataObject();
        TesboLogger tesboLogger =new TesboLogger();
        StringWriter sw = new StringWriter();
        localVariable=new JSONObject();
        testResult = "";
        int stepNumber = 0;
        boolean isTestFail=false;

        JSONObject testReportObject = new JSONObject();
        long startTime = System.currentTimeMillis();

        /*Adding data into the report*/
        testReportObject.put("startTime", startTime);
        testReportObject.put("browserName", test.get("browser"));
        testReportObject.put("testName", test.get("testName").toString());
        testReportObject.put("testsFileName", test.get("testsFileName").toString());
        testReportObject.put("suiteName", test.get("suiteName").toString());
        testReportObject.put("tagName", test.get("tagName").toString());
        tesboLogger.testLog("Test: "+test.get("testName").toString());
        log.info("Test: "+test.get("testName").toString());

        int stepIndex=0;
        JSONArray testStepArray = new JSONArray();
        if(stepIndex==0){
            JSONObject stepReportObject = new JSONObject();
            tesboLogger.testLog("Step: Open "+config.getBaseUrl());
            log.info("Step: Open "+config.getBaseUrl());
            stepReportObject.put("stepIndex", ++stepIndex);
            stepReportObject.put("steps", "Step: Open "+config.getBaseUrl());
            stepReportObject.put("status", "passed");
            testStepArray.add(stepReportObject);

        }
        screenShotPath = null;

        log.info("Get severity and priority for test is: "+stepParser.isSeverityOrPriority(test));
        if(stepParser.isSeverityOrPriority(test)){
            JSONArray severityAndPrioritySteps=testsFileParser.getSeverityAndPriority(test);
            for (int i = 0; i < severityAndPrioritySteps.size(); i++) {
                Object step = severityAndPrioritySteps.get(i);
                if(step.toString().replaceAll("\\s{2,}", " ").trim().contains("Priority:")) {
                    tesboLogger.stepLog(step.toString());
                    log.info(step.toString());
                    testReportObject.put("Priority", step.toString().replaceAll("\\s{2,}", " ").trim().split(":")[1].trim());
                }
                if(step.toString().replaceAll("\\s{2,}", " ").trim().contains("Severity:")) {
                    tesboLogger.stepLog(step.toString());
                    log.info(step.toString());
                    testReportObject.put("Severity", step.toString().replaceAll("\\s{2,}", " ").trim().split(":")[1].trim());
                }
            }
        }

        log.info("Before test functionality is exist or not in tests file: "+testsFileParser.isBeforeTestInTestsFile(test.get("testsFileName").toString()));
        if(testsFileParser.isBeforeTestInTestsFile(test.get("testsFileName").toString())){
            JSONArray annotationSteps = testsFileParser.getBeforeAndAfterTestStepByTestsFile(test.get("testsFileName").toString(), "BeforeTest");
            for (int i = 0; i < annotationSteps.size(); i++) {

                JSONObject stepReportObject = new JSONObject();
                long startTimeStep = System.currentTimeMillis();
                Object step = annotationSteps.get(i);

                if(step.toString().toLowerCase().contains("pause") )
                {
                    if(config.getPauseStepDisplay()){ stepReportObject.put("stepIndex", ++stepIndex); }
                }
                else{ stepReportObject.put("stepIndex", ++stepIndex); }

                stepReportObject.put("startTime", startTimeStep);

                stepReportObject=addStepExecutonOfannotation(driver,stepReportObject,step.toString());

                if(stepReportObject.size()!=0) {
                    if(step.toString().toLowerCase().contains("pause") )
                    {
                        if(config.getPauseStepDisplay()){ testStepArray.add(stepReportObject); }
                    }
                    else { testStepArray.add(stepReportObject); }
                }
                if (stepReportObject.get("status").equals("failed")) {
                    isTestFail=true;
                    break;
                }

            }
        }

        /*Getting step using testsFileName and Testcase Name*/
        log.info("Get steps for "+test.get("testName").toString()+" test from "+test.get("testsFileName").toString()+" tests file");
        JSONArray steps = testsFileParser.getTestStepByTestsFileandTestCaseName(test.get("testsFileName").toString(), test.get("testName").toString());

        int J = 0;
        log.info(test.get("testName").toString()+" test has "+steps.size()+" steps");
        IfStepParser ifStepParser=new IfStepParser();

        for(int i = 0; i < steps.size(); i++) {
                        boolean stepPassed = true;
            JSONObject stepReportObject = new JSONObject();
            long startTimeStep = System.currentTimeMillis();
            Object step = steps.get(i);

            if(step.toString().contains("If::")){
                try{
                    steps= ifStepParser.getStepsOfTestWhoHasIfCondition(driver,test,steps);
                    try {
                        step = steps.get(i);
                        if (step.toString().contains("If::")) {
                            i--;
                            continue;
                        }
                    }catch (Exception e){ continue; }
                } catch (Exception e) {
                    e.printStackTrace(new PrintWriter(sw));
                    exceptionAsString = sw.toString();
                    tesboLogger.testFailed("Failed");
                    tesboLogger.testFailed(exceptionAsString);
                    stepPassed = false;
                    log.error("Failed");
                    log.error(exceptionAsString);
                }

            }

            if (!step.toString().replaceAll("\\s{2,}", " ").trim().contains("Collection:")) {
                if(step.toString().toLowerCase().contains("pause") )
                {
                    if(config.getPauseStepDisplay()){ stepReportObject.put("stepIndex", ++stepIndex); }
                }
                else{
                    stepReportObject.put("stepIndex", ++stepIndex);
                }

                stepReportObject.put("startTime", startTimeStep);

                if ( !(step.toString().contains("{") && step.toString().contains("}") && step.toString().contains("print") && step.toString().contains("random"))) {
                    if(step.toString().toLowerCase().contains("pause") )
                    {
                        if(config.getPauseStepDisplay()){stepReportObject.put("steps", step.toString().replace("@", "")); }
                    }
                    else if(step.toString().contains("{") && step.toString().contains("}") && step.toString().contains("Code")){
                        stepReportObject.put("steps", stepParser.replaceArgsOfCodeStep(test,step.toString()));
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
                                if(word.contains("@") && word.contains("'")){flag=true;}
                            }
                            //String removeContent=step.split("@")[1].trim().split(" ")[0].replace("@","");
                            if(removeContent!=null && !flag) {
                                if (removeContent.contains(".")) {
                                    stepReportObject.put("steps", step.toString().replace("@" + removeContent, removeContent.split("\\.")[1]));
                                } else {
                                    stepReportObject.put("steps", step.toString().replace("@" + removeContent, removeContent));
                                }
                            }

                        }
                        else {
                            stepReportObject.put("steps", step.toString().replace("@", ""));
                        }
                    }
                }
                if (step.toString().toLowerCase().contains("print")) {
                    try {
                        stepReportObject.put("steps", stepParser.printStep(driver, step.toString(), test));
                    } catch (Exception e) {}
                }
            }

            if (isSession) {
                log.info("Start session for "+step);
                initializeSessionRunTime(step);
            }
            try {

                if (step.toString().replaceAll("\\s{2,}", " ").trim().contains("Step:")) {
                    if (step.toString().contains("{") && step.toString().contains("}")) {
                        stepReportObject.put("steps", reportParser.dataSetStepReplaceValue(test, step.toString()));
                    }
                    String Step = stepParser.parseStep(driver, test, step.toString());

                    if (step.toString().toLowerCase().contains("random")) {
                        stepReportObject.put("steps", Step.replace("@", ""));
                    }
                }

                if (step.toString().replaceAll("\\s{2,}", " ").trim().contains("Verify:")) {
                    //verifyParser.parseVerify(driver, test, step.toString());
                    sendVerifyStep(step.toString());

                }
            } catch (Exception ae) {
                if (step.toString().contains("{") && step.toString().contains("}")) {
                    stepReportObject.put("steps", step.toString().replaceAll("[{,}]", "'").replace("@", ""));
                }
                ae.printStackTrace(new PrintWriter(sw));
                exceptionAsString = sw.toString();
                tesboLogger.testFailed("Failed");
                tesboLogger.testFailed(exceptionAsString);
                stepPassed = false;
                log.error("Failed");
                log.error(exceptionAsString);
            }

            if (step.toString().replaceAll("\\s{2,}", " ").trim().contains("Close:")) {

                String sessionName = step.toString().split(":")[1].trim().replace("]", "");
                boolean isSession = false;
                for (Map.Entry session : sessionList.entrySet()) {
                    if (session.getKey().toString().equals(sessionName)) {
                        isSession = true;
                        break;
                    }
                }
                if (isSession) {
                    afterTest(sessionName);
                    log.info(sessionName+" session is closed");
                }

            } else if (step.toString().replaceAll("\\s{2,}", " ").trim().contains("Code:")) {

                try {
                    if (step.toString().contains("{") && step.toString().contains("}")) {
                        tesboLogger.stepLog(stepParser.replaceArgsOfCodeStep(test,step.toString()));
                        log.info(stepParser.replaceArgsOfCodeStep(test,step.toString()));
                    }else {
                        tesboLogger.stepLog(step.toString());
                        log.info(step.toString());
                    }
                    externalCode.runAllAnnotatedWith(Step.class, step.toString(),test, driver);
                } catch (Exception e) {
                    e.printStackTrace(new PrintWriter(sw));
                    exceptionAsString = sw.toString();
                    tesboLogger.testFailed("Failed");
                    tesboLogger.testFailed(sw.toString());
                    log.error("Failed");
                    log.error(sw.toString());
                    stepPassed = false;

                }

            } else if (step.toString().replaceAll("\\s{2,}", " ").trim().contains("Collection:")) {
                log.info("Start "+step.toString());
                JSONArray groupSteps = new JSONArray();
                try {
                    log.info("Get steps for "+step.toString());
                    groupSteps = testsFileParser.getGroupTestStepByTestFileandTestCaseName(test.get("testsFileName").toString(), stepParser.getCollectionName(step.toString()));
                } catch (Exception e) {
                    if (groupSteps.size() == 0)
                        throw e;
                    J++;
                    e.printStackTrace(new PrintWriter(sw));
                    exceptionAsString = sw.toString();
                    tesboLogger.testFailed("Failed");
                    tesboLogger.testFailed(sw.toString());
                    log.error("Failed");
                    log.error(sw.toString());
                    stepNumber++;
                    stepPassed = false;
                }
                for (int s = 0; s <= groupSteps.size() - 1; s++) {
                    Object groupStep = groupSteps.get(s);

                    startTimeStep = System.currentTimeMillis();
                    step = steps.get(i);
                    stepReportObject.put("stepIndex", ++stepIndex);
                    stepReportObject.put("startTime", startTimeStep);
                    stepReportObject.put("steps", groupStep.toString().replace("@", ""));

                    if (groupStep.toString().contains("Step:")) {
                        try {
                            stepParser.parseStep(driver, test, groupStep.toString());
                        } catch (Exception ae) {
                            J++;
                            ae.printStackTrace(new PrintWriter(sw));
                            exceptionAsString = sw.toString();
                            tesboLogger.testFailed("Failed");
                            tesboLogger.testFailed(sw.toString());
                            log.error("Failed");
                            log.error(sw.toString());
                            stepPassed = false;
                        }
                    } else if (groupStep.toString().contains("Verify:")) {
                        try {
                            //verifyParser.parseVerify(driver, test, groupStep.toString());
                            sendVerifyStep(groupStep.toString());
                        } catch (Exception NE) {
                            J++;
                            NE.printStackTrace(new PrintWriter(sw));
                            exceptionAsString = sw.toString();
                            tesboLogger.testFailed("Failed");
                            tesboLogger.testFailed(sw.toString());
                            log.error("Failed");
                            log.error(sw.toString());
                            stepPassed = false;
                        }
                    }
                    else if (groupStep.toString().replaceAll("\\s{2,}", " ").trim().contains("Code:")) {

                        try {
                            if (step.toString().contains("{") && step.toString().contains("}")) {
                                tesboLogger.stepLog(stepParser.replaceArgsOfCodeStep(test,groupStep.toString()));
                                log.info(stepParser.replaceArgsOfCodeStep(test,groupStep.toString()));
                            }else {
                                tesboLogger.stepLog(groupStep.toString());
                                log.info(groupStep.toString());
                            }
                            externalCode.runAllAnnotatedWith(Step.class, groupStep.toString(),test, driver);
                        } catch (Exception e) {
                            e.printStackTrace(new PrintWriter(sw));
                            exceptionAsString = sw.toString();
                            tesboLogger.testFailed("Failed");
                            tesboLogger.testFailed(sw.toString());
                            log.error("Failed");
                            log.error(sw.toString());
                            stepPassed = false;
                        }
                    }
                    reportParser.addScreenshotUrlInReport(stepReportObject, step.toString());
                    stepReportObject = addStepResultInReport(driver, stepReportObject, test, stepPassed);
                    testStepArray.add(stepReportObject);
                    stepReportObject = new JSONObject();
                }
                stepNumber++;
            }

            reportParser.addScreenshotUrlInReport(stepReportObject, step.toString());
            if (stepReportObject.size() != 0) {
                stepReportObject = addStepResultInReport(driver, stepReportObject, test, stepPassed);

                if(step.toString().toLowerCase().contains("pause") )
                {
                    if(config.getPauseStepDisplay()){ testStepArray.add(stepReportObject); }
                }
                else { testStepArray.add(stepReportObject); }
            }
            if (!stepPassed) {
                break;
            }

            if (step.toString().replaceAll("\\s{2,}", " ").trim().contains("Code:") && Reporter.printStepReportObject.size()!=0) {
                for (int j = 0; j < Reporter.printStepReportObject.size(); j++){
                    JSONObject ExtStep = new JSONObject();
                    JSONObject printExtStep = new JSONObject();
                    ExtStep= (JSONObject) Reporter.printStepReportObject.get(j);
                    printExtStep.put("stepIndex", ++stepIndex);
                    printExtStep.put("steps",ExtStep.get("steps"));
                    printExtStep.put("status","passed");
                    tesboLogger.stepLog(ExtStep.get("steps").toString());
                    log.info(ExtStep.get("steps").toString());
                    testStepArray.add(printExtStep);
                }

                Reporter.printStepReportObject = new JSONArray();
            }

        }
        if (testsFileParser.isAfterTestInTestsFile(test.get("testsFileName").toString())) {
            JSONArray annotationSteps = testsFileParser.getBeforeAndAfterTestStepByTestsFile(test.get("testsFileName").toString(), "AfterTest");
            for (int i = 0; i < annotationSteps.size(); i++) {

                JSONObject stepReportObject = new JSONObject();
                long startTimeStep = System.currentTimeMillis();
                Object step = annotationSteps.get(i);

                if(step.toString().toLowerCase().contains("pause") )
                {
                    if(config.getPauseStepDisplay()){ stepReportObject.put("stepIndex", ++stepIndex); }
                }
                else{ stepReportObject.put("stepIndex", ++stepIndex); }
                stepReportObject.put("startTime", startTimeStep);

                stepReportObject = addStepExecutonOfannotation(driver, stepReportObject, step.toString());

                if (stepReportObject.size() != 0) {
                    if(step.toString().toLowerCase().contains("pause") )
                    {
                        if(config.getPauseStepDisplay()){ testStepArray.add(stepReportObject); }
                    }
                    else { testStepArray.add(stepReportObject); }
                }
                if (stepReportObject.get("status").equals("failed")) {
                    break;
                }

            }
        }

        Capabilities caps = ((RemoteWebDriver) driver).getCapabilities();

        testReportObject.put("browserVersion", caps.getVersion());
        String osName= caps.getPlatform().toString();
        if(osName.toLowerCase().equals("xp")) { osName = "windows"; }
        testReportObject.put("osName", osName);

        long stopTimeTest = System.currentTimeMillis();
        testReportObject.put("testStep", testStepArray);

        if (testResult.equals("failed")) {
            testReportObject.put("fullStackTrace", exceptionAsString);
            testReportObject.put("screenShot", screenShotPath);
        }
        testReportObject.put("totalTime", stopTimeTest - startTime);
        testReportObject.put("status", testResult);

        buildReport.addDataInMainObject(test.get("browser").toString(), test.get("testsFileName").toString(), test.get("testName").toString(), testReportObject);

        ReportAPIConfig reportAPIConfig = new ReportAPIConfig();
        if(config.getIsCloudIntegration()) {
            boolean isAddOnCloud=false;
            if(testsFileParser.isRetry(test.get("testsFileName").toString(), test.get("testName").toString()).toLowerCase().equals("null") || testsFileParser.isRetry(test.get("testsFileName").toString(), test.get("testName").toString()).toLowerCase().equals("false")){
                if(!(Integer.parseInt(config.getRetryAnalyser())>0) || testsFileParser.isRetry(test.get("testsFileName").toString(), test.get("testName").toString()).toLowerCase().equals("false")){
                    isAddOnCloud=true;
                }
                else {
                    if(testExecutionBuilder.failTest == Integer.parseInt(config.getRetryAnalyser())){
                        isAddOnCloud=true;
                    }
                    else if(testResult.toLowerCase().equals("passed")){
                        isAddOnCloud=true;
                    }
                }
            }
            else {
                if(testExecutionBuilder.failTest == Integer.parseInt(config.getRetryAnalyser())){
                    isAddOnCloud=true;
                }
                else if(testResult.toLowerCase().equals("passed")){
                    isAddOnCloud=true;
                }
            }
            if (isAddOnCloud) {reportAPIConfig.organiazeDataForCloudReport(testReportObject);}
        }
        if(testResult.toLowerCase().equals("failed")){
            if(testsFileParser.isRetry(test.get("testsFileName").toString(), test.get("testName").toString()).toLowerCase().equals("null")){
                testExecutionBuilder.failTestExecutionQueue(test);
            }
        }
        else {
            Object removeTest=null;
            if(TestExecutionBuilder.failTestQueue.size()>0){
                for(Object test:TestExecutionBuilder.failTestQueue){
                    if(test.equals(this.test)){
                        removeTest=test;
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
            JSONObject testData = new JSONObject();
            TestExecutionBuilder testExecutionBuilder=new TestExecutionBuilder();
            if(testExecutionBuilder.isSingleWindow) {
                testExecutionBuilder.isSingleWindow=false;
                beforeTest(test.get("browser").toString());
                testExecutionBuilder.driver=driver;
            }
            if(! testExecutionBuilder.isSingleWindow && testExecutionBuilder.singleWindowRun) {
                driver=testExecutionBuilder.driver;
            }

            if(! testExecutionBuilder.isSingleWindow && !testExecutionBuilder.singleWindowRun) {
                beforeTest(test.get("browser").toString());
            }

            runTest();

            if(! testExecutionBuilder.isSingleWindow && !testExecutionBuilder.singleWindowRun) {
                if(!(!config.getBrowserClose() && testResult.toLowerCase().equals("failed"))) {
                    afterTest(null);
                }
            }
            TestExecutionBuilder builder = new TestExecutionBuilder();

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
        GetConfiguration config = new GetConfiguration();
        SetCommandLineArgument setCommandLineArgument=new SetCommandLineArgument();
        String seleniumAddress = null;
        Commands cmd = new Commands();
        if(config.getIsGrid() || Boolean.valueOf(setCommandLineArgument.IsGrid)) {
            seleniumAddress = cmd.getSeleniumAddress();
        }
        String browserName = test.get("browser").toString();
        log.info("Start Browser: "+browserName);
        DesiredCapabilities capability = new DesiredCapabilities();
        JSONObject capabilities = null;
        try {

            if(config.getBinaryPath(browserName+"Path")!=null){
                log.info("Binary path of "+browserName+": "+config.getBinaryPath(browserName+"Path"));
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

                if (browserName.equalsIgnoreCase("firefox")) {
                    capability.setCapability("browserName","firefox");
                    WebDriverManager.firefoxdriver().setup();
                    System.setProperty(FirefoxDriver.SystemProperty.DRIVER_USE_MARIONETTE, "true");
                    System.setProperty(FirefoxDriver.SystemProperty.BROWSER_LOGFILE, "/dev/null");
                    if (seleniumAddress == null) {
                        driver = new FirefoxDriver();
                    }
                }
                if (browserName.equalsIgnoreCase("chrome")) {
                    capability.setCapability("browserName","chrome");
                    WebDriverManager.chromedriver().setup();
                    if (seleniumAddress == null) {
                        driver = new ChromeDriver();
                    }
                }
                if (browserName.equalsIgnoreCase("ie")) {
                    capability.setCapability("browserName","internetExplorer");
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
                log.info("Start test with selenium address: "+seleniumAddress);
            }

            driver.manage().window().maximize();

            try {
                if (!config.getBaseUrl().equals("") || !config.getBaseUrl().equals(null)) {
                    driver.get(config.getBaseUrl());
                    log.info("Start browser with '"+config.getBaseUrl()+"' URL");
                }
            } catch (org.openqa.selenium.WebDriverException e) {
                //e.printStackTrace();
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
        if (step.toString().replaceAll("\\s{2,}", " ").trim().contains("[") && step.toString().replaceAll("\\s{2,}", " ").trim().contains("]")) {
            String testStep = step.toString().replace("[", "").replace("]", "");
            for (Object session : listOfSession) {
                if (testStep.toString().toLowerCase().equals(session.toString().toLowerCase())) {
                    boolean isInSessionList = false;
                    for (Map.Entry map : sessionList.entrySet()) {
                        if (map.getKey().toString().toLowerCase().equals(testStep.toString().toLowerCase())) {
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
            log.info(step.toString());
        }

    }

    /**
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     * @param browserName
     * @return
     */
    public WebDriver initializeBrowserFromBinaryPath(String browserName) {
        GetConfiguration config = new GetConfiguration();
        if(config.getBinaryPath(browserName+"Path")!=null){
            if (browserName.equalsIgnoreCase("firefox")) {
                System.setProperty("webdriver.gecko.driver", config.getBinaryPath(browserName+"Path"));
                driver = new FirefoxDriver();
            }
            if (browserName.equalsIgnoreCase("chrome")) {
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
     * @param test
     * @param stepPassed
     * @return
     */
    public JSONObject addStepResultInReport(WebDriver driver, JSONObject stepReportObject,JSONObject test, boolean stepPassed)  {
        if(stepReportObject.size()!=0) {
            if (!stepPassed) {
                stepReportObject.put("status", "failed");
                testResult = "failed";
                screenShotPath = cmd.captureScreenshot(driver, test.get("testsFileName").toString(), test.get("testName").toString());
                log.error("Capture screenshot: "+screenShotPath);
            } else {
                testResult = "passed";
                stepReportObject.put("status", "passed");
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

        if (!(step.contains("{") && step.contains("}") && step.contains("print") && step.contains("random")))  {
            stepReportObject.put("steps", step.replace("@",""));
        }
        if (step.contains("print"))  {
            try {
                stepReportObject.put("steps",stepParser.printStep(driver,step,test));
            } catch (Exception e) {
                e.printStackTrace(new PrintWriter(sw));
                tesboLogger.testFailed(sw.toString());
                log.error(sw.toString());
            }
        }

        try {
            if (step.replaceAll("\\s{2,}", " ").trim().contains("Step:")) {
                if (step.contains("{") && step.contains("}")) {

                    stepReportObject.put("steps", reportParser.dataSetStepReplaceValue(test, step));
                }
                String Step=stepParser.parseStep(driver, test, step);

                if (step.toLowerCase().contains("random")) {
                    stepReportObject.put("steps",Step.replace("@",""));
                }
            }

            if (step.replaceAll("\\s{2,}", " ").trim().contains("Verify:")) {
                //verifyParser.parseVerify(driver, test, step.toString());
                sendVerifyStep(step);

            }
        } catch (Exception ae) {
            if (step.contains("{") && step.contains("}")) {
                stepReportObject.put("steps", step.replaceAll("[{,}]","'").replace("@",""));
            }
            ae.printStackTrace(new PrintWriter(sw));
            exceptionAsString = sw.toString();
            tesboLogger.testFailed("Failed");
            tesboLogger.testFailed(exceptionAsString);
            log.error("Failed");
            log.error(exceptionAsString);
            stepPassed = false;
        }

        if (step.replaceAll("\\s{2,}", " ").trim().contains("Code:")) {
            try {
                externalCode.runAllAnnotatedWith(Step.class, step,test, driver);
            }catch (Exception e){
                e.printStackTrace(new PrintWriter(sw));
                exceptionAsString = sw.toString();
                tesboLogger.testFailed("Failed");
                tesboLogger.testFailed(sw.toString());
                log.error("Failed");
                log.error(sw.toString());
                stepPassed = false;
            }
        }
        reportParser.addScreenshotUrlInReport(stepReportObject, step);
        if(stepReportObject.size()!=0) {
            stepReportObject = addStepResultInReport(driver, stepReportObject, test, stepPassed);
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
        String verifyStep = stepParser.RemovedVerificationTextFromSteps(step);
        if (count >= 2) {
            if (!(verifyStep.toLowerCase().contains(" and ") | verifyStep.toLowerCase().contains(" or "))) {
                stepParser.listOfSteps(step, count);
                for (String newStep : stepParser.listOfSteps(step, count)) {
                    verifyParser.parseVerify(driver, test, newStep);
                }
            }else if (verifyStep.toLowerCase().contains(" and "))
            {
                for(String newStep:stepParser.ListOfStepWhoHasSameVerifier(step,"and")){
                    verifyParser.parseVerify(driver, test,newStep);
                }
            }
            else if (verifyStep.toLowerCase().contains(" or "))
            {
                int failCount = 0;
                for (String orConditionStep : stepParser.ListOfStepWhoHasSameVerifier(step,"or")) {
                    try {
                        verifyParser.parseVerify(driver, test, orConditionStep);
                    } catch (Exception e) {
                        failCount++;
                        if (failCount == stepParser.ListOfStepWhoHasSameVerifier(step,"or").size()) {
                            log.error("'" + step + "' step is not verified");
                            throw new AssertException("'" + step + "' step is not verified");
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
