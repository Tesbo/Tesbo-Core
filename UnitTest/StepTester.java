import framework.StepParser;
import org.junit.Assert;
import org.testng.annotations.Test;


public class StepTester {



    @Test
    public void verifyTextTobeEnterFromStep()
    {
        //Assert.assertTrue(loginFileData.contains("SuiteName: Login"));
        StepParser parser = new StepParser();
       Assert.assertTrue(parser.parseTextToEnter("Enter '' into @emailTextBox").equals(""));
        Assert.assertTrue(parser.parseTextToEnter("Enter 'viral103patel@gmail.com Test' into @emailTextBox").equals("viral103patel@gmail.com Test"));
        Assert.assertTrue(parser.parseTextToEnter("Enter 'viral103patel@gmail.com' into @emailTextBox").equals("viral103patel@gmail.com"));
        Assert.assertTrue(parser.parseTextToEnter("Enter viral103patel@gmail.com'' into @emailTextBox").equals(""));
        Assert.assertTrue(parser.parseTextToEnter("Enter viral103patel@gmail.com into @emailTextBox").equals(""));

    }





    




}
