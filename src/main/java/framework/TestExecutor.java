package framework;

import DataCollector.BuildReportDataObject;
import Execution.TestExecutionBuilder;
import CustomStep.*;
import Selenium.Commands;


import logger.TesboLogger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.opera.OperaDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

public class TestExecutor implements Runnable {

    TesboLogger tesboLogger = new TesboLogger();
    ReportParser reportParser=new ReportParser();
    WebDriver driver;
    Map<String, WebDriver> sessionList = new HashMap<>();
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
    TestExecutorUtility testExecutorUtility=new TestExecutorUtility();

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
        BuildReportDataObject buildReport = new BuildReportDataObject();
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
        testStepArray = testExecutorUtility.addTestFirstStepOfOpenBaseUrlToReportObject(testStepArray,stepIndex);
        screenShotPath = null;

        testReportObject=testExecutorUtility.getSeverityAndPriorityIfExist(test,testReportObject);

        JSONObject beforeTestStepsDetails=testExecutorUtility.executeBeforeTestStepIfExistInTestsFile(driver,testsFileName,stepIndex,testStepArray);
        testStepArray= (JSONArray) beforeTestStepsDetails.get("testSteps");
        stepIndex= (int) beforeTestStepsDetails.get(stepIndexText);

        /*Getting step using testsFileName and Testcase Name*/
        JSONObject testStepsDetails=testExecutorUtility.executeTestSteps(driver,testsFileName,stepIndex,testStepArray,testName,test,sessionList,listOfSession,isSession);
        testStepArray= (JSONArray) testStepsDetails.get("testSteps");
        stepIndex= (int) testStepsDetails.get(stepIndexText);
        exceptionAsString= (String) testStepsDetails.get("exceptionAsString");

        testStepArray=testExecutorUtility.executeAfterTestStepIfExistInTestsFile(driver,testsFileName,stepIndex,testStepArray);

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

        testExecutorUtility.addReportOnCloud(testName,testsFileName,testReportObject,testResult);

        testExecutorUtility.manageFailTestExecutionQueue(testResult,testName,testsFileName,test,this.test);
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

            if(! TestExecutionBuilder.isSingleWindow && !TestExecutionBuilder.singleWindowRun && !(!config.getBrowserClose() && testResult.equalsIgnoreCase(failedText))) {
                    afterTest(null);
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

        String seleniumAddress =testExecutorUtility.getSeleniumAddress();
        String browserName = browser;
        String startBrowserLog="Start Browser: "+browserName;
        log.info(startBrowserLog);
        DesiredCapabilities capability;
        try {

            JSONObject initializeDetails=testExecutorUtility.browserInitialization(driver,browserName,seleniumAddress);
            driver= (WebDriver) initializeDetails.get("driver");
            capability= (DesiredCapabilities) initializeDetails.get("capability");

            sessionList=testExecutorUtility.setSessionList(driver,session, sessionList);

            openRemoteBrowserIfSeleniumAddressExist(session,seleniumAddress,capability,sessionList);

            driver.manage().window().maximize();

            testExecutorUtility.openBaseURL(driver);

        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            tesboLogger.testFailed(sw.toString());
            log.error(sw.toString());
        }
        return driver;
    }

    public void openRemoteBrowserIfSeleniumAddressExist(Object session,String seleniumAddress, DesiredCapabilities capability,Map<String, WebDriver> sessionLists){
        if (seleniumAddress != null) {
            JSONObject remoteBrowserDetails=testExecutorUtility.setRemoteBrowser(session,driver,seleniumAddress,capability,sessionLists);
            driver= (WebDriver) remoteBrowserDetails.get("driver");
            sessionList= (Map<String, WebDriver>) remoteBrowserDetails.get("sessionList");
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
    public JSONObject addStepExecutionOfAnnotation(WebDriver driver, JSONObject stepReportObject,String step)  {

        StringWriter sw = new StringWriter();
        ExternalCode externalCode=new ExternalCode();

        boolean stepPassed = true;
        stepReportObject=testExecutorUtility.addPrintAnsRandomStepForReport(driver,step,stepReportObject,test);

        try {
            stepReportObject=testExecutorUtility.addStepForReport(driver,step,stepReportObject,test);
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



}
