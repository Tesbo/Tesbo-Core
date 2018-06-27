package ExtTestCode;

import Selenium.ExtendTesboDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import ExtCode.*;

import java.util.Random;


public class Code extends ExtendTesboDriver {
    public Code(WebDriver driver) {
        super(driver);
    }


    @ExtCode("url")
    public void ExternalCode() {
        driver.get("https://www.amazon.in");
        System.out.println("Hello My Tag");
    }


  /*  public static void main(String[] args) {
        Random();
    }*/

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

    @ExtCode("Window Resize")
    public void WindowResize() {
        Dimension dimension = new Dimension(900, 750);
        driver.manage().window().setSize(dimension);

    }

}

