package framework;

import datacollector.BuildReportDataObject;
import execution.TestExecutionBuilder;
import logger.TesboLogger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

public class TestExecutor implements Runnable {

    TesboLogger tesboLogger = new TesboLogger();
    public WebDriver driver;
    Map<String, WebDriver> sessionList = new HashMap<>();
    JSONObject test;
    JSONArray listOfSession;
    boolean isSession = false;
    String screenShotPath = null;
    String testResult = "";
    String exceptionAsString = null;
    static JSONObject localVariable=new JSONObject();
    private static final Logger log = LogManager.getLogger(TestExecutor.class);
    TestExecutorUtility testExecutorUtility=new TestExecutorUtility();

    String testsFileName;
    String testName;
    String browser;
    String stepIndexText="stepIndex";

    public TestExecutor() { }
    public TestExecutor(JSONObject test) {
        this.test = test;
        testsFileName=test.get("testsFileName").toString();
        testName=test.get("testName").toString();
        browser=test.get("browser").toString();
    }

    /**
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        TestExecutionBuilder builder = new TestExecutionBuilder();
        ReportParser report = new ReportParser();
        builder.buildExecution();
        report.generateReportDir();
    }


    /**
     *
      */
    public void beforeTest() {
        JSONObject testSessionDetails=testExecutorUtility.getSessionListIfTestHasAnsInitializeBrowser(testsFileName,testName,isSession,driver,browser,sessionList);
        sessionList= (Map<String, WebDriver>) testSessionDetails.get("sessionList");
        driver=(WebDriver) testSessionDetails.get("driver");
        listOfSession= (JSONArray) testSessionDetails.get("listOfSession");
        isSession= (boolean) testSessionDetails.get("isSession");
    }


    /**
     *
     * @param sessionName
     */
    public void afterTest(String sessionName) {
        sessionList=testExecutorUtility.stepToExecuteAfterTest(driver,sessionName,sessionList,isSession);
    }


    /**
     *
     * @return
     */
    public JSONObject runTest() {
        BuildReportDataObject buildReport = new BuildReportDataObject();
        localVariable=new JSONObject();
        String screenShotPathText="screenShotPath";
        String testResultText="testResult";
        testResult = "";


        JSONObject testReportObject = new JSONObject();
        long startTime = System.currentTimeMillis();

        /*Adding data into the report*/
        testReportObject=testExecutorUtility.addTestDetailsOnReportObject(testReportObject,test,testName,testsFileName,browser);


        int stepIndex=0;
        JSONArray testStepArray = new JSONArray();
        testStepArray = testExecutorUtility.addTestFirstStepOfOpenBaseUrlToReportObject(testStepArray,stepIndex);
        screenShotPath = "";

        testReportObject=testExecutorUtility.getSeverityAndPriorityIfExist(test,testReportObject);

        JSONObject beforeTestStepsDetails=testExecutorUtility.executeBeforeTestStepIfExistInTestsFile(driver,testsFileName,stepIndex,testStepArray,test,testResult,screenShotPath);
        testResult= (String) beforeTestStepsDetails.get(testResultText);
        screenShotPath= (String) beforeTestStepsDetails.get(screenShotPathText);
        testStepArray= (JSONArray) beforeTestStepsDetails.get("testSteps");
        stepIndex= (int) beforeTestStepsDetails.get(stepIndexText);

        JSONObject testReportDetails=new JSONObject();
        testReportDetails.put(stepIndexText,stepIndex);
        testReportDetails.put("isSession",isSession);
        testReportDetails.put("testResult",testResult);
        testReportDetails.put("screenShotPath",screenShotPath);

        /*Getting step using testsFileName and Testcase Name*/
        JSONObject testStepsDetails=testExecutorUtility.executeTestSteps(driver,testStepArray, test,sessionList,listOfSession,testReportDetails);
        sessionList= (Map<String, WebDriver>) testStepsDetails.get("sessionList");
        testResult= (String) testStepsDetails.get(testResultText);
        screenShotPath= (String) testStepsDetails.get(screenShotPathText);
        driver= (WebDriver) testStepsDetails.get("driver");
        testStepArray= (JSONArray) testStepsDetails.get("testSteps");
        stepIndex= (int) testStepsDetails.get(stepIndexText);
        exceptionAsString= (String) testStepsDetails.get("exceptionAsString");

        JSONObject afterTestStepsDetails=testExecutorUtility.executeAfterTestStepIfExistInTestsFile(driver,testsFileName,stepIndex,testStepArray,test,testResult,screenShotPath);
        testResult= (String) afterTestStepsDetails.get(testResultText);
        screenShotPath= (String) afterTestStepsDetails.get(screenShotPathText);
        testStepArray= (JSONArray) afterTestStepsDetails.get("testStepArray");

        Capabilities caps = ((RemoteWebDriver) driver).getCapabilities();

        testReportObject=testExecutorUtility.addBrowserAndOsDetailsOnReportObject(testReportObject,caps);

        testReportObject= testExecutorUtility.addTestStepStatusAndEndTimeToReportObject(testReportObject,testStepArray,testResult,screenShotPath,startTime);

        buildReport.addDataInMainObject(browser, testsFileName, testReportObject);

        testExecutorUtility.addReportOnCloud(testName,testsFileName,testReportObject,testResult);

        testExecutorUtility.manageFailTestExecutionQueue(testResult,testName,testsFileName,test,this.test);
        return testReportObject;
    }

    /**
     *
     */
    @Override
    public void run() {
        GetConfiguration config=new GetConfiguration();
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

            if(! TestExecutionBuilder.isSingleWindow && !TestExecutionBuilder.singleWindowRun && !(!config.getBrowserClose() && testResult.equalsIgnoreCase("failed"))) {
                afterTest(null);
            }

        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            tesboLogger.testFailed(sw.toString());
            log.error(sw.toString());
        }

    }

}
