package framework;

import Execution.TestExecutionBuilder;

import io.github.bonigarcia.wdm.ChromeDriverManager;
import io.github.bonigarcia.wdm.FirefoxDriverManager;
import io.github.bonigarcia.wdm.InternetExplorerDriverManager;

import io.github.bonigarcia.wdm.WebDriverManager;
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

        builder.buildExecution();

        long stopTimeSuite = System.currentTimeMillis();
        long elapsedTimeSuite = stopTimeSuite - startTimeSuite;


        DateTimeFormatter dtf1 = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

        report.generateReportDir();
        //report.writeJsonFile(builder.reportObj, builder.getbuildReportName());
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
            if(capabilities!=null)
                capability= setCapabilities(capabilities,capability);
        }
        try {
                if (browserName.equalsIgnoreCase("firefox")) {
                    capability = DesiredCapabilities.firefox();
                    WebDriverManager.firefoxdriver().setup();
                    System.setProperty(FirefoxDriver.SystemProperty.DRIVER_USE_MARIONETTE,"true");
                    System.setProperty(FirefoxDriver.SystemProperty.BROWSER_LOGFILE,"/dev/null");
                    if(seleniumAddress==null) {
                        driver = new FirefoxDriver();
                    }
                }
                if (browserName.equalsIgnoreCase("chrome")) {
                    capability = DesiredCapabilities.chrome();
                    WebDriverManager.chromedriver().setup();

                    if(seleniumAddress==null) {
                        driver = new ChromeDriver();
                    }
                }
                if (browserName.equalsIgnoreCase("ie")) {
                    capability = DesiredCapabilities.internetExplorer();
                    WebDriverManager.iedriver().setup();
                    if(seleniumAddress==null) {
                        driver = new InternetExplorerDriver();
                    }
                }



            if(seleniumAddress !=null)
            {
                driver=openRemoteBrowser(driver,capability,seleniumAddress);
            }


            driver.manage().window().maximize();

            try {
                if (!config.getBaseUrl().equals("") || !config.getBaseUrl().equals(null)) {
                    driver.get(config.getBaseUrl());
                }
            }
            catch (org.openqa.selenium.WebDriverException e) {
                e.printStackTrace();
            }
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


        JSONArray steps = parser.getTestStepBySuiteandTestCaseName(test.get("suiteName").toString(), test.get("testName").toString());

        int J = 0;
        JSONArray stepsArray = new JSONArray();
        boolean failFlag = false;

        String exceptionAsString = null;


        for (int i = 0; i <= steps.size() - 1; i++) {

            long startTimeStep = System.currentTimeMillis();
            Object step = steps.get(i);

            if (step.toString().toLowerCase().replaceAll("\\s{2,}", " ").trim().contains("step:") | step.toString().toLowerCase().replaceAll("\\s{2,}", " ").trim().contains("step :")) {
                try {
                    stepParser.parseStep(driver, test, step.toString());
                } catch (Exception ae) {
                }
                long stopTimeStep = System.currentTimeMillis();
                stepNumber++;
                if(failFlag==true){
                    break;
                }
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
                     failFlag = true;
                }
                long stopTimeStep = System.currentTimeMillis();
                 long elapsedTimeStep = stopTimeStep - startTimeStep;
                // testResult.put(stepNumber + 1, stepResult);
                 stepNumber++;
                if(failFlag==true){
                    break;
                }
            } else if (step.toString().toLowerCase().replaceAll("\\s{2,}", " ").trim().contains("collection:") | step.toString().toLowerCase().replaceAll("\\s{2,}", " ").trim().contains("collection :")) {
                JSONArray groupSteps = new JSONArray();
                try {
                    groupSteps = suiteParser.getGroupTestStepBySuiteandTestCaseName(test.get("suiteName").toString(), stepParser.getCollectionName(step.toString()));
                } catch (Exception e) {
                    J++;
                     StringWriter sw = new StringWriter();
                    e.printStackTrace(new PrintWriter(sw));
                    e.printStackTrace();
                    exceptionAsString = sw.toString();
                     stepNumber++;
                    failFlag = true;
                }

                stepsArray = new JSONArray();

                for (int s = 0; s <= groupSteps.size() - 1; s++) {
                    Object groupStep = groupSteps.get(s);

                    if (groupStep.toString().toLowerCase().contains("step:") | groupStep.toString().toLowerCase().contains("step :")) {
                        try {
                            stepParser.parseStep(driver, test, groupStep.toString());
                        } catch (Exception ae) {
                            J++;
                            StringWriter sw = new StringWriter();
                            ae.printStackTrace(new PrintWriter(sw));
                            ae.printStackTrace();
                            exceptionAsString = sw.toString();
                            break;
                        }
                    } else if (groupStep.toString().toLowerCase().contains("verify:") | groupStep.toString().toLowerCase().contains("verify :")) {
                        try {
                            logger.stepLog(groupStep.toString());
                            verifyParser.parseVerify(driver, test, groupStep.toString());
                         } catch (Exception NE) {
                            J++;
                            StringWriter sw = new StringWriter();
                            NE.printStackTrace(new PrintWriter(sw));
                            NE.printStackTrace();
                            exceptionAsString = sw.toString();
                       }
                    }
                    long stopTimeStep = System.currentTimeMillis();
                stepNumber++;
                    if(failFlag==true){
                        break;
                    }
                }
            }
        }

        long stopTimeSuite = System.currentTimeMillis();


        return testResult;
    }

    @Override
    public void run() {
        try {
            JSONObject testData = new JSONObject();
            beforeTest(test.get("browser").toString());
            runTest();
            afterTest();

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
