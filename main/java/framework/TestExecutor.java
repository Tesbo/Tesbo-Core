package framework;

import io.github.bonigarcia.wdm.ChromeDriverManager;
import io.github.bonigarcia.wdm.FirefoxDriverManager;
import io.github.bonigarcia.wdm.OperaDriverManager;
import junit.framework.TestResult;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;

public class TestExecutor implements Runnable {


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


    public void runTest() {


        SuiteParser parser = new SuiteParser();
        StepParser stepParser = new StepParser();
        VerifyParser verifyParser = new VerifyParser();

        JSONArray steps = parser.getTestStepBySuiteandTestCaseName(test.get("suiteName").toString(), test.get("testName").toString());

        System.out.println(steps.size());
        JSONArray stepResultArray = new JSONArray();
        JSONObject stepResult = new JSONObject();

        for (Object step : steps) {

            if (step.toString().toLowerCase().contains("step:") | step.toString().toLowerCase().contains("step :")) {
                System.out.println(step.toString());
                stepParser.parseStep(driver, test.get("suiteName").toString(), step.toString());
                stepResult.put("testStep", step);
            } else if (step.toString().toLowerCase().contains("verify:") | step.toString().toLowerCase().contains("verify :")) {
                System.out.println(step.toString());
                verifyParser.parseVerify(driver, test.get("suiteName").toString(), step.toString());
                stepResult.put("testVerifyl", step);
            }

        }

    }


    @Override
    public void run() {

        System.out.println("Test Started " + test.get("testName") + "Browser " + test.get("browser"));
        beforeTest(test.get("browser").toString());
        runTest();
        afterTest();

    }


}
