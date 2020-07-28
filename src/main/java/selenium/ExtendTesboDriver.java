package selenium;

import framework.TestExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;
import org.openqa.selenium.support.pagefactory.ElementLocatorFactory;

public class ExtendTesboDriver extends TestExecutor {
    int driverWait = 15;

    /**
     * Initialize UserAbstractPage.
     *
     * @param driver
     */
    public ExtendTesboDriver(WebDriver driver) {

        this.driver = driver;
        ElementLocatorFactory finder = new AjaxElementLocatorFactory(driver,
                driverWait);

        PageFactory.initElements(finder, this);

    }
}
