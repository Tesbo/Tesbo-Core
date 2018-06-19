package framework;

import DataCollector.BuildReportDataObject;
import Execution.TestExecutionBuilder;
import Selenium.Commands;
import io.github.bonigarcia.wdm.WebDriverManager;
import logger.Logger;
import net.bytebuddy.implementation.bytecode.Throw;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.*;
import Exception.TesboException;

public class TestExecutor implements Runnable {



   // public JSONObject testResult = new JSONObject();
    public WebDriver driver;
   // public WebDriver[] SessionDriver=null;
    JSONObject test;

    public TestExecutor(JSONObject test) {
        this.test = test;
    }
    public Map<String,WebDriver> sessionList=new HashMap<String, WebDriver>();
    JSONArray listOfSession;
    static Logger logger = new Logger();
    boolean isSession=false;

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
     * @auther :
     * @lastModifiedBy: Ankit Mistry
     *
     */
    public void beforeTest(String browserName) {
        SuiteParser suiteParser=new SuiteParser();
        listOfSession=suiteParser.getSessionListFromTest(test.get("suiteName").toString(),test.get("testName").toString());
        if(listOfSession.size()>1){ isSession=true; }
        else { initializeBrowser(null); }
    }


    public void afterTest(String sessionName) {
        if(sessionName!=null){
            for (Map.Entry session : sessionList.entrySet()) {
                if(sessionName.equals(session.getKey().toString())) {
                    driver = (WebDriver) session.getValue();
                    driver.quit();
                    sessionList.remove(session.getKey());
                    break;
                }
            }
        }
        else {
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
        BuildReportDataObject buildReport = new BuildReportDataObject();
        String testResult = "";
        int stepNumber = 0;
        Validation validation=new Validation();
        JSONObject testReportObject = new JSONObject();


        long startTime = System.currentTimeMillis();

        /*Adding data into the report*/
        testReportObject.put("startTime", startTime);
        testReportObject.put("browserName", test.get("browser"));
        testReportObject.put("testName", test.get("testName").toString());
        testReportObject.put("suiteName", test.get("suiteName").toString());


        /*Getting step using SuiteName and Testcase Name*/
        JSONArray steps = parser.getTestStepBySuiteandTestCaseName(test.get("suiteName").toString(), test.get("testName").toString());
        System.out.println("Steps :"+steps);
        int J = 0;
        JSONArray stepsArray = new JSONArray();
        boolean failFlag = false;

        String exceptionAsString = null;
        String screenShotPath = null;


        JSONArray testStepArray = new JSONArray();

        for (int i = 0; i <= steps.size() - 1; i++) {
            boolean stepPassed = true;

            JSONObject stepReportObject = new JSONObject();

            long startTimeStep = System.currentTimeMillis();
            Object step = steps.get(i);

            stepReportObject.put("stepIndex", i + 1);
            stepReportObject.put("startTime", startTimeStep);
            stepReportObject.put("steps", step.toString());


            if(isSession){
                validation.sessionNotDeclareOnTest(steps,listOfSession);
                validation.sessionNotDefineOnTest(steps,listOfSession);
                initializeSessionRunTime(step);
            }
            if (step.toString().toLowerCase().replaceAll("\\s{2,}", " ").trim().contains("step:") | step.toString().toLowerCase().replaceAll("\\s{2,}", " ").trim().contains("step :")) {
                try {
                    stepParser.parseStep(driver, test, step.toString());

                } catch (Exception ae) {
                    StringWriter sw = new StringWriter();
                    ae.printStackTrace(new PrintWriter(sw));
                    ae.printStackTrace();
                    exceptionAsString = sw.toString();
                    stepPassed = false;   }
                long stopTimeStep = System.currentTimeMillis();
                stepNumber++;
            } else if (step.toString().toLowerCase().replaceAll("\\s{2,}", " ").trim().contains("verify:") | step.toString().toLowerCase().replaceAll("\\s{2,}", " ").trim().contains("verify :")) {
                try {
                    logger.stepLog(step.toString());
                    verifyParser.parseVerify(driver, test, step.toString());
                } catch (Exception NE) {
                    J++;
                    StringWriter sw = new StringWriter();
                    NE.printStackTrace(new PrintWriter(sw));
                    NE.printStackTrace();
                    exceptionAsString = sw.toString();
                    stepPassed = false;

                }
                long stopTimeStep = System.currentTimeMillis();
                long elapsedTimeStep = stopTimeStep - startTimeStep;
                // testResult.put(stepNumber + 1, stepResult);
                stepNumber++;

                 stepNumber++;
                if(failFlag==true){
                    break;
                }
            } else if (step.toString().toLowerCase().replaceAll("\\s{2,}", " ").trim().contains("close:") | step.toString().toLowerCase().replaceAll("\\s{2,}", " ").trim().contains("close :")) {
                try {
                    logger.stepLog(step.toString());
                    String sessionName=step.toString().split(":")[1].trim();
                    boolean isSession=false;
                    for(Map.Entry session:sessionList.entrySet()){
                        if(session.getKey().toString().equals(sessionName)){
                            isSession=true;
                            break;
                        }
                    }
                    if(isSession){
                        afterTest(sessionName);
                    }
                    else{
                        throw new TesboException("Session '"+sessionName+"' is not available.");
                    }
                }
                catch (Exception e){
                    throw new TesboException("Session name is not found for close.");
                }


            } else if (step.toString().toLowerCase().replaceAll("\\s{2,}", " ").trim().contains("collection:") | step.toString().toLowerCase().replaceAll("\\s{2,}", " ").trim().contains("collection :")) {
                JSONArray groupSteps = new JSONArray();
                try {
                    groupSteps = suiteParser.getGroupTestStepBySuiteandTestCaseName(test.get("suiteName").toString(), stepParser.parseTextToEnter(test, step.toString()));
                } catch (Exception e) {
                    J++;
                    StringWriter sw = new StringWriter();
                    e.printStackTrace(new PrintWriter(sw));
                    e.printStackTrace();
                    exceptionAsString = sw.toString();
                    stepNumber++;
                    stepPassed = false;
                }

                stepsArray = new JSONArray();

                for (int s = 0; s <= groupSteps.size() - 1; s++) {
                    Object groupStep = groupSteps.get(s);

                    try {
                        if (groupStep.toString().toLowerCase().contains("step:") | groupStep.toString().toLowerCase().contains("step :")) {
                            stepParser.parseStep(driver, test, groupStep.toString());
                        } else if (groupStep.toString().toLowerCase().contains("verify:") | groupStep.toString().toLowerCase().contains("verify :")) {
                            {
                                verifyParser.parseVerify(driver, test, groupStep.toString());
                            }
                        }
                    } catch (Exception ae) {
                        J++;
                        StringWriter sw = new StringWriter();
                        ae.printStackTrace(new PrintWriter(sw));
                        ae.printStackTrace();
                        exceptionAsString = sw.toString();
                        stepPassed = false;
                    }
                }
                long stopTimeStep = System.currentTimeMillis();
                stepNumber++;
            }

            Capabilities caps = ((RemoteWebDriver) driver).getCapabilities();

            testReportObject.put("browserVersion", caps.getVersion());
            testReportObject.put("osName", caps.getPlatform().toString());


            if (!stepPassed) {
                stepReportObject.put("status", "failed");
                testResult = "failed";
                screenShotPath =  selCmd. captureScreenshot(driver,test.get("suiteName").toString(),test.get("testName").toString());
            } else {
                testResult = "passed";
                stepReportObject.put("status", "passed");
            }

            long stepEndTime = System.currentTimeMillis();

            stepReportObject.put("endTime", stepEndTime);

            testStepArray.add(stepReportObject);


            if(!stepPassed)
            {
                break;
            }

        }


        long stopTimeTest = System.currentTimeMillis();


        testReportObject.put("testStep", testStepArray);

        if (testResult.equals("failed")) {
            testReportObject.put("fullStackTrace", exceptionAsString);
            testReportObject.put("screenShot", screenShotPath);
        }

        long stopTimeSuite = System.currentTimeMillis();

        testReportObject.put("totalTime", stopTimeTest-startTime);
        testReportObject.put("status", testResult);

        buildReport.addDataInMainObject(test.get("browser").toString(), test.get("suiteName").toString(),test.get("testName").toString(), testReportObject);
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


        }catch(Exception e)
        {
            e.printStackTrace();
        }

   }

    /**
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     * @param browserName
     * @return
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     */
    public boolean IsCapabilities(String browserName) {
        GetConfiguration config = new GetConfiguration();
        JSONObject capabilities = null;
        boolean browser = false;
        try {
            capabilities = config.getCapabilities();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (capabilities != null) {
            Set<String> browserCaps = (Set<String>) capabilities.keySet();
            for (String browserCap : browserCaps) {
                if (browserCap.equalsIgnoreCase(browserName)) {
                    if (((ArrayList<String>) capabilities.get(browserCap)).size() == 0)
                        browser = false;
                    else
                        browser = true;
                }
            }
        }
        return browser;
    }

    /**
     * @return
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     * @return
     */
    public String getSeleniumAddress() {
        GetConfiguration config = new GetConfiguration();
        String seleniumAddress=null;
        try {
            seleniumAddress=config.getSeleniumAddress();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return seleniumAddress;
    }

    /**
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     * @param browserName
     * @return
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     */
    public ArrayList<String> getCapabilities(String browserName) {
        GetConfiguration config = new GetConfiguration();
        JSONObject capabilities=null;
        try {
            capabilities=config.getCapabilities();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        ArrayList<String> capabilitieList= (ArrayList<String>) capabilities.get(browserName);

        return capabilitieList;
    }

    /**
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     * @param Capabilities
     * @param capability
     * @return
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     */
    public DesiredCapabilities setCapabilities(ArrayList<String> Capabilities,DesiredCapabilities capability) {
        for(Object cap:Capabilities) {
            JSONObject objCap= (JSONObject) cap;
            Set capKey=objCap.keySet();
            Collection capValue= objCap.values();
            capability.setCapability(capKey.iterator().next().toString(),capValue.iterator().next());
        }
        return capability;
    }

    /**
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     * @param seleniumAddress
     * @param driver
     * @param capability
     * @return
     */
    public WebDriver openRemoteBrowser(WebDriver driver,DesiredCapabilities capability,String seleniumAddress) {
        try {
            driver = new RemoteWebDriver(new URL(seleniumAddress),capability);
       } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return driver;
    }

    /**
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     * @param session
     */
    public WebDriver initializeBrowser(Object session) {
        GetConfiguration config = new GetConfiguration();
        String seleniumAddress=null;
        seleniumAddress=getSeleniumAddress();
        String browserName=test.get("browser").toString();
        DesiredCapabilities capability = null;
        ArrayList capabilities=null;
        if (IsCapabilities(browserName)) {
            capabilities = getCapabilities(browserName);
            if (capabilities != null)
                capability = setCapabilities(capabilities, capability);
        }
        try {
            if (browserName.equalsIgnoreCase("firefox")) {
                capability = DesiredCapabilities.firefox();
                WebDriverManager.firefoxdriver().setup();
                System.setProperty(FirefoxDriver.SystemProperty.DRIVER_USE_MARIONETTE, "true");
                System.setProperty(FirefoxDriver.SystemProperty.BROWSER_LOGFILE, "/dev/null");
                if (seleniumAddress == null) {
                    driver = new FirefoxDriver();
                }
            }
            if (browserName.equalsIgnoreCase("chrome")) {
                capability = DesiredCapabilities.chrome();
                WebDriverManager.chromedriver().setup();

                if (seleniumAddress == null) {
                    driver = new ChromeDriver();
                }
            }
            if (browserName.equalsIgnoreCase("ie")) {
                capability = DesiredCapabilities.internetExplorer();
                WebDriverManager.iedriver().setup();
                if (seleniumAddress == null) {
                    driver = new InternetExplorerDriver();
                }

            }

            if(session !=null)
            {
                sessionList.put(session.toString(), driver);
            }

            if (seleniumAddress != null) {
                driver = openRemoteBrowser(driver, capability, seleniumAddress);
                if(session !=null)
                    sessionList.put(session.toString(), driver);
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
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     * @param step
     */
    public void initializeSessionRunTime(Object step) {
        if( step.toString().toLowerCase().replaceAll("\\s{2,}", " ").trim().contains("[") && step.toString().toLowerCase().replaceAll("\\s{2,}", " ").trim().contains("]")){
            String testStep= step.toString().replace("[", "").replace("]","");
            for(Object session:listOfSession)
            {
                if(testStep.toString().toLowerCase().equals(session.toString().toLowerCase()))
                {
                    boolean isInSessionList=false;
                    for(Map.Entry map:sessionList.entrySet()){
                        if(map.getKey().toString().toLowerCase().equals(testStep.toString().toLowerCase())) {
                            isInSessionList = true;
                            driver= (WebDriver) map.getValue();
                        }
                    }
                    if(!isInSessionList)
                    {
                        driver=initializeBrowser(session);
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

}
