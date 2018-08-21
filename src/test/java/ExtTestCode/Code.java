package ExtTestCode;

import Selenium.ExtendTesboDriver;
import org.openqa.selenium.*;
import ExtCode.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;

import java.util.Random;


public class Code extends ExtendTesboDriver {
    public Code(WebDriver driver) {
        super(driver);
    }

    static String LowestPriceMobile;

    @ExtCode("google")
    public void ExternalCode() {
        driver.get("https://www.google.in");
        System.out.println("Hello My Tag");
    }


    public static void main(String[] args) throws InterruptedException {




    }
    @ExtCode("Random")
    public void Random() {
        //driver.get("https://www.amazon.in");
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 18) {
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        System.out.println(saltStr);
        driver.findElement(By.xpath("//input[@id='twotabsearchtextbox']")).sendKeys(saltStr);


    }
    @ExtCode("RandomError")
    public void RandomError() {
        //driver.get("https://www.amazon.in");
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 18) {
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        System.out.println(saltStr);
        driver.findElement(By.xpath("//input[='twotabsearchtext'")).sendKeys(saltStr);


    }

    @ExtCode("Window Resize")
    public void WindowResize() {
        Dimension dimension = new Dimension(900, 750);
        driver.manage().window().setSize(dimension);

    }

    @ExtCode("signOut")
    public void me() throws InterruptedException {
        driver.findElement(By.xpath("//*[@class='nav-item__profile-member-photo nav-item__icon']")).click();
        Thread.sleep(2000);
        driver.findElement(By.xpath("//*[@data-control-name='nav.settings_signout']")).click();
    }

    @ExtCode("Get Data")
    public void getName() throws InterruptedException {
        driver.findElement(By.xpath("//SELECT[@id='sort']")).click();
        Thread.sleep(1000);
        driver.findElement(By.xpath("//*[@id='sort']/option[3]")).click();
        Thread.sleep(4000);
        WebElement name = driver.findElement(By.xpath("(//*[@class='a-link-normal s-access-detail-page  s-color-twister-title-link a-text-normal'])[2]"));
        LowestPriceMobile = name.getText();
    }

    @ExtCode("Send Data")
    public void sendName() throws InterruptedException {
        Thread.sleep(1000);
        driver.findElement(By.xpath("//INPUT[@type='search']")).sendKeys(LowestPriceMobile);
    }
    public void RandomNo(int length) {

        String SALTCHARS = "1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < length) {
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        System.out.println("Random no : "+saltStr);

    }

    public static void RandomNoAlpha(int length) {
        length = 20;
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < length) {
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        System.out.println("Random no Alpha : "+saltStr);
    }

    public static void RandomAlpha(int length) {
        length = 20;
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < length) {
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        System.out.println("Random alpha : "+saltStr);

    }
    @ExtCode("JavascriptExecutor")
    public void jsSendKey() throws InterruptedException {
        JavascriptExecutor myExecutor = ((JavascriptExecutor) driver);
        driver.get("http://demo.guru99.com/V4/");
        WebElement button =driver.findElement(By.name("btnLogin"));
        Thread.sleep(  2000);
        myExecutor.executeScript("window.scrollBy(600,500)");
        //Login to Guru99
        driver.findElement(By.name("uid")).sendKeys("mngr34926");
        driver.findElement(By.name("password")).sendKeys("amUpenu");
        Thread.sleep(2000);
        //Perform Click on LOGIN button using JavascriptExecutor
        myExecutor.executeScript("arguments[0].click();", button);
        Thread.sleep(10000);

    }


}

