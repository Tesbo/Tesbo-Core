package Selenium;

import framework.GetConfiguration;
import io.github.bonigarcia.wdm.ChromeDriverManager;
import io.github.bonigarcia.wdm.FirefoxDriverManager;
import io.github.bonigarcia.wdm.OperaDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;

public class TestExecutor {


    WebDriver driver;

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
            }


            if (browserName.equalsIgnoreCase("ie")) {
                OperaDriverManager.getInstance().setup();
                driver = new InternetExplorerDriver();
            }

            try{
                driver.get(config.getBaseUrl());

            }catch(org.openqa.selenium.WebDriverException e)
            {
                System.out.println("Base URL is not defined");
            }


        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    public void afterTest() {
        driver.quit();
    }








}
