import framework.StepParser;
import io.github.bonigarcia.wdm.ChromeDriverManager;
import org.junit.Assert;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.testng.annotations.Test;


public class StepTester {

    protected WebDriver driver;

    StepTester()
    {
        DesiredCapabilities capability = null;
        capability = DesiredCapabilities.chrome();
        ChromeDriverManager.getInstance().setup();
        capability.setJavascriptEnabled(true);
        driver = new ChromeDriver(capability);
        driver.manage().window().maximize();
    }


      @Test
    public void verifyTextTobeEnterFromStep() {

        //Assert.assertTrue(loginFileData.contains("SuiteName: Login"));
        StepParser parser = new StepParser();
        Assert.assertTrue(parser.parseTextToEnter("Enter '' into @emailTextBox").equals(""));
        Assert.assertTrue(parser.parseTextToEnter("Enter 'viral103patel@gmail.com Test' into @emailTextBox").equals("viral103patel@gmail.com Test"));
        Assert.assertTrue(parser.parseTextToEnter("Enter 'viral103patel@gmail.com' into @emailTextBox").equals("viral103patel@gmail.com"));
        Assert.assertTrue(parser.parseTextToEnter("Enter viral103patel@gmail.com'' into @emailTextBox").equals(""));
        Assert.assertTrue(parser.parseTextToEnter("Enter viral103patel@gmail.com into @emailTextBox").equals(""));
    }


    @Test
    public void verifyElementNameWithValidSyntax() {
        StepParser parser = new StepParser();
        Assert.assertTrue(parser.parseElementName("Enter '' into @emailTextBox").equals("emailTextBox"));
    }

    @Test
    public void verifyElementNameWithDoubleATToken() {
        StepParser parser = new StepParser();
        // for this condition message should be displayed like incorrect token found
        Assert.assertTrue(parser.parseElementName("Enter 'Test' into @@emailTextBox").equals("emailTextBox"));

    }

    @Test
    public void verifyElementNameWithSpaceInElementName() {
        StepParser parser = new StepParser();
        //for this messase should displayed like "please enter correct element name"
        Assert.assertTrue(parser.parseElementName("Enter '' into @ emailTextBox").equals("emailTextBox"));
    }

    @Test
    public void verifyElementNameWithBlankElementName() {
        StepParser parser = new StepParser();
        //for this messase should displayed like "please enter correct element name"
        Assert.assertTrue(parser.parseElementName("Enter '' into @").equals(""));
    }


}
