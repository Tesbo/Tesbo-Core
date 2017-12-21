import framework.StepParser;
import org.junit.Assert;
import org.testng.annotations.Test;


public class StepTester {



    @Test
    public void verifyTextTobeEnterFromStep()
    {
        //Assert.assertTrue(loginFileData.contains("SuiteName: Login"));
        StepParser parser = new StepParser();
        parser.parseTextToEnter("Verify that @email was received");
        Assert.assertTrue(parser.parseTextToEnter("Verify that @email was received").equals(""));
        Assert.assertTrue(parser.parseTextToEnter("Verify that @email was 'received'").equals("received"));
        Assert.assertTrue(parser.parseTextToEnter("Verify that @email was ''received").equals(""));

    }


    




}
