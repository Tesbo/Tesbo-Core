import framework.StepParser;
import org.junit.Assert;
import org.testng.annotations.Test;


public class StepTester {


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
