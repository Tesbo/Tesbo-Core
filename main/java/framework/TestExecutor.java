package framework;

import Execution.TestExecutionBuilder;
import io.github.bonigarcia.wdm.ChromeDriverManager;
import io.github.bonigarcia.wdm.FirefoxDriverManager;
import io.github.bonigarcia.wdm.OperaDriverManager;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TestExecutor implements Runnable {


    public JSONObject testResult = new JSONObject();
    public JSONObject suiteResult = new JSONObject();
    JSONObject test;
    WebDriver driver;

    public TestExecutor(JSONObject test) {
        this.test = test;
    }

    public void beforeTest(String browserName) {

        GetConfiguration config = new GetConfiguration();

        try {
            if (browserName.equalsIgnoreCase("firefox")) {
                FirefoxDriverManager.getInstance().setup();
                driver = new FirefoxDriver();
            }
            if (browserName.equalsIgnoreCase("chrome")) {

                ChromeDriverManager.getInstance().setup();

                driver = new ChromeDriver();
                driver.manage().window().maximize();
            }


            if (browserName.equalsIgnoreCase("ie")) {
                OperaDriverManager.getInstance().setup();
                driver = new InternetExplorerDriver();
            }

            try {
                System.out.println(config.getBaseUrl().equals(""));
                if (!config.getBaseUrl().equals("") || !config.getBaseUrl().equals(null)) {
                    driver.get(config.getBaseUrl());
                } else {
                }
            } catch (org.openqa.selenium.WebDriverException e) {
            }


        } catch (Exception e) {
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

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy|MM|dd HH:mm:ss");

        long startTimeSuite = System.currentTimeMillis();
        testResult.put("startTime", dtf.format(LocalDateTime.now()));
        testResult.put("testName", test.get("testName").toString());

        JSONArray steps = parser.getTestStepBySuiteandTestCaseName(test.get("suiteName").toString(), test.get("testName").toString());

        int J = 0;
        for (int i = 0; i <= steps.size() - 1; i++) {
            JSONObject stepResult = new JSONObject();

            long startTimeStep = System.currentTimeMillis();
            stepResult.put("startTime", dtf.format(LocalDateTime.now()));
            stepResult.put("stepIndex", i + 1);
            Object step = steps.get(i);
            if (step.toString().toLowerCase().contains("step:") | step.toString().toLowerCase().contains("step :")) {
                try {
                    System.out.println(step);
                    stepParser.parseStep(driver, test.get("suiteName").toString(), step.toString());
                    stepResult.put("steps", (((step.toString().split(": "))[1]).replace('@', ' ')).replace("  ", " "));
                    stepResult.put("status", "pass");
                } catch (NoAlertPresentException ae) {
                    J++;
                    StringWriter sw = new StringWriter();
                    ae.printStackTrace(new PrintWriter(sw));
                    ae.printStackTrace();
                    String exceptionAsString = sw.toString();
                    stepResult.put("steps", (((step.toString().split(": "))[1]).replace('@', ' ')).replace("  ", " "));
                    stepResult.put("status", "fail");
                    stepResult.put("errorMsg", "Please review steps. Alert is not display.");
                    stepResult.put("fullStackTrace", exceptionAsString);
                }catch (NoSuchElementException NE) {
                    J++;
                    StringWriter sw = new StringWriter();
                    NE.printStackTrace(new PrintWriter(sw));
                    NE.printStackTrace();
                    String exceptionAsString = sw.toString();
                    stepResult.put("steps", (((step.toString().split(": "))[1]).replace('@', ' ')).replace("  ", " "));
                    stepResult.put("status", "fail");
                    stepResult.put("errorMsg", "Locator not find. Please add new locator or update the locator value.");
                    stepResult.put("fullStackTrace", exceptionAsString);
                } catch (NullPointerException npe) {
                    J++;
                    StringWriter sw = new StringWriter();
                    npe.printStackTrace(new PrintWriter(sw));
                    npe.printStackTrace();
                    String exceptionAsString = sw.toString();
                    stepResult.put("steps", (((step.toString().split(": "))[1]).replace('@', ' ')).replace("  ", " "));
                    stepResult.put("status", "fail");
                    stepResult.put("errorMsg", "Data not found. Please add data.");
                    stepResult.put("fullsTackTrace", exceptionAsString);
                } catch (FileNotFoundException fe) {
                    J++;
                    StringWriter sw = new StringWriter();
                    fe.printStackTrace(new PrintWriter(sw));
                    fe.printStackTrace();
                    String exceptionAsString = sw.toString();
                    stepResult.put("steps", (((step.toString().split(": "))[1]).replace('@', ' ')).replace("  ", " "));
                    stepResult.put("status", "fail");
                    stepResult.put("errorMsg", "Please Enter valid directory path. " + fe.getMessage());
                    System.out.println("Please Enter valid directory path. " + fe.getMessage());
                    stepResult.put("fullStackTrace", exceptionAsString);
                } catch (Exception e) {
                    J++;
                    StringWriter sw = new StringWriter();
                    e.printStackTrace(new PrintWriter(sw));
                    e.printStackTrace();
                    String exceptionAsString = sw.toString();
                    stepResult.put("steps", (((step.toString().split(": "))[1]).replace('@', ' ')).replace("  ", " "));
                    stepResult.put("status", "fail");
                    stepResult.put("errorMsg", "Please check the steps.");
                    stepResult.put("fullStackTrace", exceptionAsString);
                }
            } else if (step.toString().toLowerCase().contains("verify:") | step.toString().toLowerCase().contains("verify :")) {
                try {
                    System.out.println(step.toString());
                    verifyParser.parseVerify(driver, test.get("suiteName").toString(), step.toString());
                    stepResult.put("steps", (((step.toString().split(": "))[1]).replace('@', ' ')).replace("  ", " "));
                    stepResult.put("status", "pass");
                } catch (NoSuchElementException NE) {
                    J++;
                    StringWriter sw = new StringWriter();
                    NE.printStackTrace(new PrintWriter(sw));
                    NE.printStackTrace();
                    String exceptionAsString = sw.toString();
                    stepResult.put("steps", (((step.toString().split(": "))[1]).replace('@', ' ')).replace("  ", " "));
                    stepResult.put("status", "fail");
                    stepResult.put("errorMsg", "PLocator not find. Please add new locator or update the locator.");
                    stepResult.put("fullsTackTrace", exceptionAsString);
                } catch (NullPointerException npe) {
                    J++;
                    StringWriter sw = new StringWriter();
                    npe.printStackTrace(new PrintWriter(sw));
                    npe.printStackTrace();
                    String exceptionAsString = sw.toString();
                    stepResult.put("steps", (((step.toString().split(": "))[1]).replace('@', ' ')).replace("  ", " "));
                    stepResult.put("status", "fail");
                    stepResult.put("errorMsg", "Data not found. Please add data.");
                    stepResult.put("fullsTackTrace", exceptionAsString);
                } catch (FileNotFoundException fe) {
                    J++;
                    StringWriter sw = new StringWriter();
                    fe.printStackTrace(new PrintWriter(sw));
                    fe.printStackTrace();
                    String exceptionAsString = sw.toString();
                    stepResult.put("steps", (((step.toString().split(": "))[1]).replace('@', ' ')).replace("  ", " "));
                    stepResult.put("status", "fail");
                    stepResult.put("errorMsg", "Please Enter valid directory path. " + fe.getMessage());
                    System.out.println("Please Enter valid directory path. " + fe.getMessage());
                    stepResult.put("fullStackTrace", exceptionAsString);
                }catch (Exception e) {
                    J++;
                    StringWriter sw = new StringWriter();
                    e.printStackTrace(new PrintWriter(sw));
                    e.printStackTrace();
                    String exceptionAsString = sw.toString();
                    stepResult.put("steps", (((step.toString().split(": "))[1]).replace('@', ' ')).replace("  ", " "));
                    stepResult.put("status", "fail");
                    stepResult.put("errorMsg", "Please check the steps.");
                    stepResult.put("fullStackTrace", exceptionAsString);
                }
            }
            long stopTimeStep = System.currentTimeMillis();
            stepResult.put("endTime", dtf.format(LocalDateTime.now()));
            long elapsedTimeStep = stopTimeStep - startTimeStep;
            stepResult.put("time", elapsedTimeStep);
            testResult.put(i + 1, stepResult);
        }

        long stopTimeSuite = System.currentTimeMillis();
        testResult.put("endTime", dtf.format(LocalDateTime.now()));
        long elapsedTimeSuite = stopTimeSuite - startTimeSuite;
        //System.out.println(elapsedTimeSuite);
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
        } else {
            testResult.put("status", "pass");
        }
        //System.out.println("Test Result : " + testResult.toString());
        return testResult;
    }

    @Override
    public void run() {
        TestExecutionBuilder builder = new TestExecutionBuilder();
        JSONObject testData = new JSONObject();
        System.out.println("Test Started " + test.get("testName") + "Browser " + test.get("browser"));
        beforeTest(test.get("browser").toString());
        runTest();
        afterTest();

        testData.put(testResult.get("testName").toString(), testResult);

        if ((builder.mainObj.get(test.get("suiteName"))) == null) {
            builder.mainObj.put(testResult.get("suiteName").toString(), testData);
        } else {
            String Name = test.get("suiteName").toString();
            JSONObject test = (JSONObject) builder.mainObj.get(Name);
            test.put(testResult.get("testName").toString(), testResult);
        }
        System.out.println("run test : " + builder.mainObj);
    }


}
