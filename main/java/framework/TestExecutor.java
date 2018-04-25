package framework;

import Execution.TestExecutionBuilder;

import io.github.bonigarcia.wdm.ChromeDriverManager;
import io.github.bonigarcia.wdm.FirefoxDriverManager;
import io.github.bonigarcia.wdm.InternetExplorerDriverManager;
import io.github.bonigarcia.wdm.OperaDriverManager;
import logger.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class TestExecutor implements Runnable {


    public JSONObject testResult = new JSONObject();
    public WebDriver driver;

    JSONObject test;

    static Logger logger = new Logger();

    public static void main(String[] args) throws Exception {
        TestExecutionBuilder builder = new TestExecutionBuilder();
        ReportParser report = new ReportParser();
        long startTimeSuite = System.currentTimeMillis();

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy|MM|dd HH:mm:ss");
        builder.reportObj.put("startTime", dtf.format(LocalDateTime.now()));

        builder.buildExecution();

        long stopTimeSuite = System.currentTimeMillis();
        builder.reportObj.put("endTime", dtf.format(LocalDateTime.now()));
        long elapsedTimeSuite = stopTimeSuite - startTimeSuite;

        builder.reportObj.put("totalTimeTaken", elapsedTimeSuite);

        DateTimeFormatter dtf1 = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

        report.generateReportDir();
        //report.writeJsonFile(builder.reportObj, builder.getbuildReportName());
        logger.stepLog("Report : "+builder.reportObj);
    }

    public TestExecutor(JSONObject test) {
        this.test = test;
    }

    /**
     * @auther :
     * @lastModifiedBy: Ankit Mistry
     *
     * @param browserName
     */
    public void beforeTest(String browserName) {

        GetConfiguration config = new GetConfiguration();
        DesiredCapabilities capability = null;
        String seleniumAddress=null;
        ArrayList capabilities=null;
        seleniumAddress=getSeleniumAddress();
        if(IsCapabilities(browserName)) {
            capabilities=getCapabilities(browserName);
        }
        try {


            if(seleniumAddress ==null)
            {
                if (browserName.equalsIgnoreCase("firefox")) {
                    FirefoxDriverManager.getInstance().setup();
                    System.setProperty(FirefoxDriver.SystemProperty.DRIVER_USE_MARIONETTE,"true");
                    System.setProperty(FirefoxDriver.SystemProperty.BROWSER_LOGFILE,"/dev/null");
                    if(seleniumAddress==null) {
                        driver = new FirefoxDriver();
                    }
                }
                if (browserName.equalsIgnoreCase("chrome")) {
                    ChromeDriverManager.getInstance().setup();
                    if(seleniumAddress==null) {
                        driver = new ChromeDriver();
                    }
                }
                if (browserName.equalsIgnoreCase("ie")) {
                    InternetExplorerDriverManager.getInstance().setup();
                    if(seleniumAddress==null) {
                        driver = new InternetExplorerDriver();
                    }
                }

            }else {
                capability= setCapabilities(capabilities,capability);
                driver=openRemoteBrowser(driver,capability,seleniumAddress);
            }


            driver.manage().window().maximize();

            try {
                if (!config.getBaseUrl().equals("") || !config.getBaseUrl().equals(null)) {
                    driver.get(config.getBaseUrl());
                }
            }
            catch (org.openqa.selenium.WebDriverException e) { }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void afterTest() {
        driver.quit();
    }


    public JSONObject runTest() {
        SuiteParser parser = new SuiteParser();
        StepParser stepParser = new StepParser();
        VerifyParser verifyParser = new VerifyParser();
        Capabilities caps = ((RemoteWebDriver) driver).getCapabilities();
        SuiteParser suiteParser = new SuiteParser();
        int stepNumber = 0;
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy|MM|dd HH:mm:ss");
        long startTimeSuite = System.currentTimeMillis();
        testResult.put("startTime", dtf.format(LocalDateTime.now()));
        testResult.put("testName", test.get("testName").toString());

        logger.testLog("Test :" + test.get("testName").toString());

        JSONArray steps = parser.getTestStepBySuiteandTestCaseName(test.get("suiteName").toString(), test.get("testName").toString());
        int J = 0;
        JSONArray stepsArray = new JSONArray();
        boolean failFlag = false;

        String exceptionAsString = null;


        for (int i = 0; i <= steps.size() - 1; i++) {
            JSONObject stepResult = new JSONObject();
            long startTimeStep = System.currentTimeMillis();
            Object step = steps.get(i);
            if (step.toString().toLowerCase().replaceAll("\\s{2,}", " ").trim().contains("step:") | step.toString().toLowerCase().replaceAll("\\s{2,}", " ").trim().contains("step :")) {
                stepResult.put("startTime", dtf.format(LocalDateTime.now()));
                stepResult.put("stepIndex", stepNumber + 1);
                try {
                    stepParser.parseStep(driver, test, step.toString());
                    stepResult.put("steps", (((step.toString().split(":"))[1]).replace('@', ' ')).replace("  ", " "));
                    stepResult.put("status", "pass");
                } catch (Exception ae) {
                    J++;
                    StringWriter sw = new StringWriter();
                    ae.printStackTrace(new PrintWriter(sw));
                    ae.printStackTrace();
                    exceptionAsString = sw.toString();
                    stepResult.put("steps", (((step.toString().split(":"))[1]).replace('@', ' ')).replace("  ", " "));
                    stepResult.put("status", "fail");
                    stepResult.put("errorMsg", "Please review steps. Alert is not display.");
                    failFlag = true;
                }
                long stopTimeStep = System.currentTimeMillis();
                stepResult.put("endTime", dtf.format(LocalDateTime.now()));
                long elapsedTimeStep = stopTimeStep - startTimeStep;
                stepResult.put("time", elapsedTimeStep);
                //testResult.put(stepNumber + 1, stepResult);
                stepsArray.add(stepResult);
                stepNumber++;
                if(failFlag==true){
                    break;
                }
            } else if (step.toString().toLowerCase().replaceAll("\\s{2,}", " ").trim().contains("verify:") | step.toString().toLowerCase().replaceAll("\\s{2,}", " ").trim().contains("verify :")) {
                stepResult.put("startTime", dtf.format(LocalDateTime.now()));
                stepResult.put("stepIndex", stepNumber + 1);
                try {
                    logger.stepLog(step.toString());
                    verifyParser.parseVerify(driver, test, step.toString());
                    stepResult.put("steps", (((step.toString().split(":"))[1]).replace('@', ' ')).replace("  ", " "));
                    stepResult.put("status", "pass");
                } catch (NoSuchElementException NE) {
                    J++;
                    StringWriter sw = new StringWriter();
                    NE.printStackTrace(new PrintWriter(sw));
                    NE.printStackTrace();
                    exceptionAsString = sw.toString();
                    stepResult.put("steps", (((step.toString().split(":"))[1]).replace('@', ' ')).replace("  ", " "));
                    stepResult.put("status", "fail");
                    stepResult.put("errorMsg", "Locator not find. Please add new locator or update the locator.");
                    stepResult.put("fullsTackTrace", exceptionAsString);
                    failFlag = true;
                } catch (NullPointerException npe) {
                    J++;
                    StringWriter sw = new StringWriter();
                    npe.printStackTrace(new PrintWriter(sw));
                    npe.printStackTrace();
                    exceptionAsString = sw.toString();
                    stepResult.put("steps", (((step.toString().split(":"))[1]).replace('@', ' ')).replace("  ", " "));
                    stepResult.put("status", "fail");
                    stepResult.put("errorMsg", "Data not found. Please add data.");
                    stepResult.put("fullsTackTrace", exceptionAsString);
                    failFlag = true;
                } catch (FileNotFoundException fe) {
                    J++;
                    StringWriter sw = new StringWriter();
                    fe.printStackTrace(new PrintWriter(sw));
                    fe.printStackTrace();
                    exceptionAsString = sw.toString();
                    stepResult.put("steps", (((step.toString().split(":"))[1]).replace('@', ' ')).replace("  ", " "));
                    stepResult.put("status", "fail");
                    stepResult.put("errorMsg", "Please Enter valid directory path. " + fe.getMessage());
                    logger.errorLog("Please Enter valid directory path. " + fe.getMessage());
                    stepResult.put("fullStackTrace", exceptionAsString);
                    failFlag = true;
                } catch (Exception e) {
                    J++;
                    StringWriter sw = new StringWriter();
                    e.printStackTrace(new PrintWriter(sw));
                    e.printStackTrace();
                    exceptionAsString = sw.toString();
                    stepResult.put("steps", (((step.toString().split(":"))[1]).replace('@', ' ')).replace("  ", " "));
                    stepResult.put("status", "fail");
                    stepResult.put("errorMsg", "Please check the steps.");
                    stepResult.put("fullStackTrace", exceptionAsString);
                    failFlag = true;
                }
                long stopTimeStep = System.currentTimeMillis();
                stepResult.put("endTime", dtf.format(LocalDateTime.now()));
                long elapsedTimeStep = stopTimeStep - startTimeStep;
                stepResult.put("time", elapsedTimeStep);
                // testResult.put(stepNumber + 1, stepResult);
                stepsArray.add(stepResult);
                stepNumber++;
                if(failFlag==true){
                    break;
                }
            } else if (step.toString().toLowerCase().replaceAll("\\s{2,}", " ").trim().contains("collection:") | step.toString().toLowerCase().replaceAll("\\s{2,}", " ").trim().contains("collection :")) {
                JSONArray groupSteps = new JSONArray();
                try {
                    groupSteps = suiteParser.getGroupTestStepBySuiteandTestCaseName(test.get("suiteName").toString(), stepParser.parseTextToEnter(test,step.toString()));
                } catch (Exception e) {
                    J++;
                    stepResult.put("startTime", dtf.format(LocalDateTime.now()));
                    stepResult.put("stepIndex", stepNumber + 1);
                    StringWriter sw = new StringWriter();
                    e.printStackTrace(new PrintWriter(sw));
                    e.printStackTrace();
                    exceptionAsString = sw.toString();
                    stepResult.put("steps", (((step.toString().split(":"))[1]).replace('@', ' ')).replace("  ", " "));
                    stepResult.put("status", "fail");
                    stepResult.put("errorMsg", "Please enter valid collection name.");
                    stepResult.put("fullStackTrace", exceptionAsString);
                    long stopTimeStep = System.currentTimeMillis();
                    stepResult.put("endTime", dtf.format(LocalDateTime.now()));
                    long elapsedTimeStep = stopTimeStep - startTimeStep;
                    stepResult.put("time", elapsedTimeStep);
                    //testResult.put(stepNumber + 1, stepResult);
                    stepsArray.add(stepResult);
                    stepNumber++;
                    failFlag = true;
                }

                stepsArray = new JSONArray();

                for (int s = 0; s <= groupSteps.size() - 1; s++) {
                    JSONObject groupResult = new JSONObject();
                    Object groupStep = groupSteps.get(s);
                    groupResult.put("startTime", dtf.format(LocalDateTime.now()));
                    groupResult.put("stepIndex", stepNumber + 1);

                    if (groupStep.toString().toLowerCase().contains("step:") | groupStep.toString().toLowerCase().contains("step :")) {
                        try {
                            stepParser.parseStep(driver, test, groupStep.toString());
                            groupResult.put("steps", (((groupStep.toString().split(":"))[1]).replace('@', ' ')).replace("  ", " "));
                            groupResult.put("status", "pass");
                        } catch (NoAlertPresentException ae) {
                            J++;
                            StringWriter sw = new StringWriter();
                            ae.printStackTrace(new PrintWriter(sw));
                            ae.printStackTrace();
                            exceptionAsString = sw.toString();
                            groupResult.put("steps", (((groupStep.toString().split(":"))[1]).replace('@', ' ')).replace("  ", " "));
                            groupResult.put("status", "fail");
                            groupResult.put("errorMsg", "Please review steps. Alert is not display.");
                            groupResult.put("fullStackTrace", exceptionAsString);
                            break;
                        } catch (NoSuchElementException NE) {
                            J++;
                            StringWriter sw = new StringWriter();
                            NE.printStackTrace(new PrintWriter(sw));
                            NE.printStackTrace();
                            exceptionAsString = sw.toString();
                            groupResult.put("steps", (((groupStep.toString().split(":"))[1]).replace('@', ' ')).replace("  ", " "));
                            groupResult.put("status", "fail");
                            groupResult.put("errorMsg", "Locator not find. Please add new locator or update the locator value.");
                            groupResult.put("fullStackTrace", exceptionAsString);
                            break;
                        } catch (NullPointerException npe) {
                            J++;
                            StringWriter sw = new StringWriter();
                            npe.printStackTrace(new PrintWriter(sw));
                            npe.printStackTrace();
                            exceptionAsString = sw.toString();
                            groupResult.put("steps", (((groupStep.toString().split(":"))[1]).replace('@', ' ')).replace("  ", " "));
                            groupResult.put("status", "fail");
                            groupResult.put("errorMsg", "Data not found. Please add data.");
                            groupResult.put("fullsTackTrace", exceptionAsString);
                            break;
                        } catch (FileNotFoundException fe) {
                            J++;
                            StringWriter sw = new StringWriter();
                            fe.printStackTrace(new PrintWriter(sw));
                            fe.printStackTrace();
                            exceptionAsString = sw.toString();
                            groupResult.put("steps", (((groupStep.toString().split(":"))[1]).replace('@', ' ')).replace("  ", " "));
                            groupResult.put("status", "fail");
                            groupResult.put("errorMsg", "Please Enter valid directory path. " + fe.getMessage());
                            logger.errorLog("Please Enter valid directory path. " + fe.getMessage());
                            groupResult.put("fullStackTrace", exceptionAsString);
                            break;
                        } catch (Exception e) {
                            J++;
                            StringWriter sw = new StringWriter();
                            e.printStackTrace(new PrintWriter(sw));
                            e.printStackTrace();
                            exceptionAsString = sw.toString();
                            groupResult.put("steps", (((groupStep.toString().split(":"))[1]).replace('@', ' ')).replace("  ", " "));
                            groupResult.put("status", "fail");
                            groupResult.put("errorMsg", "Please check the steps.");
                            groupResult.put("fullStackTrace", exceptionAsString);
                            break;
                        }
                    } else if (groupStep.toString().toLowerCase().contains("verify:") | groupStep.toString().toLowerCase().contains("verify :")) {
                        try {
                            logger.stepLog(groupStep.toString());
                            verifyParser.parseVerify(driver, test, groupStep.toString());
                            groupResult.put("steps", (((groupStep.toString().split(":"))[1]).replace('@', ' ')).replace("  ", " "));
                            groupResult.put("status", "pass");
                        } catch (NoSuchElementException NE) {
                            J++;
                            StringWriter sw = new StringWriter();
                            NE.printStackTrace(new PrintWriter(sw));
                            NE.printStackTrace();
                            exceptionAsString = sw.toString();
                            groupResult.put("steps", (((groupStep.toString().split(":"))[1]).replace('@', ' ')).replace("  ", " "));
                            groupResult.put("status", "fail");
                            groupResult.put("errorMsg", "PLocator not find. Please add new locator or update the locator.");
                            groupResult.put("fullsTackTrace", exceptionAsString);
                        } catch (NullPointerException npe) {
                            J++;
                            StringWriter sw = new StringWriter();
                            npe.printStackTrace(new PrintWriter(sw));
                            npe.printStackTrace();
                            exceptionAsString = sw.toString();
                            groupResult.put("steps", (((groupStep.toString().split(":"))[1]).replace('@', ' ')).replace("  ", " "));
                            groupResult.put("status", "fail");
                            groupResult.put("errorMsg", "Data not found. Please add data.");
                            groupResult.put("fullsTackTrace", exceptionAsString);
                        } catch (FileNotFoundException fe) {
                            J++;
                            StringWriter sw = new StringWriter();
                            fe.printStackTrace(new PrintWriter(sw));
                            fe.printStackTrace();
                            exceptionAsString = sw.toString();
                            groupResult.put("steps", (((groupStep.toString().split(":"))[1]).replace('@', ' ')).replace("  ", " "));
                            groupResult.put("status", "fail");
                            groupResult.put("errorMsg", "Please Enter valid directory path. " + fe.getMessage());
                            logger.errorLog("Please Enter valid directory path. " + fe.getMessage());
                            groupResult.put("fullStackTrace", exceptionAsString);
                        } catch (Exception e) {
                            J++;
                            StringWriter sw = new StringWriter();
                            e.printStackTrace(new PrintWriter(sw));
                            e.printStackTrace();
                            exceptionAsString = sw.toString();
                            groupResult.put("steps", (((groupStep.toString().split(":"))[1]).replace('@', ' ')).replace("  ", " "));
                            groupResult.put("status", "fail");
                            groupResult.put("errorMsg", "Please check the steps.");
                            groupResult.put("fullStackTrace", exceptionAsString);
                        }
                    }
                    long stopTimeStep = System.currentTimeMillis();
                    groupResult.put("endTime", dtf.format(LocalDateTime.now()));
                    long elapsedTimeStep = stopTimeStep - startTimeStep;
                    groupResult.put("time", elapsedTimeStep);
                    stepsArray.add(groupResult);
                    stepNumber++;
                    if(failFlag==true){
                        break;
                    }
                }
            }
        }

        long stopTimeSuite = System.currentTimeMillis();

        testResult.put("endTime", dtf.format(LocalDateTime.now()));
        testResult.put("testStep", stepsArray);
        long elapsedTimeSuite = stopTimeSuite - startTimeSuite;
        testResult.put("totalTime", elapsedTimeSuite);
        testResult.put("suiteName", test.get("suiteName").toString());
        String browserName = caps.getBrowserName();
        testResult.put("browserName", browserName);
        String browserVersion = caps.getVersion();
        testResult.put("browserVersion", browserVersion);
        String os = System.getProperty("os.name");
        testResult.put("osName", os);
        testResult.put("tag", test.get("tag"));
        if (J >= 1) {
            testResult.put("status", "fail");
            testResult.put("screenshot", stepParser.screenshot(driver,test.get("suiteName").toString(),test.get("testName").toString()));
            testResult.put("fullsTackTrace",exceptionAsString);
        } else {
            testResult.put("status", "pass");
        }
        return testResult;
    }

    @Override
    public void run() {
        JSONObject testData = new JSONObject();
        beforeTest(test.get("browser").toString());
        runTest();
        afterTest();

        testData.put(testResult.get("testName").toString(), testResult);

        //addDataIntoMainObject(test.get("browser").toString(), testData);
        TestExecutionBuilder builder = new TestExecutionBuilder();
        if ((builder.mainObj.get("testCase")) == null) {
            JSONArray stepsArray = new JSONArray();
            stepsArray.add(testResult);
            builder.mainObj.put("testCase", stepsArray);
        } else {
            String Name = test.get("suiteName").toString();
            JSONArray test = (JSONArray) builder.mainObj.get("testCase");
            test.add(testResult);
        }
   }

    /**
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     * @param browserName
     * @return
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


}
