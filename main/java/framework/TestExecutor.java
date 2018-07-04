package framework;

import DataCollector.BuildReportDataObject;
import Exception.TesboException;
import Execution.TestExecutionBuilder;
import ExtCode.*;
import Selenium.Commands;
import io.github.bonigarcia.wdm.WebDriverManager;
import logger.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.opera.OperaDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import java.io.PrintWriter;
import java.io.StringWriter;

import java.time.format.DateTimeFormatter;
import java.util.*;

public class TestExecutor implements Runnable {


    static Logger logger = new Logger();
    // public JSONObject testResult = new JSONObject();
    public WebDriver driver;
    public Map<String, WebDriver> sessionList = new HashMap<String, WebDriver>();
    JSONObject test;
    JSONArray listOfSession;
    boolean isSession = false;
    TestExecutionBuilder testExecutionBuilder=new TestExecutionBuilder();

    public TestExecutor() { }
    public TestExecutor(JSONObject test) {
        this.test = test;
    }

    public static void main(String[] args) throws Exception {
        TestExecutionBuilder builder = new TestExecutionBuilder();
        ReportParser report = new ReportParser();
        long startTimeSuite = System.currentTimeMillis();

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy|MM|dd HH:mm:ss");

        builder.buildExecution();

        long stopTimeSuite = System.currentTimeMillis();
        long elapsedTimeSuite = stopTimeSuite - startTimeSuite;


        DateTimeFormatter dtf1 = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

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
        SuiteParser suiteParser = new SuiteParser();
        listOfSession = suiteParser.getSessionListFromTest(test.get("suiteName").toString(), test.get("testName").toString());
        if (listOfSession.size() > 0) {
            isSession = true;
        } else {
            initializeBrowser(null);
        }
    }


    public void afterTest(String sessionName) {
        if (sessionName != null) {
            for (Map.Entry session : sessionList.entrySet()) {
                if (sessionName.equals(session.getKey().toString())) {
                    driver = (WebDriver) session.getValue();
                    driver.quit();
                    sessionList.remove(session.getKey());
                    break;
                }
            }
        } else {
            if (isSession) {
                for (Map.Entry session : sessionList.entrySet()) {
                    driver = (WebDriver) session.getValue();
                    driver.quit();
                }
            } else {
                driver.quit();
            }
        }
    }


    public JSONObject runTest() {
        SuiteParser parser = new SuiteParser();
        StepParser stepParser = new StepParser();
        Commands selCmd = new Commands();
        VerifyParser verifyParser = new VerifyParser();
        SuiteParser suiteParser = new SuiteParser();
        ExternalCode externalCode=new ExternalCode();
        BuildReportDataObject buildReport = new BuildReportDataObject();
        String testResult = "";
        int stepNumber = 0;
        JSONObject testReportObject = new JSONObject();


        long startTime = System.currentTimeMillis();

        /*Adding data into the report*/
        testReportObject.put("startTime", startTime);
        testReportObject.put("browserName", test.get("browser"));
        testReportObject.put("testName", test.get("testName").toString());
        testReportObject.put("suiteName", test.get("suiteName").toString());

        /*Getting step using SuiteName and Testcase Name*/
        JSONArray steps = parser.getTestStepBySuiteandTestCaseName(test.get("suiteName").toString(), test.get("testName").toString());
        int J = 0;
        JSONArray stepsArray = new JSONArray();
        boolean failFlag = false;

        String exceptionAsString = null;
        String screenShotPath = null;


        JSONArray testStepArray = new JSONArray();

        for (int i = 0; i < steps.size(); i++) {
            boolean stepPassed = true;

            JSONObject stepReportObject = new JSONObject();

            long startTimeStep = System.currentTimeMillis();
            Object step = steps.get(i);

            stepReportObject.put("stepIndex", i + 1);
            stepReportObject.put("startTime", startTimeStep);

            if (step.toString().contains("{") && step.toString().contains("}")) {
                ReportParser reportParser=new ReportParser();
                stepReportObject.put("steps",reportParser.detaSetStepReplaceValue(test,step.toString()));
            }
            else {
            stepReportObject.put("steps", step.toString());
            }

            if (isSession) {
                initializeSessionRunTime(step);
            }

            try {
                if (step.toString().replaceAll("\\s{2,}", " ").trim().contains("Step:")) {
                    stepParser.parseStep(driver, test, step.toString());
                }

                if (step.toString().replaceAll("\\s{2,}", " ").trim().contains("Verify:")) {
                    verifyParser.parseVerify(driver, test, step.toString());

                }
            } catch (Exception ae) {
                StringWriter sw = new StringWriter();
                ae.printStackTrace(new PrintWriter(sw));
                ae.printStackTrace();
                exceptionAsString = sw.toString();
                stepPassed = false;
            }


            if (step.toString().replaceAll("\\s{2,}", " ").trim().contains("Close:")) {

                    String sessionName = step.toString().split(":")[1].trim().replace("]","");
                    boolean isSession = false;
                    for (Map.Entry session : sessionList.entrySet()) {
                        if (session.getKey().toString().equals(sessionName)) {
                            isSession = true;
                            break;
                        }
                    }
                    if (isSession) {
                        afterTest(sessionName);
                    }

            }else if (step.toString().replaceAll("\\s{2,}", " ").trim().contains("ExtCode:")) {
                String extVal=null;
                try {
                    extVal=step.toString().split(":")[1].trim();
                } catch (Exception e) {
                    throw new TesboException("ExtCode step has no value");
                }
                externalCode.runAllAnnotatedWith(ExtCode.class,extVal,driver);

            } else if (step.toString().replaceAll("\\s{2,}", " ").trim().contains("Collection:")) {
                JSONArray groupSteps = new JSONArray();
                try {
                    groupSteps = suiteParser.getGroupTestStepBySuiteandTestCaseName(test.get("suiteName").toString(), stepParser.getCollectionName(step.toString()));
                } catch (Exception e) {
                    if (groupSteps.size() == 0)
                        throw e;
                    J++;
                    StringWriter sw = new StringWriter();
                    e.printStackTrace(new PrintWriter(sw));
                    e.printStackTrace();
                    exceptionAsString = sw.toString();
                    stepNumber++;
                    stepPassed = false;
                }


                for (int s = 0; s <= groupSteps.size() - 1; s++) {
                    Object groupStep = groupSteps.get(s);

                    if (groupStep.toString().contains("Step:")) {
                        try {
                            stepParser.parseStep(driver, test, groupStep.toString());
                        } catch (Exception ae) {
                            J++;
                            StringWriter sw = new StringWriter();
                            ae.printStackTrace(new PrintWriter(sw));
                            ae.printStackTrace();
                            exceptionAsString = sw.toString();
                            stepPassed = false;


                        }
                    } else if (groupStep.toString().contains("Verify:")) {
                        try {
                            verifyParser.parseVerify(driver, test, groupStep.toString());
                        } catch (Exception NE) {
                            J++;
                            StringWriter sw = new StringWriter();
                            NE.printStackTrace(new PrintWriter(sw));
                            NE.printStackTrace();
                            exceptionAsString = sw.toString();
                            stepPassed = false;
                        }
                    }
                }
                long stopTimeStep = System.currentTimeMillis();
                stepNumber++;
            }




            if (!stepPassed) {
                stepReportObject.put("status", "failed");
                testResult = "failed";
                screenShotPath = selCmd.captureScreenshot(driver, test.get("suiteName").toString(), test.get("testName").toString());
            } else {
                testResult = "passed";
                stepReportObject.put("status", "passed");
            }

            long stepEndTime = System.currentTimeMillis();

            stepReportObject.put("endTime", stepEndTime);



            testStepArray.add(stepReportObject);

            if (!stepPassed) {
                break;
            }

        }




        Capabilities caps = ((RemoteWebDriver) driver).getCapabilities();

        testReportObject.put("browserVersion", caps.getVersion());
        testReportObject.put("osName", caps.getPlatform().toString());

        long stopTimeTest = System.currentTimeMillis();
        testReportObject.put("testStep", testStepArray);

        if (testResult.equals("failed")) {
            testReportObject.put("fullStackTrace", exceptionAsString);
            testReportObject.put("screenShot", screenShotPath);
        }

        long stopTimeSuite = System.currentTimeMillis();

        testReportObject.put("totalTime", stopTimeTest - startTime);
        testReportObject.put("status", testResult);

        buildReport.addDataInMainObject(test.get("browser").toString(), test.get("suiteName").toString(), test.get("testName").toString(), testReportObject);
        if(testResult.toLowerCase().equals("failed")){

           testExecutionBuilder.failTestExecutionQueue(test);
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
            beforeTest(test.get("browser").toString());
            runTest();
            afterTest(null);


            //  testData.put(testResult.get("testName").toString(), testResult);
            //testData.put(testResult.get("testName").toString(), testResult);

            //addDataIntoMainObject(test.get("browser").toString(), testData);
            TestExecutionBuilder builder = new TestExecutionBuilder();


        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    /**
     * @param session
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     */
    public WebDriver initializeBrowser(Object session) {
        GetConfiguration config = new GetConfiguration();
        String seleniumAddress = null;
        Commands cmd = new Commands();
        seleniumAddress = cmd.getSeleniumAddress();
        String browserName = test.get("browser").toString();
        DesiredCapabilities capability = new DesiredCapabilities();
        JSONObject capabilities = null;
        try {

            if(config.getBinaryPath(browserName+"Path")!=null){
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
            }


            driver.manage().window().maximize();

            try {
                if (!config.getBaseUrl().equals("") || !config.getBaseUrl().equals(null)) {
                    driver.get(config.getBaseUrl());

                }
            } catch (org.openqa.selenium.WebDriverException e) {
                //e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
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
                            e.printStackTrace();
                        }
                    }


                }
            }
            logger.stepLog(step.toString());
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


}
